package com.example.weathervoyager.presentation.forecast

import android.content.Context
import app.cash.turbine.test
import com.example.weathervoyager.domain.weather.NetworkUtils
import com.example.weathervoyager.domain.weather.WeatherInfo
import com.example.weathervoyager.domain.weather.WeatherRepository
import com.example.weathervoyager.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForecastViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var context: Context
    private lateinit var viewModel: ForecastViewModel

    private val mockWeatherInfo = WeatherInfo(
        currentTemperature = 15.0,
        relativeHumidity = 40.0,
        isDay = true,
        precipitation = 0.0,
        windSpeed = 3.0,
        windDirection = 90,
        weatherCode = 1,
        dailyForecast = emptyList() // The forecast state depends on this list
    )

    @Before
    fun setUp() {
        weatherRepository = mockk()
        context = mockk(relaxed = true)

        mockkObject(NetworkUtils)
        every { NetworkUtils.isInternetAvailable(any()) } returns true

        viewModel = ForecastViewModel(weatherRepository, context)
    }

    @After
    fun tearDown() {
        unmockkObject(NetworkUtils)
    }

    @Test
    fun loadForecast_noInternet_updatesToError() = runTest {
        // Мокаем зависимости ДО вызова ивентов
        every { NetworkUtils.isInternetAvailable(any()) } returns false
        every { context.getString(any()) } returns "No internet"

        viewModel.uiState.test {
            // Пропускаем (проверяем) изначальный статус Idle
            assertTrue(awaitItem() is ForecastState.Idle)
            
            // Триггерим стейт
            viewModel.handleEvent(ForecastEvent.LoadForecast(10.0, 20.0))
            
            // Ловим ФИНАЛЬНЫЙ результат (без вызовов проверрки промежуточного Loading стейта)
            val errorState = awaitItem() as ForecastState.Error
            assertTrue(errorState.message.isNotEmpty())
        }
    }

    @Test
    fun loadForecast_withInternet_success_updatesToSuccess() = runTest {
        coEvery { weatherRepository.getWeatherData(any(), any()) } returns Result.success(mockWeatherInfo)

        viewModel.uiState.test {
            assertTrue(awaitItem() is ForecastState.Idle)
            
            viewModel.handleEvent(ForecastEvent.LoadForecast(10.0, 20.0))
            
            val successState = awaitItem() as ForecastState.Success
            assertEquals(mockWeatherInfo.dailyForecast, successState.forecastList)
        }
    }

    @Test
    fun loadForecast_withInternet_failure_updatesToError() = runTest {
        coEvery { weatherRepository.getWeatherData(any(), any()) } returns Result.failure(Exception("API Error"))

        viewModel.uiState.test {
            assertTrue(awaitItem() is ForecastState.Idle)
            
            viewModel.handleEvent(ForecastEvent.LoadForecast(10.0, 20.0))
            
            val errorState = awaitItem() as ForecastState.Error
            assertTrue(errorState.message.contains("API Error"))
        }
    }
}
