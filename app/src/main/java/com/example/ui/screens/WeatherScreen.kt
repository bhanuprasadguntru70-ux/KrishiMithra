package com.example.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AgriViewModel
import com.example.ui.util.FarmerTranslations
import com.example.ui.util.PortalLanguageSelector
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.absoluteValue

// --- WEATHER DATA MODELS ---
data class WeatherInfo(
    val locationName: String,
    val district: String,
    val state: String,
    val temperature: Double,
    val feelsLike: Double,
    val condition: String, // Sunny, Cloudy, Rain, Thunderstorm, Fog, Windy, Night
    val rainProbability: Int,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: String,
    val uvIndex: Int,
    val airPressure: Int,
    val visibility: Double,
    val sunrise: String,
    val sunset: String,
    val cloudCoverage: Int,
    val aqi: Int?,
    val lastUpdated: String,
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>,
    val farmingAdvisory: List<String>
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val condition: String,
    val rainChance: Int
)

data class DailyForecast(
    val day: String,
    val condition: String,
    val minTemp: Double,
    val maxTemp: Double,
    val rainChance: Int
)

// --- SUGGESTED POPULAR LOCATIONS ---
val SUGGESTIONS_LIST = listOf(
    "Nuzvid, Eluru, Andhra Pradesh",
    "Vijayawada, NTR, Andhra Pradesh",
    "Hyderabad, Rangareddy, Telangana",
    "Warangal, Hanamkonda, Telangana",
    "Khammam, Khammam, Telangana",
    "Guntur, Guntur, Andhra Pradesh",
    "Visakhapatnam, Visakhapatnam, Andhra Pradesh",
    "Eluru, Eluru, Andhra Pradesh",
    "Anantapur, Anantapur, Andhra Pradesh",
    "Madanapalle, Chittoor, Andhra Pradesh",
    "Amritsar, Amritsar, Punjab",
    "Lucknow, Lucknow, Uttar Pradesh",
    "Delhi, NCR, Delhi",
    "Mumbai, Mumbai City, Maharashtra",
    "Bengaluru, Bengaluru Urban, Karnataka",
    "Karnal, Karnal, Haryana",
    "Nashik, Nashik, Maharashtra",
    "Coimbatore, Coimbatore, Tamil Nadu"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit,
    onNotificationSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // User's default location setup
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val defaultLoc = remember(currentUser) {
        val dist = currentUser?.district ?: "Nuzvid"
        val state = currentUser?.state ?: "Andhra Pradesh"
        "$dist, $state"
    }

    // SharedPreferences for Persistence
    val prefs = remember { context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE) }
    
    // UI state
    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var weatherInfo by remember { mutableStateOf<WeatherInfo?>(null) }
    
    // Favorites & Recents in State
    var favorites by remember { mutableStateOf(loadFavorites(prefs)) }
    var recentSearches by remember { mutableStateOf(loadRecentSearches(prefs)) }

    // Floating Refresh rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )

    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            getCurrentLocation(context) { lat, lon ->
                viewModel.fetchWeatherForGps(lat, lon, appLanguage)
            }
        } else {
            android.widget.Toast.makeText(context, "Location permission denied. You can search manually.", android.widget.Toast.LENGTH_LONG).show()
            viewModel.fetchWeather(defaultLoc, appLanguage)
        }
    }

    LaunchedEffect(weatherState) {
        when (val state = weatherState) {
            is com.example.ui.WeatherState.Loading -> {
                loading = true
                errorMsg = null
            }
            is com.example.ui.WeatherState.Success -> {
                weatherInfo = state.weatherInfo
                loading = false
                isRefreshing = false
                errorMsg = null
            }
            is com.example.ui.WeatherState.Error -> {
                errorMsg = state.message
                loading = false
                isRefreshing = false
            }
        }
    }

    // Execute Search Query
    val onSearchExecute: (String) -> Unit = { query ->
        if (query.isNotBlank()) {
            keyboardController?.hide()
            searchFocused = false
            searchQuery = query
            isRefreshing = true
            errorMsg = null
            
            // Save to recents
            saveRecentSearch(prefs, query)
            recentSearches = loadRecentSearches(prefs)

            viewModel.fetchWeather(query, lang = appLanguage, forceRefresh = true)
        }
    }

    // Toggle favorite location
    val onToggleFav: (String) -> Unit = { loc ->
        val currentFavs = favorites.toMutableSet()
        if (currentFavs.contains(loc)) {
            currentFavs.remove(loc)
        } else {
            currentFavs.add(loc)
        }
        favorites = currentFavs
        saveFavorites(prefs, currentFavs)
    }

    // Initial load: Request location permission or search default location
    LaunchedEffect(Unit) {
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getCurrentLocation(context) { lat, lon ->
                viewModel.fetchWeatherForGps(lat, lon, appLanguage)
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Background clean KrishiMithra layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FBF8))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Top Bar - KrishiMithra Header Green
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B5E20))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("weather_back_button")) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = FarmerTranslations.get("weather_forecast", appLanguage),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Weather Forecast & Farmer Alerts",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    PortalLanguageSelector(
                        currentLanguage = appLanguage,
                        onLanguageSelected = { viewModel.setAppLanguage(it) }
                    )

                    // Notification Settings Button
                    IconButton(
                        onClick = onNotificationSettingsClick,
                        modifier = Modifier.testTag("weather_notification_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notification Settings",
                            tint = Color(0xFFFFD54F)
                        )
                    }

                    // Animated refresh icon
                    IconButton(
                        onClick = {
                            weatherInfo?.let { onSearchExecute(it.locationName) } ?: onSearchExecute(defaultLoc)
                        },
                        modifier = Modifier.testTag("weather_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White,
                            modifier = if (isRefreshing) Modifier.rotate(refreshRotation) else Modifier
                        )
                    }
                }
            }

            // MAIN INTERACTIVE SCROLLABLE SPACE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SEARCH AND SEARCH CONTEXT CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    searchFocused = true
                                },
                                label = { Text("Search Village, Mandal, City...", color = Color(0xFF616161)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                permissionLauncher.launch(
                                                    arrayOf(
                                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MyLocation,
                                                contentDescription = "My Location",
                                                tint = Color(0xFF1B5E20)
                                            )
                                        }
                                        if (searchQuery.isNotBlank()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    tint = Color.Gray
                                                )
                                            }
                                        }
                                        IconButton(onClick = { onSearchExecute(searchQuery) }) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search",
                                                tint = Color(0xFF1B5E20)
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF212121),
                                    unfocusedTextColor = Color(0xFF212121),
                                    focusedBorderColor = Color(0xFF1B5E20),
                                    unfocusedBorderColor = Color(0xFFC8E6C9),
                                    focusedLabelColor = Color(0xFF1B5E20)
                                )
                            )

                            // SUGGESTIONS & RECENT SEARCHES PANEL
                            AnimatedVisibility(
                                visible = searchFocused,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (recentSearches.isNotEmpty()) {
                                        Text(
                                            text = "Recent Searches",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            recentSearches.forEach { recent ->
                                                SuggestionChip(
                                                    onClick = {
                                                        searchQuery = recent
                                                        onSearchExecute(recent)
                                                    },
                                                    label = { Text(recent, color = Color(0xFF1B5E20)) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = Color(0xFFE8F5E9)
                                                    ),
                                                    border = null
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Popular Locations",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                    
                                    val filteredSuggestions = remember(searchQuery) {
                                        if (searchQuery.isBlank()) {
                                            SUGGESTIONS_LIST.take(6)
                                        } else {
                                            SUGGESTIONS_LIST.filter {
                                                it.contains(searchQuery, ignoreCase = true)
                                            }.take(5)
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        filteredSuggestions.forEach { suggestion ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        searchQuery = suggestion
                                                        onSearchExecute(suggestion)
                                                    }
                                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1B5E20),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = suggestion,
                                                    color = Color(0xFF212121),
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // FAVORITES BAR
                            if (favorites.isNotEmpty()) {
                                HorizontalDivider(color = Color(0xFFE2EBE4))
                                Text(
                                    text = "⭐ Favorite Locations",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    favorites.forEach { fav ->
                                        InputChip(
                                            selected = true,
                                            onClick = {
                                                searchQuery = fav
                                                onSearchExecute(fav)
                                            },
                                            label = { Text(fav.substringBefore(","), color = Color(0xFF1B5E20)) },
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove Favorite",
                                                    tint = Color.Gray,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable { onToggleFav(fav) }
                                                )
                                            },
                                            colors = InputChipDefaults.inputChipColors(
                                                selectedContainerColor = Color(0xFFE8F5E9)
                                            ),
                                            border = null
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // MAIN CONTENT STATES (LOADING / ERROR / DATA SUCCESS)
                    if (loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFF1B5E20))
                                Text(
                                    text = "Analyzing atmospheric forecasts for selected location...",
                                    color = Color(0xFF616161),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (errorMsg != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Error",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = errorMsg ?: "Connection Error",
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Button(
                                    onClick = { onSearchExecute(searchQuery.ifBlank { defaultLoc }) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    } else {
                        weatherInfo?.let { weather ->
                            // HERO WEATHER OVERVIEW CARD
                            val isFav = favorites.contains(weather.locationName)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { onToggleFav(weather.locationName) }) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFav) Color(0xFFD32F2F) else Color.Gray
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = weather.locationName,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1B5E20)
                                            )
                                            if (weather.district.isNotBlank() && weather.state.isNotBlank()) {
                                                Text(
                                                    text = "${weather.district}, ${weather.state}",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF424242)
                                                )
                                            }
                                        }

                                        IconButton(onClick = { onSearchExecute(weather.locationName) }) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Refresh Data",
                                                tint = Color(0xFF1B5E20)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Display Big Condition Icon
                                    val iconData = getWeatherIconAndColor(weather.condition)
                                    Icon(
                                        imageVector = iconData.icon,
                                        contentDescription = weather.condition,
                                        tint = iconData.tint,
                                        modifier = Modifier.size(72.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "${weather.temperature.toInt()}°C",
                                        fontSize = 52.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1B5E20)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${weather.condition} • Feels Like ${weather.feelsLike.toInt()}°C",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Last Updated: ${weather.lastUpdated} • Forecast Data",
                                        fontSize = 11.sp,
                                        color = Color(0xFF616161)
                                    )
                                }
                            }

                            // FARMING ADVISORIES CARD (CRITICAL AGRICULTURAL UTILITY)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Agri Advice",
                                            tint = Color(0xFF1B5E20),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Agricultural Weather Advisory",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        weather.farmingAdvisory.forEach { advice ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text(
                                                    text = "•",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1B5E20),
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text(
                                                    text = advice,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF212121),
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // SMART IRRIGATION PLANNING MODULE
                            SmartIrrigationPlannerCard(weather = weather)

                            // HOURLY FORECAST SCROLL
                            Text(
                                text = "Hourly Outlook",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1B5E20)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                weather.hourlyForecast.forEach { hour ->
                                    val hrIconData = getWeatherIconAndColor(hour.condition)
                                    Card(
                                        modifier = Modifier.width(84.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE2EBE4))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = hour.time,
                                                fontSize = 11.sp,
                                                color = Color(0xFF616161)
                                            )
                                            Icon(
                                                imageVector = hrIconData.icon,
                                                contentDescription = hour.condition,
                                                tint = hrIconData.tint,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "${hour.temperature.toInt()}°",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B5E20)
                                            )
                                            if (hour.rainChance > 0) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.WaterDrop,
                                                        contentDescription = "Rain",
                                                        tint = Color(0xFF0288D1),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "${hour.rainChance}%",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF0288D1),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.height(10.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // DETAILED ATMOSPHERIC GRID
                            Text(
                                text = "Atmospheric Parameters",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1B5E20)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                val metrics = remember(weather) {
                                    listOf(
                                        WeatherMetricItem("Rain Chance", "${weather.rainProbability}%", Icons.Default.Water, "Precipitation Probability"),
                                        WeatherMetricItem("Humidity", "${weather.humidity}%", Icons.Default.WaterDrop, "Moisture index"),
                                        WeatherMetricItem("Wind Speed", "${weather.windSpeed} km/h", Icons.Default.Air, "Wind rate"),
                                        WeatherMetricItem("Wind Direction", weather.windDirection, Icons.Default.Navigation, "Wind origin"),
                                        WeatherMetricItem("UV Index", "${weather.uvIndex}", Icons.Default.WbSunny, "UV severity scale"),
                                        WeatherMetricItem("Air Pressure", "${weather.airPressure} hPa", Icons.Default.Speed, "Atmospheric pressure"),
                                        WeatherMetricItem("Visibility", "${weather.visibility} km", Icons.Default.RemoveRedEye, "Horizon clarity"),
                                        WeatherMetricItem("Sunrise", weather.sunrise, Icons.Default.LightMode, "Sunrise time"),
                                        WeatherMetricItem("Sunset", weather.sunset, Icons.Default.WbTwilight, "Sunset time"),
                                        WeatherMetricItem("Cloud Coverage", "${weather.cloudCoverage}%", Icons.Default.Cloud, "Sky cloud coverage"),
                                        WeatherMetricItem("Air Quality Index", weather.aqi?.toString() ?: "N/A", Icons.Default.Info, "AQI standard")
                                    )
                                }

                                for (i in metrics.indices step 2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        MetricWidget(metric = metrics[i], modifier = Modifier.weight(1f))
                                        if (i + 1 < metrics.size) {
                                            MetricWidget(metric = metrics[i + 1], modifier = Modifier.weight(1f))
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            // 7 DAY AGRI FORECAST
                            Text(
                                text = "7-Day Forecast",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1B5E20)
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 32.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2EBE4))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    weather.dailyForecast.forEach { day ->
                                        val dayIconData = getWeatherIconAndColor(day.condition)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = day.day,
                                                modifier = Modifier.width(90.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1B5E20)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = dayIconData.icon,
                                                    contentDescription = day.condition,
                                                    tint = dayIconData.tint,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = day.condition,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF424242)
                                                )
                                                if (day.rainChance > 0) {
                                                    Text(
                                                        text = " (${day.rainChance}%)",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF0288D1),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${day.maxTemp.toInt()}° / ${day.minTemp.toInt()}°",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B5E20),
                                                modifier = Modifier.width(70.dp),
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SMART IRRIGATION PLANNING COMPONENT ---
data class IrrigationDecision(
    val title: String,
    val description: String,
    val color: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartIrrigationPlannerCard(weather: WeatherInfo) {
    var selectedCrop by remember { mutableStateOf("Paddy (Rice)") }
    var selectedSoil by remember { mutableStateOf("Black Cotton Soil") }
    var selectedMethod by remember { mutableStateOf("Drip Irrigation") }

    var expandedCrop by remember { mutableStateOf(false) }
    var expandedSoil by remember { mutableStateOf(false) }
    var expandedMethod by remember { mutableStateOf(false) }

    val cropOptions = listOf("Paddy (Rice)", "Cotton", "Maize", "Tomato", "Chilli", "Sugarcane", "Groundnut")
    val soilOptions = listOf("Black Cotton Soil", "Red Sandy Loam", "Clay Soil", "Alluvial Soil")
    val methodOptions = listOf("Drip Irrigation", "Sprinkler Irrigation", "Surface / Flood Irrigation")

    val temp = weather.temperature
    val humidity = weather.humidity
    val wind = weather.windSpeed
    val rainProb = weather.rainProbability

    val et0 = remember(temp, humidity, wind) {
        val baseEt = 0.0023 * (temp + 17.8) * kotlin.math.sqrt(kotlin.math.max(temp * 0.4, 4.0)) * (1.0 + wind / 150.0)
        val humidityFactor = (100.0 - humidity) / 50.0
        (baseEt * humidityFactor).coerceIn(2.0, 9.5)
    }

    val kc = when (selectedCrop) {
        "Paddy (Rice)" -> 1.15
        "Sugarcane" -> 1.20
        "Tomato" -> 1.05
        "Chilli" -> 1.00
        "Cotton" -> 0.95
        "Maize" -> 0.90
        "Groundnut" -> 0.85
        else -> 1.00
    }

    val soilFactor = when (selectedSoil) {
        "Black Cotton Soil" -> 0.85
        "Clay Soil" -> 0.90
        "Alluvial Soil" -> 1.00
        "Red Sandy Loam" -> 1.20
        else -> 1.00
    }

    val methodEfficiency = when (selectedMethod) {
        "Drip Irrigation" -> 0.90
        "Sprinkler Irrigation" -> 0.75
        "Surface / Flood Irrigation" -> 0.55
        else -> 0.80
    }

    val litersPerAcre = remember(et0, kc, soilFactor, methodEfficiency) {
        val grossMm = (et0 * kc * soilFactor) / methodEfficiency
        (grossMm * 4046.86).toInt()
    }

    val durationText = remember(litersPerAcre, selectedMethod) {
        when (selectedMethod) {
            "Drip Irrigation" -> {
                val mins = (litersPerAcre / 300).coerceIn(25, 180)
                "${mins / 60}h ${mins % 60}m"
            }
            "Sprinkler Irrigation" -> {
                val mins = (litersPerAcre / 450).coerceIn(20, 150)
                "${mins / 60}h ${mins % 60}m"
            }
            else -> {
                val mins = (litersPerAcre / 800).coerceIn(35, 300)
                "${mins / 60}h ${mins % 60}m"
            }
        }
    }

    val decision = remember(rainProb, temp) {
        when {
            rainProb >= 50 -> IrrigationDecision(
                title = "HOLD IRRIGATION (Rain Chance: $rainProb%)",
                description = "High rainfall expected today. Pause irrigation to prevent waterlogging, save ~${String.format("%,d", litersPerAcre)}L water/acre, and protect crop roots.",
                color = Color(0xFFFF5252),
                icon = Icons.Default.Umbrella
            )
            rainProb >= 30 -> IrrigationDecision(
                title = "REDUCED WATERING RECOMMENDED",
                description = "Light rainfall possible ($rainProb%). Reduce standard watering runtime by 40% and check soil moisture.",
                color = Color(0xFFFFB74D),
                icon = Icons.Default.WaterDrop
            )
            temp >= 35 -> IrrigationDecision(
                title = "OPTIMAL DRIP WATERING (High Heat: ${temp.toInt()}°C)",
                description = "High evapotranspiration rate (${String.format(java.util.Locale.US, "%.1f", et0)} mm/day). Irrigate during early morning (6-8 AM) or sunset.",
                color = Color(0xFF81C784),
                icon = Icons.Default.Water
            )
            else -> IrrigationDecision(
                title = "STANDARD IRRIGATION SCHEDULE",
                description = "Weather is steady. Evapotranspiration rate is ${String.format(java.util.Locale.US, "%.1f", et0)} mm/day. Run normal irrigation cycle.",
                color = Color(0xFF64B5F6),
                icon = Icons.Default.CheckCircle
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_irrigation_planner_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
        border = BorderStroke(1.dp, Color(0xFF81D4FA))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF0288D1).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Water,
                            contentDescription = "Irrigation Planner",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Smart Irrigation Planner",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF01579B)
                        )
                        Text(
                            text = "Evapotranspiration & Water Requirements",
                            fontSize = 11.sp,
                            color = Color(0xFF0277BD)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0288D1).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "ET₀: ${String.format(java.util.Locale.US, "%.1f", et0)} mm/d",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF01579B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Real-time Decision Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(decision.color.copy(alpha = 0.15f))
                    .border(1.dp, decision.color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = decision.icon,
                        contentDescription = "Decision",
                        tint = decision.color,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = decision.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = decision.color
                        )
                        Text(
                            text = decision.description,
                            fontSize = 12.sp,
                            color = Color(0xFF212121),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Selectors: Crop, Soil, Irrigation Method
            Text(
                text = "Customize Farm Parameters",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Crop Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedCrop = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF01579B),
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF81D4FA))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCrop.substringBefore(" "),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = expandedCrop,
                        onDismissRequest = { expandedCrop = false }
                    ) {
                        cropOptions.forEach { crop ->
                            DropdownMenuItem(
                                text = { Text(crop, fontSize = 12.sp) },
                                onClick = {
                                    selectedCrop = crop
                                    expandedCrop = false
                                }
                            )
                        }
                    }
                }

                // Soil Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedSoil = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF01579B),
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF81D4FA))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSoil.substringBefore(" "),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = expandedSoil,
                        onDismissRequest = { expandedSoil = false }
                    ) {
                        soilOptions.forEach { soil ->
                            DropdownMenuItem(
                                text = { Text(soil, fontSize = 12.sp) },
                                onClick = {
                                    selectedSoil = soil
                                    expandedSoil = false
                                }
                            )
                        }
                    }
                }

                // Method Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedMethod = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF01579B),
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF81D4FA))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedMethod.substringBefore(" "),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = expandedMethod,
                        onDismissRequest = { expandedMethod = false }
                    ) {
                        methodOptions.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method, fontSize = 12.sp) },
                                onClick = {
                                    selectedMethod = method
                                    expandedMethod = false
                                }
                            )
                        }
                    }
                }
            }

            // Calculated Outputs Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Water Needed Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFB3E5FC))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Water Needed", fontSize = 10.sp, color = Color(0xFF616161))
                        }
                        Text(
                            text = "${String.format("%,d", litersPerAcre)} L",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF01579B)
                        )
                        Text(text = "per Acre / Day", fontSize = 9.sp, color = Color(0xFF757575))
                    }
                }

                // Duration Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFB3E5FC))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pump Duration", fontSize = 10.sp, color = Color(0xFF616161))
                        }
                        Text(
                            text = durationText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF01579B)
                        )
                        Text(text = "Target runtime", fontSize = 9.sp, color = Color(0xFF757575))
                    }
                }

                // Best Slot Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFB3E5FC))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Best Window", fontSize = 10.sp, color = Color(0xFF616161))
                        }
                        Text(
                            text = "6-8:30 AM",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF01579B)
                        )
                        Text(text = "Low evaporation", fontSize = 9.sp, color = Color(0xFF757575))
                    }
                }
            }
        }
    }
}

// --- WEATHER ICON PARSER ---
data class IconTheme(val icon: ImageVector, val tint: Color)

fun getWeatherIconAndColor(condition: String): IconTheme {
    return when (condition.trim().replaceFirstChar { it.uppercase() }) {
        "Sunny" -> IconTheme(Icons.Default.WbSunny, Color(0xFFF57F17))
        "Cloudy" -> IconTheme(Icons.Default.Cloud, Color(0xFF607D8B))
        "Rain" -> IconTheme(Icons.Default.Umbrella, Color(0xFF0288D1))
        "Thunderstorm" -> IconTheme(Icons.Default.Thunderstorm, Color(0xFF5E35B1))
        "Fog" -> IconTheme(Icons.Default.Dehaze, Color(0xFF78909C))
        "Windy" -> IconTheme(Icons.Default.Air, Color(0xFF00897B))
        "Night" -> IconTheme(Icons.Default.NightsStay, Color(0xFF3949AB))
        else -> IconTheme(Icons.Default.WbSunny, Color(0xFFF57F17))
    }
}

// --- DETAILED METRICS COMPONENT ---
data class WeatherMetricItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun MetricWidget(
    metric: WeatherMetricItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2EBE4))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = metric.title,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = metric.title,
                    fontSize = 11.sp,
                    color = Color(0xFF616161)
                )
            }
            Text(
                text = metric.value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(
                text = metric.description,
                fontSize = 10.sp,
                color = Color(0xFF757575),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- PERSISTENCE LOGIC WITH SHARED PREFERENCES ---
fun loadFavorites(prefs: SharedPreferences): Set<String> {
    return prefs.getStringSet("favorites_set", setOf("Nuzvid, Eluru, Andhra Pradesh")) ?: setOf("Nuzvid, Eluru, Andhra Pradesh")
}

fun saveFavorites(prefs: SharedPreferences, favorites: Set<String>) {
    prefs.edit().putStringSet("favorites_set", favorites).apply()
}

fun loadRecentSearches(prefs: SharedPreferences): List<String> {
    val searchesStr = prefs.getString("recent_searches_str", "") ?: ""
    if (searchesStr.isBlank()) return emptyList()
    return searchesStr.split("|").filter { it.isNotBlank() }
}

fun saveRecentSearch(prefs: SharedPreferences, query: String) {
    val searches = loadRecentSearches(prefs).toMutableList()
    searches.remove(query)
    searches.add(0, query)
    val trimmed = searches.take(5)
    prefs.edit().putString("recent_searches_str", trimmed.joinToString("|")).apply()
}

// --- WEATHER FALLBACK GENERATOR ---
fun generateFallbackWeather(query: String): WeatherInfo {
    val hash = query.hashCode().absoluteValue
    val tempBase = 28.0 + (hash % 8)
    val humidity = 55 + (hash % 35)
    val conditions = listOf("Sunny", "Cloudy", "Rain", "Thunderstorm", "Windy")
    val cond = conditions[hash % conditions.size]
    val rainProb = if (cond == "Rain" || cond == "Thunderstorm") 75 + (hash % 25) else hash % 35
    
    val parts = query.split(",").map { it.trim() }
    val loc = parts.getOrNull(0)?.replaceFirstChar { it.uppercase() } ?: query.replaceFirstChar { it.uppercase() }
    val dist = parts.getOrNull(1)?.replaceFirstChar { it.uppercase() } ?: "Krishna"
    val state = parts.getOrNull(2)?.replaceFirstChar { it.uppercase() } ?: "Andhra Pradesh"

    val days = listOf("Today", "Tomorrow", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday")
    
    val hourly = (0..7).map { i ->
        val hourText = when {
            i == 0 -> "Now"
            else -> {
                val h = (12 + i * 2) % 12
                val ampm = if ((12 + i * 2) % 24 >= 12) "PM" else "AM"
                "${if (h == 0) 12 else h} $ampm"
            }
        }
        HourlyForecast(
            time = hourText,
            temperature = tempBase - (i * 0.4) + (hash % 3 - 1),
            condition = conditions[(hash + i) % conditions.size],
            rainChance = (rainProb + i * 4).coerceIn(0, 100)
        )
    }

    val daily = (0..6).map { i ->
        DailyForecast(
            day = days[i],
            condition = conditions[(hash + i * 2) % conditions.size],
            minTemp = tempBase - 5 + (hash % 3),
            maxTemp = tempBase + 3 + (hash % 3),
            rainChance = (rainProb + i * 3).coerceIn(0, 100)
        )
    }

    val advisories = when (cond) {
        "Rain", "Thunderstorm" -> listOf(
            "🌧️ Spraying Alert: Avoid pesticide and chemical spraying to prevent wash-off.",
            "🌾 Crop Drying: Suspend grain sun-drying today and secure current harvested yield.",
            "💦 Irrigation Advice: No irrigation needed; soil profile is fully saturated.",
            "🌱 Soil Health: Check and clear drainage networks around field roots immediately."
        )
        "Sunny" -> listOf(
            "☀️ Irrigation Rule: Scheduled deep watering is recommended in morning/evening hours.",
            "🐛 Pest Check: Bright sun is perfect for crop disease and pest scouting.",
            "🌾 Harvesting: Excellent time for harvesting grains and natural sun dehydration.",
            "🌱 Fertilizers: High water uptake means perfect timing to apply crop nutrition."
        )
        else -> listOf(
            "⛅ Field Prep: Highly favorable weather for soil preparation and weed removal.",
            "💦 Watering: Check relative soil moisture at root-level before starting drip irrigators.",
            "🌿 Pest Alert: Humid cloudiness is inviting for fungus growth. Inspect crops."
        )
    }

    return WeatherInfo(
        locationName = loc,
        district = dist,
        state = state,
        temperature = tempBase,
        feelsLike = tempBase + 2,
        condition = cond,
        rainProbability = rainProb,
        humidity = humidity,
        windSpeed = 10.0 + (hash % 10),
        windDirection = listOf("ENE", "NNE", "WSW", "CALM", "SSE")[hash % 5],
        uvIndex = 2 + (hash % 10),
        airPressure = 1005 + (hash % 8),
        visibility = 6.0 + (hash % 4),
        sunrise = "05:42 AM",
        sunset = "06:44 PM",
        cloudCoverage = 15 + (hash % 80),
        aqi = 35 + (hash % 120),
        lastUpdated = "Just updated",
        hourlyForecast = hourly,
        dailyForecast = daily,
        farmingAdvisory = advisories
    )
}

// --- RAW JSON PARSER USING STANDARD ANDROID org.json ---
fun parseWeatherJson(jsonStr: String, query: String): WeatherInfo {
    var clean = jsonStr.trim()
    if (clean.startsWith("```")) {
        clean = clean.substringAfter("{").substringBeforeLast("}")
        clean = "{" + clean + "}"
    }
    
    val jsonObj = JSONObject(clean)
    
    val hourlyList = mutableListOf<HourlyForecast>()
    val hourlyArr = jsonObj.optJSONArray("hourlyForecast")
    if (hourlyArr != null) {
        for (i in 0 until hourlyArr.length()) {
            val item = hourlyArr.getJSONObject(i)
            hourlyList.add(
                HourlyForecast(
                    time = item.optString("time", ""),
                    temperature = item.optDouble("temperature", 30.0),
                    condition = item.optString("condition", "Cloudy"),
                    rainChance = item.optInt("rainChance", 0)
                )
            )
        }
    }
    
    val dailyList = mutableListOf<DailyForecast>()
    val dailyArr = jsonObj.optJSONArray("dailyForecast")
    if (dailyArr != null) {
        for (i in 0 until dailyArr.length()) {
            val item = dailyArr.getJSONObject(i)
            dailyList.add(
                DailyForecast(
                    day = item.optString("day", ""),
                    condition = item.optString("condition", "Cloudy"),
                    minTemp = item.optDouble("minTemp", 22.0),
                    maxTemp = item.optDouble("maxTemp", 32.0),
                    rainChance = item.optInt("rainChance", 0)
                )
            )
        }
    }
    
    val advisoryList = mutableListOf<String>()
    val advisoryArr = jsonObj.optJSONArray("farmingAdvisory")
    if (advisoryArr != null) {
        for (i in 0 until advisoryArr.length()) {
            advisoryList.add(advisoryArr.getString(i))
        }
    }
    
    return WeatherInfo(
        locationName = jsonObj.optString("locationName", query.substringBefore(",")),
        district = jsonObj.optString("district", ""),
        state = jsonObj.optString("state", ""),
        temperature = jsonObj.optDouble("temperature", 30.0),
        feelsLike = jsonObj.optDouble("feelsLike", 32.0),
        condition = jsonObj.optString("condition", "Cloudy"),
        rainProbability = jsonObj.optInt("rainProbability", 0),
        humidity = jsonObj.optInt("humidity", 60),
        windSpeed = jsonObj.optDouble("windSpeed", 10.0),
        windDirection = jsonObj.optString("windDirection", "CALM"),
        uvIndex = jsonObj.optInt("uvIndex", 5),
        airPressure = jsonObj.optInt("airPressure", 1010),
        visibility = jsonObj.optDouble("visibility", 10.0),
        sunrise = jsonObj.optString("sunrise", "05:45 AM"),
        sunset = jsonObj.optString("sunset", "06:45 PM"),
        cloudCoverage = jsonObj.optInt("cloudCoverage", 40),
        aqi = if (jsonObj.has("aqi") && !jsonObj.isNull("aqi")) jsonObj.getInt("aqi") else null,
        lastUpdated = jsonObj.optString("lastUpdated", "Just updated"),
        hourlyForecast = hourlyList,
        dailyForecast = dailyList,
        farmingAdvisory = advisoryList
    )
}

fun getCurrentLocation(context: Context, onLocationObtained: (Double, Double) -> Unit) {
    try {
        val fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient =
            com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    onLocationObtained(location.latitude, location.longitude)
                } else {
                    try {
                        fusedLocationClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                            null
                        ).addOnSuccessListener { currentLocation: android.location.Location? ->
                            if (currentLocation != null) {
                                onLocationObtained(currentLocation.latitude, currentLocation.longitude)
                            } else {
                                fallbackToLocationManager(context, onLocationObtained)
                            }
                        }.addOnFailureListener {
                            fallbackToLocationManager(context, onLocationObtained)
                        }
                    } catch (e: SecurityException) {
                        fallbackToLocationManager(context, onLocationObtained)
                    } catch (e: Exception) {
                        fallbackToLocationManager(context, onLocationObtained)
                    }
                }
            }
            .addOnFailureListener {
                fallbackToLocationManager(context, onLocationObtained)
            }
    } catch (e: SecurityException) {
        fallbackToLocationManager(context, onLocationObtained)
    } catch (e: Exception) {
        fallbackToLocationManager(context, onLocationObtained)
    }
}

private fun fallbackToLocationManager(context: Context, onLocationObtained: (Double, Double) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    try {
        val providers = locationManager.getProviders(true)
        var bestLocation: android.location.Location? = null
        for (provider in providers) {
            @Suppress("DEPRECATION")
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                bestLocation = l
            }
        }
        
        if (bestLocation != null) {
            onLocationObtained(bestLocation.latitude, bestLocation.longitude)
        } else {
            val provider = if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                android.location.LocationManager.GPS_PROVIDER
            } else if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                android.location.LocationManager.NETWORK_PROVIDER
            } else {
                null
            }
            
            if (provider != null) {
                @Suppress("DEPRECATION")
                locationManager.requestSingleUpdate(
                    provider,
                    object : android.location.LocationListener {
                        override fun onLocationChanged(location: android.location.Location) {
                            onLocationObtained(location.latitude, location.longitude)
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    },
                    null
                )
            } else {
                onLocationObtained(16.7844, 80.8491) // default to Nuzvid
            }
        }
    } catch (e: Exception) {
        onLocationObtained(16.7844, 80.8491)
    }
}
