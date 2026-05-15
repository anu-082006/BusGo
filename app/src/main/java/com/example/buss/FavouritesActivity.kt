package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class FavouritesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val favourites = SharedPreferencesHelper.getFavourites(this).toList()
        val noFavouritesTextView: TextView = findViewById(R.id.noFavouritesTextView)
        val favouritesRecyclerView: RecyclerView = findViewById(R.id.favouritesRecyclerView)

        if (favourites.isEmpty()) {
            noFavouritesTextView.visibility = View.VISIBLE
            favouritesRecyclerView.visibility = View.GONE
        } else {
            noFavouritesTextView.visibility = View.GONE
            favouritesRecyclerView.visibility = View.VISIBLE
            favouritesRecyclerView.layoutManager = LinearLayoutManager(this)
            favouritesRecyclerView.adapter = FavouritesAdapter(favourites)
        }

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

    class FavouritesAdapter(private val favourites: List<String>) :
        RecyclerView.Adapter<FavouritesAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = favourites[position]
        }

        override fun getItemCount() = favourites.size
    }
}
