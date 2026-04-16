package com.example.weathervoyager.data.repository

import com.example.weathervoyager.data.remote.WeatherApi
import com.example.weathervoyager.data.remote.mapper.toDomain
import com.example.weathervoyager.domain.location.SearchResult
import com.example.weathervoyager.domain.weather.WeatherInfo
import com.example.weathervoyager.domain.weather.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getWeatherData(lat: Double, lon: Double): Result<WeatherInfo> {
        return try {
            val response = api.getWeatherData(lat = lat, long = lon)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            // Catch all errors (IOException, HttpException, SerializationException)
            Result.failure(e)
        }
    }

    override suspend fun searchLocation(query: String, language: String): Result<List<SearchResult>> {
        return try {
            val response = api.searchLocation(query = query, language = language)
            val results = response.results?.map { it.toDomain() } ?: emptyList()
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
