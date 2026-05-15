package com.example.buss

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NearbyStopsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStopName: TextView
    private lateinit var tvStopAddress: TextView
    private lateinit var btnNavigate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_stops)

        // Initialize Views
        progressBar = findViewById(R.id.progressBar)
        tvStopName = findViewById(R.id.tvStopName)
        tvStopAddress = findViewById(R.id.tvStopAddress)
        btnNavigate = findViewById(R.id.btnNavigate)
        val bottomSheet = findViewById<View>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Toolbar Setup
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSOS).setOnClickListener { showSOSDialog() }

        // Initialize SDKs
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }
        placesClient = Places.createClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Map setup
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupSearch()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        // Set to none or home since Nearby Stops is a sub-page
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Enable all gestures for full interactivity
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
            isMyLocationButtonEnabled = true
        }
        
        requestLocationPermission()

        mMap.setOnMarkerClickListener { marker ->
            val stop = marker.tag as? BusStop
            if (stop != null) {
                showBottomSheet(stop)
            }
            false
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }
        mMap.isMyLocationEnabled = true
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    fetchNearbyBusStops(currentLatLng)
                }
            }
        }
    }

    private fun setupSearch() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchPlace(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun searchPlace(query: String) {
        progressBar.visibility = View.VISIBLE
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val searchRequest = SearchByTextRequest.builder(query, fields).build()

        placesClient.searchByText(searchRequest)
            .addOnSuccessListener { response ->
                val place = response.places.firstOrNull()
                if (place != null && place.latLng != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng!!, 15f))
                    fetchNearbyBusStops(place.latLng!!)
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Search failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchNearbyBusStops(location: LatLng) {
        progressBar.visibility = View.VISIBLE
        mMap.clear()
        
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = SearchByTextRequest.builder("bus station", fields)
            .setLocationBias(CircularBounds.newInstance(location, 3000.0))
            .setMaxResultCount(20)
            .build()

        val busIcon = bitmapDescriptorFromVector(R.drawable.ic_bus_marker)

        placesClient.searchByText(request)
            .addOnSuccessListener { response ->
                for (place in response.places) {
                    if (place.latLng != null) {
                        val busStop = BusStop(
                            place.name ?: "Unknown Stop",
                            place.address ?: "No address",
                            place.latLng!!.latitude,
                            place.latLng!!.longitude,
                            place.id ?: "",
                            place.address ?: ""
                        )
                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(place.latLng!!)
                                .title(place.name)
                                .icon(busIcon)
                        )
                        marker?.tag = busStop
                    }
                }
                progressBar.visibility = View.GONE
                if (response.places.isEmpty()) {
                    Toast.makeText(this, R.string.no_stops_found, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load stops: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable?.intrinsicWidth ?: 0, vectorDrawable?.intrinsicHeight ?: 0, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun showBottomSheet(stop: BusStop) {
        tvStopName.text = stop.name
        tvStopAddress.text = stop.address
        btnNavigate.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=${stop.latitude},${stop.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showSOSDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sos_dialog_title)
            .setMessage(R.string.sos_dialog_message)
            .setPositiveButton(R.string.sos_dialog_confirm) { _, _ ->
                Toast.makeText(this, "SOS Alert Sent!", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(R.string.sos_dialog_cancel, null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        }
    }
}
