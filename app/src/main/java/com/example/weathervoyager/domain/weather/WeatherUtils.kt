package com.example.weathervoyager.domain.weather

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.example.weathervoyager.R

object WeatherUtils {

    fun getWindDirectionText(degrees: Int): Int {
        val index = ((degrees + 22.5) / 45).toInt() % 8
        return when (index) {
            0 -> R.string.wind_dir_n
            1 -> R.string.wind_dir_ne
            2 -> R.string.wind_dir_e
            3 -> R.string.wind_dir_se
            4 -> R.string.wind_dir_s
            5 -> R.string.wind_dir_sw
            6 -> R.string.wind_dir_w
            else -> R.string.wind_dir_nw
        }
    }

    fun getWeatherDescriptionAndIcon(code: Int, isDay: Boolean): Pair<Int, ImageVector> {
        return when (code) {
            0 -> Pair(R.string.weather_clear, if (isDay) Icons.Outlined.WbSunny else Icons.Outlined.Nightlight)
            1, 2, 3 -> Pair(R.string.weather_cloudy, Icons.Outlined.Cloud)
            45, 48 -> Pair(R.string.weather_fog, Icons.Outlined.Menu) // DensityLarge/Fog replacement
            51, 53, 55, 56, 57 -> Pair(R.string.weather_drizzle, Icons.Outlined.WaterDrop)
            61, 63, 65, 66, 67 -> Pair(R.string.weather_rain, Icons.Outlined.Umbrella)
            71, 73, 75, 77, 85, 86 -> Pair(R.string.weather_snow, Icons.Outlined.AcUnit)
            80, 81, 82 -> Pair(R.string.weather_rain, Icons.Outlined.Umbrella)
            95, 96, 99 -> Pair(R.string.weather_thunderstorm, Icons.Outlined.FlashOn)
            else -> Pair(R.string.weather_clear, if (isDay) Icons.Outlined.WbSunny else Icons.Outlined.Nightlight)
        }
    }
}
