package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.InputStreamReader

class FavouritesActivity : AppCompatActivity() {

    private lateinit var adapter: RouteAdapter
    private val allRoutes = mutableListOf<BusRoute>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadRoutesFromCsv()
        setupRecyclerView()

        // Bottom Navigation
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_favorites

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, RouteSearchActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_favorites -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        val noFavouritesTextView: TextView = findViewById(R.id.noFavouritesTextView)
        val favouritesRecyclerView: RecyclerView = findViewById(R.id.favouritesRecyclerView)

        val favRouteNos = SharedPreferencesHelper.getFavourites(this)
        val favRoutes = allRoutes.filter { it.routeNo in favRouteNos }

        if (favRoutes.isEmpty()) {
            noFavouritesTextView.visibility = View.VISIBLE
            favouritesRecyclerView.visibility = View.GONE
        } else {
            noFavouritesTextView.visibility = View.GONE
            favouritesRecyclerView.visibility = View.VISIBLE
            favouritesRecyclerView.layoutManager = LinearLayoutManager(this)
            
            adapter = RouteAdapter(favRoutes) { route ->
                // Handle unfavoriting from this page
                val current = SharedPreferencesHelper.getFavourites(this).toMutableSet()
                if (current.contains(route.routeNo)) {
                    current.remove(route.routeNo)
                    Toast.makeText(this, "Removed from favourites", Toast.LENGTH_SHORT).show()
                }
                SharedPreferencesHelper.saveFavourites(this, current)
                
                // Refresh list
                val updatedFavs = allRoutes.filter { it.routeNo in current }
                adapter.updateData(updatedFavs)
                
                if (updatedFavs.isEmpty()) {
                    noFavouritesTextView.visibility = View.VISIBLE
                    favouritesRecyclerView.visibility = View.GONE
                }
            }
            favouritesRecyclerView.adapter = adapter
        }
    }

    private fun loadRoutesFromCsv() {
        try {
            val inputStream = assets.open("routes.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            var isFirstLine = true
            var line: String? = reader.readLine()

            while (line != null) {
                if (isFirstLine) {
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1)
                    }
                    isFirstLine = false
                    line = reader.readLine()
                    continue
                }

                if (line.isBlank()) {
                    line = reader.readLine()
                    continue
                }

                val tokens = line.split(",")
                if (tokens.size >= 4) {
                    val routeNo = tokens[1].trim()
                    val source = tokens[2].trim()
                    val destination = tokens[3].trim()

                    if (routeNo.isNotEmpty()) {
                        allRoutes.add(BusRoute(routeNo, source, destination, listOf(source, destination)))
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
