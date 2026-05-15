package com.example.buss

data class BusRoute(
    val routeNo: String,
    val source: String,
    val destination: String,
    val stops: List<String>
)
