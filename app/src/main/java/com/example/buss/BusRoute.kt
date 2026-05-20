package com.example.buss

import java.io.Serializable

data class BusRoute(
    val routeNo: String,
    val source: String,
    val destination: String,
    val stops: List<String>
) : Serializable
