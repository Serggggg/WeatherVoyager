package com.example.weathervoyager.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponseDto(
    @SerialName("results") val results: List<GeocodingDto>? = null
)

@Serializable
data class GeocodingDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("country") val country: String? = null
)
