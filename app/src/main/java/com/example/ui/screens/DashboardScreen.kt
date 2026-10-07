package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.MandiCropRate
import com.example.ui.theme.KrishiAccentOrange
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen
import com.example.ui.theme.KrishiTextDark
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: KrishiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val agriViewModel: com.example.ui.AgriViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val weatherState by agriViewModel.weatherState.collectAsStateWithLifecycle()
    val currentUser by agriViewModel.currentUser.collectAsState()
    var showWeatherSettings by remember { mutableStateOf(false) }
    var showAuthScreen by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var showPremiumPlans by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (agriViewModel.weatherState.value is com.example.ui.WeatherState.Loading) {
            agriViewModel.fetchWeather(uiState.selectedLocation)
        }
    }

    androidx.activity.compose.BackHandler(enabled = uiState.isWeatherDetailActive || showAuthScreen || showAdminPanel || showPremiumPlans) {
        if (showWeatherSettings) {
            showWeatherSettings = false
        } else if (showAuthScreen) {
            showAuthScreen = false
        } else if (showAdminPanel) {
            showAdminPanel = false
        } else if (showPremiumPlans) {
            showPremiumPlans = false
        } else {
            viewModel.toggleWeatherDetail(false)
        }
    }

    if (showAuthScreen) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (isRegistering) {
                RegisterScreen(
                    viewModel = agriViewModel,
                    onRegisterSuccess = { showAuthScreen = false },
                    onNavigateToLogin = { isRegistering = false }
                )
            } else {
                LoginScreen(
                    viewModel = agriViewModel,
                    onLoginSuccess = { showAuthScreen = false },
                    onNavigateToRegister = { isRegistering = true }
                )
            }
        }
    } else if (showAdminPanel) {
        AdminPanelScreen(
            viewModel = agriViewModel,
            onBack = { showAdminPanel = false }
        )
    } else if (showPremiumPlans) {
        SubscriptionScreen(
            viewModel = agriViewModel,
            onBack = { showPremiumPlans = false }
        )
    } else if (uiState.isWeatherDetailActive) {
        if (showWeatherSettings) {
            WeatherNotificationSettingsScreen(
                viewModel = agriViewModel,
                onBack = { showWeatherSettings = false }
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF7FBF8)
            ) {
                WeatherScreen(
                    viewModel = agriViewModel,
                    onBack = { viewModel.toggleWeatherDetail(false) },
                    onNotificationSettingsClick = { showWeatherSettings = true }
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBarSection(uiState, viewModel, agriViewModel)
            },
            bottomBar = {
                BottomNavBarSection(uiState, viewModel)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF7FBF8))
            ) {
                when (uiState.currentTab) {
                    0 -> HomeDashboardContent(uiState, viewModel, agriViewModel)
                    1 -> LiveMandiRatesTabScreen(uiState, viewModel)
                    2 -> SellCropTabScreen(uiState, viewModel)
                    3 -> MoneyBookTabScreen(uiState, viewModel)
                    4 -> ProfileScreen(
                        viewModel = agriViewModel,
                        onBack = { viewModel.selectTab(0) },
                        onAdminClick = { showAdminPanel = true },
                        onPremiumClick = { showPremiumPlans = true },
                        onLogout = { viewModel.selectTab(0) },
                        onOpenAuth = { showAuthScreen = true; isRegistering = false }
                    )
                    5 -> com.example.calculator.CalculatorPortalScreen(
                        mandiRates = uiState.mandiRates,
                        selectedLang = uiState.selectedLanguage,
                        savedCalculations = uiState.savedCalculations,
                        historyItems = uiState.calculationHistory,
                        onSaveCalculation = { viewModel.saveCalculation(it) },
                        onDeleteSaved = { viewModel.deleteSavedCalculation(it) },
                        activeToolId = uiState.activeCalculatorId,
                        onCloseActiveTool = { viewModel.closeCalculatorPortal() }
                    )
                }

                // Interactive Dialogs & Modals
                if (uiState.isLocationSelectorActive) {
                    LocationSelectorDialog(uiState, viewModel)
                }
                if (uiState.isVoiceAssistantActive) {
                    VoiceAssistantDialog(uiState, viewModel)
                }
                if (uiState.isNotificationsActive) {
                    NotificationsDialog(uiState, viewModel)
                }
                if (uiState.activeTool != null) {
                    CalculatorToolModal(uiState.activeTool!!, onDismiss = { viewModel.openTool(null) })
                }
                if (uiState.isAgriDoctorActive) {
                    AgriDoctorModal(uiState, viewModel)
                }
                if (uiState.isSellCropModalActive) {
                    SellCropModal(uiState, viewModel)
                }
                if (uiState.isAddBookEntryActive) {
                    AddBookEntryModal(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
fun TopAppBarSection(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    agriViewModel: com.example.ui.AgriViewModel
) {
    val currentUser by agriViewModel.currentUser.collectAsState()
    val greetingName = if (currentUser != null) {
        val firstName = currentUser!!.name.trim().split(" ").firstOrNull()?.ifBlank { null } ?: currentUser!!.name
        "Welcome, $firstName"
    } else {
        "Welcome Farmer"
    }

    Column(modifier = Modifier.fillMaxWidth().background(KrishiHeaderGreen)) {
        // Main Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Logo Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_icon_1785930251097),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "KRISHIMITHRA",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = greetingName,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Right icons: Notifications & Profile
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable { viewModel.toggleNotifications(true) }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    if (uiState.unreadNotifications > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(KrishiAccentOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.unreadNotifications}",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { viewModel.selectTab(4) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Sub Bar: Location Selector & Language Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Location Picker
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.toggleLocationSelector(true) }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Location",
                    tint = KrishiPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = uiState.selectedLocation,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = KrishiHeaderGreen
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Location",
                    tint = KrishiHeaderGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Language Switcher (English | తెలుగు)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEAF5ED))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (uiState.selectedLanguage == "English") KrishiHeaderGreen else Color.Transparent)
                        .clickable { viewModel.updateLanguage("English") }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "English",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.selectedLanguage == "English") Color.White else KrishiHeaderGreen
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (uiState.selectedLanguage == "తెలుగు") KrishiHeaderGreen else Color.Transparent)
                        .clickable { viewModel.updateLanguage("తెలుగు") }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "తెలుగు",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.selectedLanguage == "తెలుగు") Color.White else KrishiHeaderGreen
                    )
                }
            }
        }
    }
}

@Composable
fun HomeDashboardContent(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    agriViewModel: com.example.ui.AgriViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val weatherState by agriViewModel.weatherState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. SEARCH BAR
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = KrishiHeaderGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = if (uiState.selectedLanguage == "English") "Search crops, markets, tools..." else "పంటలు, మార్కెట్లు, పరికరాల కోసం శోధించండి...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { viewModel.toggleVoiceAssistant(true) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF5ED))
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        tint = KrishiPrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. FIRST PRIORITY: 🔴 LIVE MANDI RATES
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LIVE MANDI RATES",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = KrishiHeaderGreen,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Today's verified market prices",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.selectTab(1) }
                    ) {
                        Text(
                            text = "View All",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = KrishiHeaderGreen
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = KrishiHeaderGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Scroll of Mandi Crop Cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.mandiRates.take(4)) { crop ->
                        MandiCropCard(crop = crop, onClick = { viewModel.selectTab(1) })
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2EBE4))
                Spacer(modifier = Modifier.height(8.dp))

                // Mandi Verification Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = KrishiHeaderGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Prices updated from 125+ verified markets",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = KrishiHeaderGreen
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Last updated: 10:30 AM",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = KrishiHeaderGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. SECOND: 🌾 SELL MY CROP & 🛒 BUY CROPS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SELL MY CROP Card
            FarmerActionCard(
                title = "SELL MY CROP",
                subtitle = "Post your crop and\nget verified buyer offers",
                iconEmoji = "🌾",
                bgColor = Color(0xFFEAF5ED),
                borderColor = Color(0xFFC8E6C9),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.toggleSellCropModal(true) }
            )

            // BUY CROPS Card
            FarmerActionCard(
                title = "BUY CROPS",
                subtitle = "Buy quality crops\nfrom verified sellers",
                iconEmoji = "🛒",
                bgColor = Color(0xFFFFFBEA),
                borderColor = Color(0xFFFFECB3),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectTab(2) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. THIRD: 🌦️ LIVE WEATHER CARD (Requirement 2)
        val activeWeather = (weatherState as? com.example.ui.WeatherState.Success)?.weatherInfo
        val isWeatherError = weatherState is com.example.ui.WeatherState.Error
        val weatherErrorText = (weatherState as? com.example.ui.WeatherState.Error)?.message

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleWeatherDetail(true) }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⛅", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("WEATHER FORECAST", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = KrishiHeaderGreen)
                            Text(activeWeather?.locationName ?: uiState.selectedLocation, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (activeWeather != null) {
                            Text("${activeWeather.temperature.toInt()}°C", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = KrishiHeaderGreen)
                            Text(activeWeather.condition, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = KrishiPrimaryGreen)
                        } else if (isWeatherError) {
                            Text("--°C", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.Gray)
                            Text(weatherErrorText ?: "Unable to fetch weather", fontSize = 10.sp, color = Color.Red)
                        } else {
                            Text("--°C", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.Gray)
                            Text("Loading...", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💧 Humidity: ${activeWeather?.humidity?.let { "$it%" } ?: "--"}", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    Text("💨 Wind: ${activeWeather?.windSpeed?.let { "${it.toInt()} km/h" } ?: "--"}", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    Text("🌧️ Rain: ${activeWeather?.rainProbability?.let { "$it%" } ?: "--"}", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Updated: ${activeWeather?.lastUpdated ?: "Just now"}", fontSize = 10.sp, color = Color.Gray)
                    Text("View 7-Day Forecast ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI AGRI DOCTOR Card
        FarmerActionCard(
            title = "AI AGRI DOCTOR",
            subtitle = "Scan crop leaf or upload photo to diagnose diseases & get instant treatments",
            iconEmoji = "🍃",
            bgColor = Color.White,
            borderColor = Color(0xFFE2EBE4),
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.toggleAgriDoctor(true) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 5. FOURTH: 🧮 FARM TOOLS & CALCULATORS
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FARM TOOLS & CALCULATORS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = KrishiHeaderGreen,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.openCalculatorPortal() }
                    ) {
                        Text(
                            text = "View All",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = KrishiHeaderGreen
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = KrishiHeaderGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val shortcuts = listOf(
                    Triple("🚜", "Tractor\nCalculator", "1"),
                    Triple("🌾", "Crop Cost\nCalculator", "7"),
                    Triple("📦", "Bags to\nAmount", "2"),
                    Triple("📈", "Profit / Loss\nCalculator", "3"),
                    Triple("👨‍🌾", "Labour\nCalculator", "4"),
                    Triple("🎛️", "More\nTools", "all")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    shortcuts.take(3).forEach { (emoji, label, toolId) ->
                        ToolShortcutCard(
                            emoji = emoji,
                            label = label,
                            modifier = Modifier.weight(1f).padding(horizontal = 3.dp),
                            onClick = { viewModel.openCalculatorPortal(if (toolId == "all") null else toolId) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    shortcuts.drop(3).forEach { (emoji, label, toolId) ->
                        ToolShortcutCard(
                            emoji = emoji,
                            label = label,
                            modifier = Modifier.weight(1f).padding(horizontal = 3.dp),
                            onClick = { viewModel.openCalculatorPortal(if (toolId == "all") null else toolId) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. QUICK UTILITY SHORTCUTS
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickIconShortcut("📷", "Scan Crop\nDisease") { viewModel.toggleAgriDoctor(true) }
                QuickIconShortcut("🎙️", "Voice AI\nAssistant") { viewModel.toggleVoiceAssistant(true) }
                QuickIconShortcut("📰", "Market\nNews") { viewModel.toggleNewsDetail(true) }
                QuickIconShortcut("📅", "Crop\nCalendar") { viewModel.toggleNewsDetail(true) }
                QuickIconShortcut("👥", "Farmer\nStories") { viewModel.toggleStoriesDetail(true) }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ---------------------- COMPONENT CARDS ----------------------

@Composable
fun MandiCropCard(
    crop: MandiCropRate,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
        modifier = Modifier
            .width(185.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(11.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (crop.imageResId != null) {
                    Image(
                        painter = painterResource(id = crop.imageResId),
                        contentDescription = crop.commodity,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌾", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = crop.commodity,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = KrishiHeaderGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = crop.market,
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Modal: ₹${crop.modalPrice.toInt()}/qtl",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = KrishiHeaderGreen
            )

            Text(
                text = "Min: ₹${crop.minPrice.toInt()} • Max: ₹${crop.maxPrice.toInt()}",
                fontSize = 10.sp,
                color = Color.Gray
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray.copy(alpha = 0.3f))

            Text(
                text = "Date: ${crop.marketDate}",
                fontSize = 10.sp,
                color = Color.Gray
            )
            Text(
                text = "Updated: ${crop.updateTime}",
                fontSize = 10.sp,
                color = KrishiHeaderGreen,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Source: ${crop.source}",
                fontSize = 9.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FarmerActionCard(
    title: String,
    subtitle: String,
    iconEmoji: String,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = KrishiHeaderGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = KrishiTextDark,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(KrishiHeaderGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ToolShortcutCard(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = KrishiHeaderGreen,
            lineHeight = 12.sp,
            maxLines = 2
        )
    }
}

@Composable
fun QuickIconShortcut(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = KrishiHeaderGreen,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun BottomNavBarSection(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = KrishiHeaderGreen,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = uiState.currentTab == 0,
            onClick = { viewModel.selectTab(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = KrishiHeaderGreen,
                selectedTextColor = KrishiHeaderGreen,
                indicatorColor = Color(0xFFEAF5ED)
            )
        )

        NavigationBarItem(
            selected = uiState.currentTab == 1,
            onClick = { viewModel.selectTab(1) },
            icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Markets") },
            label = { Text("Markets", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = KrishiHeaderGreen,
                selectedTextColor = KrishiHeaderGreen,
                indicatorColor = Color(0xFFEAF5ED)
            )
        )

        // Center "+ Sell" Button
        NavigationBarItem(
            selected = uiState.currentTab == 2,
            onClick = { viewModel.toggleSellCropModal(true) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(KrishiHeaderGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Sell",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            label = { Text("Sell", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) }
        )

        NavigationBarItem(
            selected = uiState.currentTab == 3,
            onClick = { viewModel.selectTab(3) },
            icon = { Icon(Icons.Default.Book, contentDescription = "My Book") },
            label = { Text("My Book", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = KrishiHeaderGreen,
                selectedTextColor = KrishiHeaderGreen,
                indicatorColor = Color(0xFFEAF5ED)
            )
        )

        NavigationBarItem(
            selected = uiState.currentTab == 4,
            onClick = { viewModel.selectTab(4) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = KrishiHeaderGreen,
                selectedTextColor = KrishiHeaderGreen,
                indicatorColor = Color(0xFFEAF5ED)
            )
        )
    }
}

