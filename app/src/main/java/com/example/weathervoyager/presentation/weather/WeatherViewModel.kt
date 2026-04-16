package com.example.weathervoyager.presentation.weather

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.viewModelScope
import com.example.weathervoyager.core.mvi.BaseViewModel
import com.example.weathervoyager.R
import com.example.weathervoyager.domain.location.LocationTracker
import com.example.weathervoyager.domain.location.SearchResult
import com.example.weathervoyager.domain.weather.NetworkUtils
import com.example.weathervoyager.domain.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel<WeatherState, WeatherEvent, WeatherEffect>(WeatherState.Idle) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isCustomMode = MutableStateFlow(false)
    val isCustomMode = _isCustomMode.asStateFlow()

    private var searchJob: Job? = null

    override fun handleEvent(event: WeatherEvent) {
        when (event) {
            is WeatherEvent.LoadWeather -> {
                if (uiState.value is WeatherState.Success || _isCustomMode.value) return
                forceLoadWeather()
            }
            is WeatherEvent.RefreshWeather -> {
                if (_isCustomMode.value) {
                    val currentState = uiState.value as? WeatherState.Success
                    if (currentState != null) {
                        handleLocationSelected(currentState.lat, currentState.lon, currentState.locationName)
                    }
                } else {
                    forceLoadWeather()
                }
            }

            is WeatherEvent.SearchQueryChanged -> handleSearchQueryChanged(event.query)
            is WeatherEvent.LocationSelected -> handleLocationSelected(event.lat, event.lon, event.name)
            WeatherEvent.ResetToDefaultMode -> handleResetToDefaultMode()
        }
    }

    private fun handleSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            val language = Locale.getDefault().language
            weatherRepository.searchLocation(query, language)
                .onSuccess { results ->
                    _searchResults.value = results
                }
        }
    }

    private fun handleLocationSelected(lat: Double, lon: Double, name: String) {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isCustomMode.value = true

        viewModelScope.launch {
            if (NetworkUtils.isInternetAvailable(context).not()) {
                setState { WeatherState.Error(context.getString(R.string.error_no_internet)) }
                return@launch
            }
            setState { WeatherState.Loading }
            weatherRepository.getWeatherData(lat, lon)
                .onSuccess { weatherInfo ->
                    setState {
                        WeatherState.Success(
                            weather = weatherInfo,
                            locationName = name,
                            lat = lat,
                            lon = lon
                        )
                    }
                }
                .onFailure { error ->
                    setState { WeatherState.Error(error.message ?: context.getString(R.string.error_network_unknown)) }
                }
        }
    }

    private fun handleResetToDefaultMode() {
        _isCustomMode.value = false
        setState { WeatherState.Idle } // Reset state to avoid double invocation
        forceLoadWeather()
    }

    private fun forceLoadWeather() {

        viewModelScope.launch {
            setState { WeatherState.Loading }

            val location = locationTracker.getCurrentLocation()
            if (location == null) {
                setState { WeatherState.Error(context.getString(R.string.error_location_permission)) }
                return@launch
            }

            val locationName = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val city = address.locality ?: address.subAdminArea ?: ""
                        val country = address.countryName ?: ""
                        listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")
                            .takeIf { it.isNotBlank() } ?: context.getString(R.string.unknown_location)
                    } else {
                        context.getString(R.string.unknown_location)
                    }
                } catch (e: Exception) {
                    context.getString(R.string.unknown_location)
                }
            }

            weatherRepository.getWeatherData(location.latitude, location.longitude)
                .onSuccess { weatherInfo ->
                    setState {
                        WeatherState.Success(
                            weather = weatherInfo,
                            locationName = locationName,
                            lat = location.latitude,
                            lon = location.longitude
                        )
                    }
                }
                .onFailure { error ->
                    setState { WeatherState.Error(error.message ?: context.getString(R.string.error_network_unknown)) }
                }
        }
    }
}
