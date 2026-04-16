package com.example.weathervoyager.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.weathervoyager.domain.weather.DailyForecast
import com.example.weathervoyager.presentation.forecast.ForecastEvent
import com.example.weathervoyager.presentation.forecast.ForecastState
import com.example.weathervoyager.presentation.forecast.ForecastViewModel
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import com.example.weathervoyager.R
import com.example.weathervoyager.domain.weather.WeatherUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun ForecastScreen(
    locationName: String,
    lat: Double,
    lon: Double,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.handleEvent(ForecastEvent.LoadForecast(lat, lon))
    }

    LaunchedEffect(state) {
        if (state !is ForecastState.Loading) {
            isRefreshing = false
        }
    }

    ForecastScreenContent(
        state = state,
        locationName = locationName,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.handleEvent(ForecastEvent.LoadForecast(lat, lon))
        },
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreenContent(
    state: ForecastState,
    locationName: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ForecastHeader(locationName = locationName, onBack = onBack)

            // Content
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val currentState = state) {
                        is ForecastState.Idle, is ForecastState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isRefreshing) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        is ForecastState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentState.message,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        }

                        is ForecastState.Success -> {
                            val configuration = LocalConfiguration.current
                            val isTabletLandscape =
                                configuration.screenWidthDp >= 600 && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                            if (isTabletLandscape) {
                                TabletLandscapeForecastLayout(forecastList = currentState.forecastList)
                            } else {
                                StandardForecastLayout(forecastList = currentState.forecastList)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StandardForecastLayout(forecastList: List<DailyForecast>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(forecastList) { daily ->
            DailyForecastCard(daily)
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun TabletLandscapeForecastLayout(forecastList: List<DailyForecast>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (forecastList.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                DailyForecastCard(forecastList.first())
            }
            items(forecastList.drop(1)) { daily ->
                DailyForecastCard(daily)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ForecastHeader(locationName: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = stringResource(R.string.forecast_for, locationName),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun DailyForecastCard(forecast: DailyForecast) {
    val date = try {
        val parsedDate = LocalDate.parse(forecast.time)
        parsedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
    } catch (e: Exception) {
        forecast.time
    }
    val (descRes, icon) = WeatherUtils.getWeatherDescriptionAndIcon(forecast.weatherCode, isDay = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.8f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(descRes),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1.2f)
            ) {
                Text(
                    text = stringResource(R.string.temp_min, forecast.minTemp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
                Text(
                    text = stringResource(R.string.temp_max, forecast.maxTemp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForecastScreenPreview() {
    val mockList = listOf(
        DailyForecast("2026-04-10", 25.0, 15.0, 1),
        DailyForecast("2026-04-11", 23.0, 14.0, 2),
        DailyForecast("2026-04-12", 20.0, 10.0, 61)
    )
    ForecastScreenContent(
        state = ForecastState.Success(mockList),
        locationName = "Лондон",
        isRefreshing = false,
        onRefresh = {},
        onBack = {}
    )
}

@Preview(
    name = "Tablet Landscape Preview",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,orientation=landscape"
)
@Composable
fun ForecastScreenTabletLandscapePreview() {
    val mockList = listOf(
        DailyForecast("2026-04-10", 25.0, 15.0, 1),
        DailyForecast("2026-04-11", 23.0, 14.0, 2),
        DailyForecast("2026-04-12", 20.0, 10.0, 3),
        DailyForecast("2026-04-13", 22.0, 12.0, 45),
        DailyForecast("2026-04-14", 19.0, 11.0, 61),
        DailyForecast("2026-04-15", 18.0, 9.0, 80),
        DailyForecast("2026-04-16", 21.0, 13.0, 1)
    )

    MaterialTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ForecastScreenContent(
                state = ForecastState.Success(mockList),
                locationName = "Лондон (Tablet Mode)",
                isRefreshing = false,
                onRefresh = {},
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DailyForecastCardPreview() {
    val mockForecast = DailyForecast("2026-04-10", 25.0, 15.0, 1)
    DailyForecastCard(forecast = mockForecast)
}
