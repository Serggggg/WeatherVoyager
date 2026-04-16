package com.example.weathervoyager.domain.location

/**
 * Простая доменная модель для координат, независимая от платформы Android.
 */
data class Location(
    val latitude: Double,
    val longitude: Double
)
