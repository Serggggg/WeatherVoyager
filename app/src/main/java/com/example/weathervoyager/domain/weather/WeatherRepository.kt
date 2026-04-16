package com.example.weathervoyager.domain.weather

import com.example.weathervoyager.domain.location.SearchResult

/**
 * Контракт для получения данных о погоде (для использования в UseCases или ViewModels).
 */
interface WeatherRepository {

    suspend fun getWeatherData(lat: Double, lon: Double): Result<WeatherInfo>

    suspend fun searchLocation(query: String, language: String): Result<List<SearchResult>>
}
