package com.example.weathervoyager.presentation.forecast

import com.example.weathervoyager.core.mvi.UiEffect
import com.example.weathervoyager.core.mvi.UiEvent
import com.example.weathervoyager.core.mvi.UiState
import com.example.weathervoyager.domain.weather.DailyForecast

sealed interface ForecastState : UiState {
    data object Idle : ForecastState
    data object Loading : ForecastState
    data class Success(val forecastList: List<DailyForecast>) : ForecastState
    data class Error(val message: String) : ForecastState
}

sealed interface ForecastEvent : UiEvent {
    data class LoadForecast(val lat: Double, val lon: Double) : ForecastEvent
}

sealed interface ForecastEffect : UiEffect {
    data class ShowToast(val message: String) : ForecastEffect
}
