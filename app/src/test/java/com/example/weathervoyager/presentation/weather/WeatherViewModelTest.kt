package com.example.weathervoyager.presentation.weather

import android.content.Context
import app.cash.turbine.test
import com.example.weathervoyager.domain.location.Location
import com.example.weathervoyager.domain.location.LocationTracker
import com.example.weathervoyager.domain.location.SearchResult
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var locationTracker: LocationTracker
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var context: Context
    private lateinit var viewModel: WeatherViewModel

    private val mockWeatherInfo = WeatherInfo(
        currentTemperature = 20.0,
        relativeHumidity = 50.0,
        isDay = true,
        precipitation = 0.0,
        windSpeed = 5.0,
        windDirection = 180,
        weatherCode = 0,
        dailyForecast = emptyList()
    )

    @Before
    fun setUp() {
        locationTracker = mockk()
        weatherRepository = mockk()
        context = mockk(relaxed = true)

        mockkObject(NetworkUtils)
        every { NetworkUtils.isInternetAvailable(any()) } returns true

        viewModel = WeatherViewModel(locationTracker, weatherRepository, context)
    }

    @After
    fun tearDown() {
        unmockkObject(NetworkUtils)
    }

    @Test
    fun loadWeather_withLocation_updatesToSuccess() = runTest {
        val mockLocation = Location(latitude = 51.5, longitude = -0.1)
        coEvery { locationTracker.getCurrentLocation() } returns mockLocation
        coEvery { weatherRepository.getWeatherData(any(), any()) } returns Result.success(mockWeatherInfo)
        
        viewModel.uiState.test {
            assertTrue(awaitItem() is WeatherState.Idle)
            viewModel.handleEvent(WeatherEvent.LoadWeather)

            assertTrue(awaitItem() is WeatherState.Loading)

            val successState = awaitItem() as WeatherState.Success
            assertEquals(51.5, successState.lat, 0.0)
            assertEquals(-0.1, successState.lon, 0.0)
            assertEquals(mockWeatherInfo, successState.weather)
        }
    }

    @Test
    fun loadWeather_noLocation_updatesToError() = runTest {
        coEvery { locationTracker.getCurrentLocation() } returns null
        
        viewModel.uiState.test {
            assertTrue(awaitItem() is WeatherState.Idle)
            viewModel.handleEvent(WeatherEvent.LoadWeather)

            assertTrue(awaitItem() is WeatherState.Error)
        }
    }

    @Test
    fun searchQueryChanged_shortQuery_clearsResults() = runTest {
        viewModel.handleEvent(WeatherEvent.SearchQueryChanged("A"))
        
        advanceTimeBy(600)
        
        val results = viewModel.searchResults.value
        assertTrue(results.isEmpty())
        assertEquals("A", viewModel.searchQuery.value)
    }

    @Test
    fun searchQueryChanged_validQuery_updatesResults() = runTest {
        val mockResults = listOf(SearchResult(1, "Paris", "France", 48.85, 2.35))
        coEvery { weatherRepository.searchLocation(any(), any()) } returns Result.success(mockResults)
        
        viewModel.handleEvent(WeatherEvent.SearchQueryChanged("Paris"))
        
        advanceTimeBy(600)
        
        val results = viewModel.searchResults.value
        assertEquals(1, results.size)
        assertEquals("Paris", results[0].name)
        assertEquals("Paris", viewModel.searchQuery.value)
    }

    @Test
    fun locationSelected_noInternet_updatesToError() = runTest {
        every { NetworkUtils.isInternetAvailable(any()) } returns false
        
        viewModel.uiState.test {
            assertTrue(awaitItem() is WeatherState.Idle)
            viewModel.handleEvent(WeatherEvent.LocationSelected(48.85, 2.35, "Paris"))
            
            assertTrue(awaitItem() is WeatherState.Error)
        }
    }

    @Test
    fun locationSelected_withInternet_updatesToSuccess() = runTest {
        coEvery { weatherRepository.getWeatherData(any(), any()) } returns Result.success(mockWeatherInfo)
        
        viewModel.uiState.test {
            assertTrue(awaitItem() is WeatherState.Idle)
            viewModel.handleEvent(WeatherEvent.LocationSelected(48.85, 2.35, "Paris"))
            
            val successState = awaitItem() as WeatherState.Success
            assertEquals("Paris", successState.locationName)
            assertEquals(48.85, successState.lat, 0.0)
            assertEquals(2.35, successState.lon, 0.0)
            assertEquals(mockWeatherInfo, successState.weather)
        }
        
        assertTrue(viewModel.isCustomMode.value)
    }

    @Test
    fun resetToDefaultMode_resetsState() = runTest {
        // Подготавливаем моки ДО вызова ивентов
        coEvery { weatherRepository.getWeatherData(any(), any()) } returns Result.success(mockWeatherInfo)
        val mockLocation = Location(latitude = 51.5, longitude = -0.1)
        coEvery { locationTracker.getCurrentLocation() } returns mockLocation
        
        // Сначала переходим в кастомный режим
        viewModel.handleEvent(WeatherEvent.LocationSelected(48.85, 2.35, "Paris"))
        assertTrue(viewModel.isCustomMode.value)
        
        // Затем сбрасываем стейт
        viewModel.handleEvent(WeatherEvent.ResetToDefaultMode)
        assertFalse(viewModel.isCustomMode.value)
    }
}
