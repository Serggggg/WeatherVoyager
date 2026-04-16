package com.example.weathervoyager.domain.weather

/**
 * Чистая доменная модель содержащая агрегированную информацию о погоде.
 */
data class WeatherInfo(
    val currentTemperature: Double,
    val relativeHumidity: Double,
    val isDay: Boolean,
    val precipitation: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val weatherCode: Int,
    val dailyForecast: List<DailyForecast>
)

data class DailyForecast(
    val time: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int
)
