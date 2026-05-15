package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class BusTrackerActivity : AppCompatActivity() {

    private val routes = listOf("Select Route", "201R (Majestic)", "335E (KBS)", "500C (Silk Board)")
    private lateinit var routeSpinner: Spinner
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var scheduleRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_tracker)

        val userName = AppPrefsHelper.getName(this).ifEmpty { SharedPreferencesHelper.getUserName(this) }
        Toast.makeText(this, "Welcome, $userName! Tracking your bus...", Toast.LENGTH_SHORT).show()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        routeSpinner = findViewById(R.id.routeSpinner)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        statusTextView = findViewById(R.id.statusTextView)
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView)

        setupSpinner()
        scheduleRecyclerView.layoutManager = LinearLayoutManager(this)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        // Bus Tracker is a sub-page, but we show the footer for navigation consistency
        // Since it's not a main tab, we can leave selectedItem as none or home
        bottomNavigation.selectedItemId = R.id.nav_home 

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

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, routes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        routeSpinner.adapter = adapter

        routeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    statusTextView.visibility = View.GONE
                    scheduleRecyclerView.adapter = null
                    return
                }

                showLoadingAndData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showLoadingAndData() {
        loadingProgressBar.visibility = View.VISIBLE
        statusTextView.visibility = View.GONE
        scheduleRecyclerView.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            loadingProgressBar.visibility = View.GONE
            statusTextView.visibility = View.VISIBLE
            scheduleRecyclerView.visibility = View.VISIBLE
            
            statusTextView.text = "Next bus in 5 minutes"
            
            val dummyData = listOf(
                BusSchedule("10:00 AM", "Electronic City", "On Time"),
                BusSchedule("10:15 AM", "Silk Board", "Delayed"),
                BusSchedule("10:30 AM", "HSR Layout", "On Time"),
                BusSchedule("10:45 AM", "Koramangala", "On Time")
            )
            scheduleRecyclerView.adapter = BusScheduleAdapter(dummyData)
        }, 1000)
    }

    class BusScheduleAdapter(private val schedules: List<BusSchedule>) :
        RecyclerView.Adapter<BusScheduleAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val timeTextView: TextView = view.findViewById(R.id.timeTextView)
            val stopNameTextView: TextView = view.findViewById(R.id.stopNameTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_schedule, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = schedules[position]
            holder.timeTextView.text = item.arrivalTime
            holder.stopNameTextView.text = item.stopName
        }

        override fun getItemCount() = schedules.size
    }
}
