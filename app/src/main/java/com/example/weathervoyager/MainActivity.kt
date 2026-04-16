package com.example.weathervoyager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.weathervoyager.ui.theme.WeatherVoyagerTheme
import com.example.weathervoyager.ui.Forecast
import com.example.weathervoyager.ui.ForecastScreen
import com.example.weathervoyager.ui.Home
import com.example.weathervoyager.ui.WeatherScreen
import com.example.weathervoyager.ui.DoubleType
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.weathervoyager.domain.weather.WeatherInfo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherVoyagerTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Home,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<Home> {
                            WeatherScreen(
                                onNavigateToForecast = { locationName, lat, lon ->
                                    navController.navigate(Forecast(locationName, lat, lon))
                                }
                            )
                        }
                        composable<Forecast>(
                            typeMap = mapOf(kotlin.reflect.typeOf<Double>() to DoubleType)
                        ) { backStackEntry ->
                            val forecast = backStackEntry.toRoute<Forecast>()
                            ForecastScreen(
                                locationName = forecast.locationName,
                                lat = forecast.lat,
                                lon = forecast.lon,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}