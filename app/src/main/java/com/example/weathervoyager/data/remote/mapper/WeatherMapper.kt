package com.example.weathervoyager.data.remote.mapper

import com.example.weathervoyager.data.remote.dto.WeatherDto
import com.example.weathervoyager.domain.weather.DailyForecast
import com.example.weathervoyager.domain.weather.WeatherInfo

/**
 * Расширение для разделения DTO и доменной сущности.
 * Превращает данные из API в чистую доменную модель WeatherInfo.
 */
fun WeatherDto.toDomain(): WeatherInfo {
    val dailyForecasts = daily.time.mapIndexed { index, time ->
        DailyForecast(
            time = time,
            // If some data is missing, we take 0.0 as a fallback (can be handled more flexibly)
            maxTemp = daily.maxTemp.getOrNull(index) ?: 0.0,
            minTemp = daily.minTemp.getOrNull(index) ?: 0.0,
            weatherCode = daily.weatherCode.getOrNull(index) ?: 0
        )
    }
    
    return WeatherInfo(
        currentTemperature = currentWeather.temperature,
        relativeHumidity = currentWeather.relativeHumidity,
        isDay = currentWeather.isDay == 1,
        precipitation = currentWeather.precipitation,
        windSpeed = currentWeather.windSpeed,
        windDirection = currentWeather.windDirection,
        weatherCode = currentWeather.weatherCode,
        dailyForecast = dailyForecasts
    )
}
