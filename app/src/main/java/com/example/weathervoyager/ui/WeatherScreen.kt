package com.example.weathervoyager.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.weathervoyager.R
import com.example.weathervoyager.presentation.weather.WeatherEffect
import com.example.weathervoyager.presentation.weather.WeatherEvent
import com.example.weathervoyager.presentation.weather.WeatherState
import com.example.weathervoyager.presentation.weather.WeatherViewModel
import com.example.weathervoyager.domain.weather.WeatherUtils
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.example.weathervoyager.domain.weather.WeatherInfo
import com.example.weathervoyager.domain.location.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onNavigateToForecast: (String, Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isCustomMode by viewModel.isCustomMode.collectAsState()

    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED
        )
    }

    var searchActive by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            viewModel.handleEvent(WeatherEvent.LoadWeather)
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            viewModel.handleEvent(WeatherEvent.LoadWeather)
        } else {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is WeatherEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(state) {
        if (state !is WeatherState.Loading) {
            isRefreshing = false
        }
    }

    WeatherScreenContent(
        state = state,
        searchQuery = query,
        searchResults = results,
        isCustomMode = isCustomMode,
        searchActive = searchActive,
        hasLocationPermission = hasLocationPermission,
        onQueryChange = { viewModel.handleEvent(WeatherEvent.SearchQueryChanged(it)) },
        onSearchActiveChange = { searchActive = it },
        onLocationSelected = { result ->
            searchActive = false
            viewModel.handleEvent(
                WeatherEvent.LocationSelected(
                    lat = result.lat,
                    lon = result.lon,
                    name = "${result.name}, ${result.country}"
                )
            )
        },
        onResetMode = { viewModel.handleEvent(WeatherEvent.ResetToDefaultMode) },
        onRequestPermission = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        },
        onRetryLoad = {
            isRefreshing = true
            viewModel.handleEvent(WeatherEvent.RefreshWeather)
        },
        onNavigateToForecast = onNavigateToForecast,
        isRefreshing = isRefreshing,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreenContent(
    state: WeatherState,
    searchQuery: String,
    searchResults: List<SearchResult>,
    isCustomMode: Boolean,
    searchActive: Boolean,
    hasLocationPermission: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onLocationSelected: (SearchResult) -> Unit,
    onResetMode: () -> Unit,
    onRequestPermission: () -> Unit,
    onRetryLoad: () -> Unit,
    onNavigateToForecast: (String, Double, Double) -> Unit,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Background content
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRetryLoad,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!hasLocationPermission && !isCustomMode) {
                PermissionDeniedView(onRetry = onRequestPermission)
            } else {
                when (val currentState = state) {
                    is WeatherState.Idle, is WeatherState.Loading -> {
                        if (!isRefreshing) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    is WeatherState.Success -> {
                        WeatherCard(
                            state = currentState,
                            onClick = {
                                onNavigateToForecast(
                                    currentState.locationName,
                                    currentState.lat,
                                    currentState.lon
                                )
                            }
                        )
                    }

                    is WeatherState.Error -> {
                        ErrorView(
                            message = currentState.message,
                            onRetry = onRetryLoad
                        )
                    }
                }
            }
        }
        }

        // Search area
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            DockedSearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = onQueryChange,
                        enabled = state !is WeatherState.Loading && state !is WeatherState.Idle,
                        onSearch = { onSearchActiveChange(false) },
                        expanded = searchActive,
                        onExpandedChange = onSearchActiveChange,
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.content_desc_search)
                            )
                        },
                        trailingIcon = {
                            if (searchActive) {
                                IconButton(onClick = {
                                    if (searchQuery.isNotEmpty()) {
                                        onQueryChange("")
                                    } else {
                                        onSearchActiveChange(false)
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.content_desc_clear)
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = searchActive,
                onExpandedChange = onSearchActiveChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn {
                    items(searchResults) { result ->
                        ListItem(
                            headlineContent = { Text(result.name) },
                            supportingContent = { Text(result.country) },
                            modifier = Modifier.clickable {
                                onLocationSelected(result)
                            }
                        )
                    }
                }
            }
        }

        // Return to GPS button
        if (isCustomMode) {
            FloatingActionButton(
                onClick = onResetMode,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.content_desc_my_loc))
            }
        }
    }
}

@Composable
fun WeatherCard(state: WeatherState.Success, onClick: () -> Unit) {
    val (descRes, icon) = WeatherUtils.getWeatherDescriptionAndIcon(state.weather.weatherCode, state.weather.isDay)
    val todayForecast = state.weather.dailyForecast.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Top padding is necessary so the SearchBar doesn't overlap the info
            .padding(top = 100.dp, start = 32.dp, end = 32.dp, bottom = 32.dp)
            .clip(RoundedCornerShape(32.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.locationName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${state.weather.currentTemperature}°C",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 80.sp
                )
            )
            if (todayForecast != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${
                        stringResource(
                            R.string.temp_min,
                            todayForecast.minTemp
                        )
                    } | ${stringResource(R.string.temp_max, todayForecast.maxTemp)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Air, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${stringResource(WeatherUtils.getWindDirectionText(state.weather.windDirection))} ${
                            stringResource(
                                R.string.wind_speed_format,
                                state.weather.windSpeed
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.humidity_format, state.weather.relativeHumidity),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Umbrella, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.precip_format, state.weather.precipitation),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
        ) {
            Text(stringResource(R.string.retry_button), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun PermissionDeniedView(onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = stringResource(R.string.permission_rationale),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
        ) {
            Text(stringResource(R.string.grant_permission_button), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    val mockWeather = WeatherInfo(
        currentTemperature = 22.5,
        relativeHumidity = 45.0,
        isDay = true,
        precipitation = 0.0,
        windSpeed = 3.2,
        windDirection = 45,
        weatherCode = 1,
        dailyForecast = emptyList()
    )
    WeatherScreenContent(
        state = WeatherState.Success(mockWeather, "Лондон", 51.5, -0.1),
        searchQuery = "",
        searchResults = emptyList(),
        isCustomMode = false,
        searchActive = false,
        hasLocationPermission = true,
        onQueryChange = {},
        onSearchActiveChange = {},
        onLocationSelected = {},
        onResetMode = {},
        onRequestPermission = {},
        onRetryLoad = {},
        onNavigateToForecast = { _, _, _ -> },
        isRefreshing = false
    )
}
