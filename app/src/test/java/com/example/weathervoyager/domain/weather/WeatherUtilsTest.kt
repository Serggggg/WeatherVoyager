package com.example.weathervoyager.domain.weather

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.WbSunny
import com.example.weathervoyager.R
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherUtilsTest {

    @Test
    fun getWindDirectionText_returnsCorrectStringRes() {
        assertEquals(R.string.wind_dir_n, WeatherUtils.getWindDirectionText(0))
        assertEquals(R.string.wind_dir_ne, WeatherUtils.getWindDirectionText(45))
        assertEquals(R.string.wind_dir_e, WeatherUtils.getWindDirectionText(90))
        assertEquals(R.string.wind_dir_se, WeatherUtils.getWindDirectionText(135))
        assertEquals(R.string.wind_dir_s, WeatherUtils.getWindDirectionText(180))
        assertEquals(R.string.wind_dir_sw, WeatherUtils.getWindDirectionText(225))
        assertEquals(R.string.wind_dir_w, WeatherUtils.getWindDirectionText(270))
        assertEquals(R.string.wind_dir_nw, WeatherUtils.getWindDirectionText(315))
    }

    @Test
    fun getWeatherDescriptionAndIcon_returnsCorrectPair() {
        val clearDay = WeatherUtils.getWeatherDescriptionAndIcon(0, isDay = true)
        assertEquals(R.string.weather_clear, clearDay.first)
        assertEquals(Icons.Outlined.WbSunny, clearDay.second)

        val clearNight = WeatherUtils.getWeatherDescriptionAndIcon(0, isDay = false)
        assertEquals(R.string.weather_clear, clearNight.first)
        assertEquals(Icons.Outlined.Nightlight, clearNight.second)
    }
}
