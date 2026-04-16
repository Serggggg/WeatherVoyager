package com.example.weathervoyager.presentation.weather

import com.example.weathervoyager.core.mvi.UiEffect
import com.example.weathervoyager.core.mvi.UiEvent
import com.example.weathervoyager.core.mvi.UiState
import com.example.weathervoyager.domain.weather.WeatherInfo

sealed interface WeatherState : UiState {
    data object Idle : WeatherState
    data object Loading : WeatherState
    data class Success(
        val weather: WeatherInfo, 
        val locationName: String,
        val lat: Double,
        val lon: Double
    ) : WeatherState
    data class Error(val message: String) : WeatherState
}

sealed interface WeatherEvent : UiEvent {
    data object LoadWeather : WeatherEvent
    data class SearchQueryChanged(val query: String) : WeatherEvent
    data class LocationSelected(val lat: Double, val lon: Double, val name: String) : WeatherEvent
    data object ResetToDefaultMode : WeatherEvent
    data object RefreshWeather : WeatherEvent
}

sealed interface WeatherEffect : UiEffect {
    data class ShowToast(val message: String) : WeatherEffect
}
