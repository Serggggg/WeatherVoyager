package com.example.weathervoyager.data.remote.mapper

import com.example.weathervoyager.data.remote.dto.GeocodingDto
import com.example.weathervoyager.domain.location.SearchResult

fun GeocodingDto.toDomain(): SearchResult {
    return SearchResult(
        id = id,
        name = name,
        country = country ?: "",
        lat = latitude,
        lon = longitude
    )
}
