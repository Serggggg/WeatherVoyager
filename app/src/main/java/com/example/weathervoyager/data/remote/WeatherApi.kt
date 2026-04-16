package com.example.weathervoyager.data.remote

import com.example.weathervoyager.data.remote.dto.GeocodingResponseDto
import com.example.weathervoyager.data.remote.dto.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") lat: Double,
        @Query("longitude") long: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,is_day,precipitation,weather_code,wind_speed_10m,wind_direction_10m",
        @Query("wind_speed_unit") windSpeedUnit: String = "ms",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weathercode",
        @Query("timezone") timezone: String = "auto"
    ): WeatherDto

    // Pass the absolute URL (with https://), Retrofit will override the baseUrl for this method.
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchLocation(
        @Query("name") query: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String,
        @Query("format") format: String = "json"
    ): GeocodingResponseDto
}
