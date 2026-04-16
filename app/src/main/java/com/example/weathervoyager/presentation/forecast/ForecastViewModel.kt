package com.example.weathervoyager.presentation.forecast

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.weathervoyager.R
import com.example.weathervoyager.core.mvi.BaseViewModel
import com.example.weathervoyager.domain.weather.NetworkUtils
import com.example.weathervoyager.domain.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel<ForecastState, ForecastEvent, ForecastEffect>(ForecastState.Idle) {

    override fun handleEvent(event: ForecastEvent) {
        when (event) {
            is ForecastEvent.LoadForecast -> loadForecast(event.lat, event.lon)
        }
    }

    private fun loadForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            if (NetworkUtils.isInternetAvailable(context).not()) {
                setState { ForecastState.Error(context.getString(R.string.error_no_internet)) }
                return@launch
            }
            setState { ForecastState.Loading }
            weatherRepository.getWeatherData(lat, lon)
                .onSuccess { weatherInfo ->
                    setState { ForecastState.Success(forecastList = weatherInfo.dailyForecast) }
                }
                .onFailure { error ->
                    setState {
                        ForecastState.Error(
                            message = error.message ?: context.getString(R.string.error_network_unknown)
                        )
                    }
                }
        }
    }
}
