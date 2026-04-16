package com.example.weathervoyager.domain.location

data class SearchResult(
    val id: Int,
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double
)
