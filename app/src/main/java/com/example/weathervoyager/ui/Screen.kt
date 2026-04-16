package com.example.weathervoyager.ui

import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.Serializable

val DoubleType = object : NavType<Double>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Double? =
        if (bundle.containsKey(key)) bundle.getDouble(key) else null

    override fun parseValue(value: String): Double = value.toDouble()

    override fun put(bundle: Bundle, key: String, value: Double) =
        bundle.putDouble(key, value)
}

@Serializable
data object Home

@Serializable
data class Forecast(
    val locationName: String,
    val lat: Double,
    val lon: Double
)
