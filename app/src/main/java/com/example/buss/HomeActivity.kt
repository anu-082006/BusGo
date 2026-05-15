package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Greeting using the logged-in user's name
        val tvGreeting: TextView = findViewById(R.id.tvGreeting)
        val userName = AppPrefsHelper.getName(this).ifEmpty { SharedPreferencesHelper.getUserName(this) }
        tvGreeting.text = if (userName.isNotEmpty()) "Hello, $userName!" else "Hello, Commuter!"

        // Bottom Navigation
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, RouteSearchActivity::class.java))
                    true
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Setup the 3 Action Cards using M2's drawable resources
        setupCard(R.id.cardJourney, "Find Route", "Search buses and routes", R.drawable.ic_bus)
        setupCard(R.id.cardNearby, "Nearby Stops", "Find boarding points near you", R.drawable.ic_location)
        setupCard(R.id.cardTrack, "Track Bus", "Live bus schedule tracker", R.drawable.ic_track)

        // Card click listeners wired to actual activities
        findViewById<View>(R.id.cardJourney).setOnClickListener {
            startActivity(Intent(this, RouteSearchActivity::class.java))
        }

        findViewById<View>(R.id.cardNearby).setOnClickListener {
            startActivity(Intent(this, NearbyStopsActivity::class.java))
        }

        findViewById<View>(R.id.cardTrack).setOnClickListener {
            startActivity(Intent(this, BusTrackerActivity::class.java))
        }
    }

    private fun setupCard(cardId: Int, title: String, subtitle: String, iconRes: Int) {
        val card = findViewById<View>(cardId)
        card.findViewById<TextView>(R.id.title).text = title
        card.findViewById<TextView>(R.id.subtitle).text = subtitle
        card.findViewById<ImageView>(R.id.icon).setImageResource(iconRes)
    }
}
