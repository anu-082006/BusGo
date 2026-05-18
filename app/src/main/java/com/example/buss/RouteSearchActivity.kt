package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.InputStreamReader

class RouteSearchActivity : AppCompatActivity() {

    private lateinit var adapter: RouteAdapter
    private val allRoutes = mutableListOf<BusRoute>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_search)

        val etSource: AutoCompleteTextView = findViewById(R.id.etSource)
        val etDestination: AutoCompleteTextView = findViewById(R.id.etDestination)
        val btnSearch: Button = findViewById(R.id.btnSearch)
        val rvResults: RecyclerView = findViewById(R.id.rvResults)

        loadRoutesFromCsv()

        // Extract unique stop names for autofill
        val stopNames = mutableSetOf<String>()
        allRoutes.forEach {
            stopNames.add(it.source)
            stopNames.add(it.destination)
        }
        val sortedStops = stopNames.toList().sorted()

        val autoCompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sortedStops)
        etSource.setAdapter(autoCompleteAdapter)
        etDestination.setAdapter(autoCompleteAdapter)

        adapter = RouteAdapter(emptyList()) { route ->
            // Toggle favourite using SharedPreferencesHelper
            val current = SharedPreferencesHelper.getFavourites(this).toMutableSet()
            if (current.contains(route.routeNo)) {
                current.remove(route.routeNo)
                Toast.makeText(this, "Removed from favourites: ${route.routeNo}", Toast.LENGTH_SHORT).show()
            } else {
                current.add(route.routeNo)
                Toast.makeText(this, "Added to favourites: ${route.routeNo}", Toast.LENGTH_SHORT).show()
            }
            SharedPreferencesHelper.saveFavourites(this, current)
        }

        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = adapter

        btnSearch.setOnClickListener {
            val sourceQuery = etSource.text.toString().trim()
            val destQuery = etDestination.text.toString().trim()

            if (sourceQuery.isEmpty() && destQuery.isEmpty()) {
                Toast.makeText(this, "Please enter source or destination", Toast.LENGTH_SHORT).show()
            } else {
                val filtered = allRoutes.filter { route ->
                    val sourceMatch = sourceQuery.isEmpty() || route.source.contains(sourceQuery, ignoreCase = true)
                    val destMatch = destQuery.isEmpty() || route.destination.contains(destQuery, ignoreCase = true)
                    sourceMatch && destMatch
                }
                if (filtered.isEmpty()) {
                    Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show()
                }
                adapter.updateData(filtered)
            }
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_search
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_search -> true
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadRoutesFromCsv() {
        try {
            val inputStream = assets.open("routes.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            var isFirstLine = true
            var line: String? = reader.readLine()

            while (line != null) {
                // Remove BOM from first line only
                if (isFirstLine) {
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1)
                    }
                    isFirstLine = false
                    line = reader.readLine() // Skip header
                    continue
                }

                // Skip empty lines
                if (line.isBlank()) {
                    line = reader.readLine()
                    continue
                }

                val tokens = line.split(",")
                if (tokens.size >= 4) {
                    val routeNo = tokens[1].trim()
                    val source = tokens[2].trim()
                    val destination = tokens[3].trim()

                    // Only add if route number is not empty
                    if (routeNo.isNotEmpty()) {
                        allRoutes.add(BusRoute(routeNo, source, destination, listOf(source, destination)))
                    }
                }
                line = reader.readLine()
            }
            reader.close()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading routes: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
