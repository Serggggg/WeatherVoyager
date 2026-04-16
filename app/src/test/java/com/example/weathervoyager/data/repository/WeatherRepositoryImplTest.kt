package com.example.weathervoyager.data.repository

import com.example.weathervoyager.data.remote.WeatherApi
import com.example.weathervoyager.data.remote.dto.CurrentWeatherDto
import com.example.weathervoyager.data.remote.dto.DailyWeatherDto
import com.example.weathervoyager.data.remote.dto.WeatherDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class WeatherRepositoryImplTest {

    private lateinit var api: WeatherApi
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = WeatherRepositoryImpl(api)
    }

    @Test
    fun getWeatherData_success_returnsDomainModel() = runTest {
        val mockWeatherDto = WeatherDto(
            currentWeather = CurrentWeatherDto(
                temperature = 25.0,
                windSpeed = 5.0,
                weatherCode = 0,
                relativeHumidity = 50.0,
                isDay = 1,
                precipitation = 0.0,
                windDirection = 180
            ),
            daily = DailyWeatherDto(
                time = listOf("2026-04-10"),
                maxTemp = listOf(28.0),
                minTemp = listOf(15.0),
                weatherCode = listOf(0)
            )
        )

        coEvery { api.getWeatherData(any(), any(), any(), any(), any()) } returns mockWeatherDto

        val result = repository.getWeatherData(51.5, -0.1)

        assertTrue(result.isSuccess)
    }

    @Test
    fun getWeatherData_apiThrowsException_returnsFailure() = runTest {
        coEvery { api.getWeatherData(any(), any(), any(), any(), any()) } throws IOException("Network Error")

        val result = repository.getWeatherData(51.5, -0.1)

        assertTrue(result.isFailure)
    }
}
