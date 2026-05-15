package com.example.buss

data class BusStop(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String,
    val vicinity: String
)
