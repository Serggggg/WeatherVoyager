package com.example.weathervoyager.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("current") val currentWeather: CurrentWeatherDto,
    @SerialName("daily") val daily: DailyWeatherDto
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val relativeHumidity: Double,
    @SerialName("is_day") val isDay: Int,
    @SerialName("precipitation") val precipitation: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("wind_direction_10m") val windDirection: Int
)

@Serializable
data class DailyWeatherDto(
    @SerialName("time") val time: List<String>,
    @SerialName("temperature_2m_max") val maxTemp: List<Double>,
    @SerialName("temperature_2m_min") val minTemp: List<Double>,
    @SerialName("weathercode") val weatherCode: List<Int>
)
