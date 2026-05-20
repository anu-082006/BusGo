package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class BusTrackerActivity : AppCompatActivity() {

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var tvRouteInfo: TextView
    
    private var selectedRoute: BusRoute? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_tracker)

        // Retrieve the route passed from Search
        selectedRoute = intent.getSerializableExtra("SELECTED_ROUTE") as? BusRoute

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        statusTextView = findViewById(R.id.statusTextView)
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView)
        tvRouteInfo = findViewById(R.id.statusTextView) // Reusing statusTextView or finding another one

        // Hide the spinner since we are using a direct route now
        findViewById<View>(R.id.routeSpinner).visibility = View.GONE

        scheduleRecyclerView.layoutManager = LinearLayoutManager(this)

        if (selectedRoute != null) {
            startTracking()
        } else {
            Toast.makeText(this, "No route selected to track", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupBottomNavigation()
    }

    private fun startTracking() {
        loadingProgressBar.visibility = View.VISIBLE
        scheduleRecyclerView.visibility = View.GONE

        // Simulation delay
        handler.postDelayed({
            loadingProgressBar.visibility = View.GONE
            scheduleRecyclerView.visibility = View.VISIBLE
            
            updateTrackingData()
        }, 800)

        // Refresh data every 30 seconds to update "time remaining"
        updateRunnable = object : Runnable {
            override fun run() {
                updateTrackingData()
                handler.postDelayed(this, 30000)
            }
        }
        handler.post(updateRunnable)
    }

    private fun updateTrackingData() {
        val route = selectedRoute ?: return
        val now = Calendar.getInstance()
        
        // Generate a schedule starting from "now"
        val fullSchedule = generateDynamicStops(route)
        
        // Filter to show only upcoming stops (Time is after now)
        val upcomingStops = fullSchedule.filter { it.calendarTime.after(now) }

        if (upcomingStops.isEmpty()) {
            statusTextView.text = "Bus has reached its destination."
            scheduleRecyclerView.adapter = BusScheduleAdapter(emptyList())
        } else {
            val nextStop = upcomingStops[0]
            val diffMs = nextStop.calendarTime.timeInMillis - now.timeInMillis
            val diffMins = (diffMs / (1000 * 60)).toInt()
            
            statusTextView.visibility = View.VISIBLE
            statusTextView.text = "Next: ${nextStop.stopName} in $diffMins mins"
            scheduleRecyclerView.adapter = BusScheduleAdapter(upcomingStops)
        }
    }

    private fun generateDynamicStops(route: BusRoute): List<StopSchedule> {
        val list = mutableListOf<StopSchedule>()
        val cal = Calendar.getInstance()
        
        // Let's assume the bus started its trip a bit ago, but we only show what's next
        // We'll generate a fixed timeline for this "instance" of the bus
        val intermediateNames = listOf("Main Market", "Police Station", "Public Library", "City Hospital")
        
        // Start Stop (Source) - let's say it was 5 mins ago
        val startCal = Calendar.getInstance().apply { add(Calendar.MINUTE, -5) }
        list.add(StopSchedule(route.source, startCal))

        var currentCal = startCal
        intermediateNames.forEach { name ->
            val nextCal = (currentCal.clone() as Calendar).apply { add(Calendar.MINUTE, 10) }
            list.add(StopSchedule(name, nextCal))
            currentCal = nextCal
        }

        // Final Stop (Destination)
        val endCal = (currentCal.clone() as Calendar).apply { add(Calendar.MINUTE, 10) }
        list.add(StopSchedule(route.destination, endCal))

        return list
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::updateRunnable.isInitialized) {
            handler.removeCallbacks(updateRunnable)
        }
    }

    data class StopSchedule(val stopName: String, val calendarTime: Calendar) {
        val arrivalTime: String
            get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendarTime.time)
    }

    class BusScheduleAdapter(private val schedules: List<StopSchedule>) :
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
            
            // Highlight the first (nearest) stop
            if (position == 0) {
                holder.stopNameTextView.setTextColor(holder.itemView.context.getColor(R.color.bmtc_blue))
                holder.timeTextView.setTextColor(holder.itemView.context.getColor(R.color.bmtc_blue))
            } else {
                holder.stopNameTextView.setTextColor(holder.itemView.context.getColor(R.color.black))
                holder.timeTextView.setTextColor(holder.itemView.context.getColor(R.color.nav_unselected))
            }
        }

        override fun getItemCount() = schedules.size
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home 
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_search -> { startActivity(Intent(this, RouteSearchActivity::class.java)); finish(); true }
                R.id.nav_favorites -> { startActivity(Intent(this, FavouritesActivity::class.java)); finish(); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); finish(); true }
                else -> false
            }
        }
    }
}
