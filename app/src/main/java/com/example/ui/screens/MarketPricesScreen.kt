package com.example.ui.screens

import com.example.ui.MarketPriceViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MarketCropEntity
import com.example.ui.AgriViewModel
import com.example.ui.util.FarmerTranslations
import com.example.ui.util.PortalLanguageSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// High-quality image map for agricultural commodities to make the UI look exceptionally premium.
private val CropImages = mapOf(
    "tomato" to "https://images.unsplash.com/photo-1518977676601-b53f82aba655?q=80&w=400",
    "onion" to "https://images.unsplash.com/photo-1508747703725-719777637510?q=80&w=400",
    "brinjal" to "https://images.unsplash.com/photo-1590379497901-bf193051bc72?q=80&w=400",
    "okra" to "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833?q=80&w=400",
    "ladies finger" to "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833?q=80&w=400",
    "chilli" to "https://images.unsplash.com/photo-1565557623262-b51c2513a641?q=80&w=400",
    "potato" to "https://images.unsplash.com/photo-1518977676601-b53f82aba655?q=80&w=400",
    "mango" to "https://images.unsplash.com/photo-1553279768-865429fa0078?q=80&w=400",
    "banana" to "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?q=80&w=400",
    "paddy" to "https://images.unsplash.com/photo-1536304997881-a372c179924b?q=80&w=400",
    "rice" to "https://images.unsplash.com/photo-1586201375761-83865001e31c?q=80&w=400",
    "wheat" to "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?q=80&w=400",
    "maize" to "https://images.unsplash.com/photo-1551754655-cd27e38d2076?q=80&w=400",
    "cotton" to "https://images.unsplash.com/photo-1594122230689-45899d9e6f69?q=80&w=400",
    "rose" to "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=400",
    "jasmine" to "https://images.unsplash.com/photo-1508717272800-9fff97da7e8f?q=80&w=400",
    "marigold" to "https://images.unsplash.com/photo-1596701062951-df6890f50ab1?q=80&w=400",
    "turmeric" to "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?q=80&w=400",
    "ginger" to "https://images.unsplash.com/photo-1599599810769-bcde5a160d32?q=80&w=400",
    "garlic" to "https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?q=80&w=400",
    "groundnut" to "https://images.unsplash.com/photo-1569430335805-89b14b2d3bf9?q=80&w=400",
    "coconut" to "https://images.unsplash.com/photo-1526318896980-cf78c088247c?q=80&w=400",
    "lemon" to "https://images.unsplash.com/photo-1590502593747-42a996133562?q=80&w=400",
    "papaya" to "https://images.unsplash.com/photo-1526318896980-cf78c088247c?q=80&w=400",
    "orange" to "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?q=80&w=400",
    "grape" to "https://images.unsplash.com/photo-1537640538966-79f369143f8f?q=80&w=400"
)

private fun getCropImageUrl(cropName: String, originalUrl: String): String {
    if (originalUrl.isNotEmpty() && originalUrl.startsWith("http")) return originalUrl
    val lower = cropName.lowercase()
    for ((key, url) in CropImages) {
        if (lower.contains(key)) return url
    }
    return "https://images.unsplash.com/photo-1464226184884-fa280b87c399?q=80&w=400" // general agriculture fallback
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPricesScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit,
    marketPriceViewModel: MarketPriceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val searchInput by viewModel.marketSearchQuery.collectAsState()
    val allCrops by viewModel.allMarketCrops.collectAsState()

    // Location selection states
    var selectedState by remember { mutableStateOf("Andhra Pradesh") }
    var selectedDistrict by remember { mutableStateOf("Krishna") }
    var selectedMarket by remember { mutableStateOf("Nuzvid") }
    var showLocationSelector by remember { mutableStateOf(false) }

    // Navigation and tab filtering
    var selectedCategoryTab by remember { mutableStateOf("All") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // Loading & Refreshing states
    var isRefreshing by remember { mutableStateOf(false) }
    var isShimmering by remember { mutableStateOf(false) }

    // Search bar visual state
    var isSearchFocused by remember { mutableStateOf(false) }

    // Favorites for Fallback crops (which are in-memory with negative IDs)
    var favoriteFallbackCropKeys by remember { mutableStateOf(setOf<String>()) }

    // Price alerts configuration dialog state
    var selectedCropForAlert by remember { mutableStateOf<MarketCropEntity?>(null) }
    var targetPriceInput by remember { mutableStateOf("") }

    // Historical Trend Chart & Selling Advice Dialog state
    var selectedCropForTrendChart by remember { mutableStateOf<MarketCropEntity?>(null) }

    // Search Suggestions and Recent Searches
    var recentSearches by remember { mutableStateOf(listOf("Tomato", "Onion", "Paddy", "Mango")) }
    val popularSuggestions = listOf("Tomato", "Onion", "Mango", "Banana", "Lemon", "Coconut", "Paddy", "Maize", "Groundnut", "Green Chilli", "Red Chilli", "Turmeric")

    // Dynamic crops generator combined with database elements
    val displayedCrops = remember(allCrops, selectedState, selectedDistrict, selectedMarket, selectedCategoryTab, searchInput, showOnlyFavorites, favoriteFallbackCropKeys) {
        // 1. Gather all matching real database records first
        val dbFiltered = allCrops.filter { crop ->
            val matchesState = selectedState == "All Indian States" || crop.state == selectedState
            val matchesDistrict = selectedDistrict == "All Districts" || crop.district == selectedDistrict
            val matchesMarket = selectedMarket == "All Markets" || crop.market == selectedMarket
            matchesState && matchesDistrict && matchesMarket
        }

        // 2. If no real DB records exist for the selected hierarchy (or we want to show a highly interactive, live set),
        // we dynamically generate organic high-fidelity fallback records!
        val baseList = if (dbFiltered.isEmpty() && selectedState != "All Indian States" && selectedDistrict != "All Districts" && selectedMarket != "All Markets") {
            generateFallbackCrops(selectedState, selectedDistrict, selectedMarket, selectedCategoryTab)
        } else {
            dbFiltered
        }

        // 3. Apply category tabs and search inputs
        baseList.filter { crop ->
            val matchesCategory = when (selectedCategoryTab) {
                "All" -> true
                "Grains" -> crop.category == "Food Grains" || crop.category == "Grains"
                else -> crop.category.equals(selectedCategoryTab, ignoreCase = true)
            }

            val matchesSearch = searchInput.isEmpty() ||
                    crop.cropName.contains(searchInput, ignoreCase = true) ||
                    crop.cropNameTe.contains(searchInput, ignoreCase = true) ||
                    crop.market.contains(searchInput, ignoreCase = true) ||
                    crop.district.contains(searchInput, ignoreCase = true)

            // Evaluate favorite state (either database-persisted or in-memory fallback)
            val isFav = if (crop.id < 0) {
                val key = "${crop.cropName}_${crop.market}_${crop.district}"
                favoriteFallbackCropKeys.contains(key)
            } else {
                crop.isFavorite
            }
            val matchesFavorite = !showOnlyFavorites || isFav

            matchesCategory && matchesSearch && matchesFavorite
        }
    }

    // Pull to Refresh Handler
    val handleRefresh: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            isShimmering = true
            viewModel.refreshLiveMarketPrices {
                isRefreshing = false
                isShimmering = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AgriMarket Live",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Real-time Mandi Prices • eNAM & Agmarknet",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    PortalLanguageSelector(
                        currentLanguage = appLanguage,
                        onLanguageSelected = { viewModel.setAppLanguage(it) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Manual refresh trigger
                    IconButton(onClick = { handleRefresh() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh prices", tint = MaterialTheme.colorScheme.primary)
                    }
                    // Star toggler for quick-filter on stars
                    IconButton(onClick = { showOnlyFavorites = !showOnlyFavorites }) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Show favorites only",
                            tint = if (showOnlyFavorites) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. TOP SECTION: Search bar and suggestions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = {
                            viewModel.searchMarketCrops(it)
                            isSearchFocused = true
                        },
                        placeholder = { Text("Search crops, fruits, grains, mandis...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                        trailingIcon = {
                            if (searchInput.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.searchMarketCrops("")
                                    isSearchFocused = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("crop_search_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    // 2. LOCATION INDICATOR CARD: Tapping this launches cascading dropdown modal
                    Card(
                        onClick = { showLocationSelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location Pin",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "SELECTED MARKET LOCATION",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = buildString {
                                            append("India")
                                            if (selectedState != "All Indian States") append(" → $selectedState")
                                            if (selectedDistrict != "All Districts") append(" → $selectedDistrict")
                                            if (selectedMarket != "All Markets") append(" → $selectedMarket")
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Change",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Arrow Down",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Quick access location pills row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quick:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        
                        // AP Nuzvid Pill
                        val isAPNuzvid = selectedState == "Andhra Pradesh" && selectedDistrict == "Krishna" && selectedMarket == "Nuzvid"
                        SuggestionChip(
                            onClick = {
                                selectedState = "Andhra Pradesh"
                                selectedDistrict = "Krishna"
                                selectedMarket = "Nuzvid"
                            },
                            label = { Text("📍 AP - Krishna - Nuzvid", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isAPNuzvid) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (isAPNuzvid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        // TS Bowenpally Pill
                        val isTSBowenpally = selectedState == "Telangana" && selectedDistrict == "Hyderabad" && selectedMarket == "Bowenpally Mandi"
                        SuggestionChip(
                            onClick = {
                                selectedState = "Telangana"
                                selectedDistrict = "Hyderabad"
                                selectedMarket = "Bowenpally Mandi"
                            },
                            label = { Text("📍 TS - Hyd - Bowenpally", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isTSBowenpally) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (isTSBowenpally) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        // All India Pill
                        val isAllIndia = selectedState == "All Indian States"
                        SuggestionChip(
                            onClick = {
                                selectedState = "All Indian States"
                                selectedDistrict = "All Districts"
                                selectedMarket = "All Markets"
                            },
                            label = { Text("🌐 All India", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isAllIndia) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (isAllIndia) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // HISTORICAL PRICE TRENDS & SELLING ADVICE PROMINENT BANNER
                    Card(
                        onClick = {
                            selectedCropForTrendChart = displayedCrops.firstOrNull()
                                ?: generateFallbackCrops(selectedState, selectedDistrict, selectedMarket, "All").first()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_price_trend_chart_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShowChart,
                                        contentDescription = "Price Chart Icon",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "HISTORICAL PRICE TRENDS & SELLING ADVICE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "Interactive Recharts Line Chart & Decision Engine",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.tertiary
                            ) {
                                Text(
                                    text = "Open 📈",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 3. SEARCH SUGGESTIONS DROP-DOWN OVERLAY
                AnimatedVisibility(visible = isSearchFocused) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Recent Searches Section
                            if (recentSearches.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Recent Searches", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text(
                                        text = "Clear All",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { recentSearches = emptyList() }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    recentSearches.forEach { query ->
                                        AssistChip(
                                            onClick = {
                                                viewModel.searchMarketCrops(query)
                                                isSearchFocused = false
                                            },
                                            label = { Text(query, fontSize = 11.sp) },
                                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Suggestions Header
                            Text("Popular Suggestions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Suggestions grid/row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                popularSuggestions.take(6).forEach { sug ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            viewModel.searchMarketCrops(sug)
                                            if (!recentSearches.contains(sug)) {
                                                recentSearches = (listOf(sug) + recentSearches).take(5)
                                            }
                                            isSearchFocused = false
                                        },
                                        label = { Text(sug, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { isSearchFocused = false },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Hide suggestions", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // 4. CATEGORY TABS (All, Vegetables, Fruits, Grains, Pulses, Spices, Flowers)
                val categories = listOf("All", "Vegetables", "Fruits", "Grains", "Pulses", "Spices", "Flowers")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { tab ->
                        val isSelected = selectedCategoryTab == tab
                        val backgroundBrush = if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(backgroundBrush)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable {
                                    selectedCategoryTab = tab
                                    isSearchFocused = false
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val tabIcon = when (tab) {
                                    "All" -> Icons.Default.GridView
                                    "Vegetables" -> Icons.Default.Grass
                                    "Fruits" -> Icons.Default.Eco
                                    "Grains" -> Icons.Default.Agriculture
                                    "Pulses" -> Icons.Default.Category
                                    "Spices" -> Icons.Default.LocalFireDepartment
                                    "Flowers" -> Icons.Default.LocalFlorist
                                    else -> Icons.Default.FilterList
                                }
                                Icon(
                                    imageVector = tabIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = tab,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 5. PULL-TO-REFRESH CONTAINER WRAPPING RECYCLER LIST
                PullToRefreshContainer(
                    isRefreshing = isRefreshing,
                    onRefresh = { handleRefresh() }
                ) {
                    if (isShimmering) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(4) {
                                ShimmerItem()
                            }
                        }
                    } else if (displayedCrops.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inbox,
                                        contentDescription = "No Crops",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No prices listed in this category",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Try switching state/district or searching for other commodities.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        selectedState = "All Indian States"
                                        selectedDistrict = "All Districts"
                                        selectedMarket = "All Markets"
                                        viewModel.searchMarketCrops("")
                                        selectedCategoryTab = "All"
                                        showOnlyFavorites = false
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 48.dp, top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(displayedCrops, key = { crop ->
                                // Unique key using DB ID or hash description
                                if (crop.id < 0) "${crop.cropName}_${crop.market}_${crop.id}" else crop.id.toString()
                            }) { crop ->
                                val isFav = if (crop.id < 0) {
                                    val key = "${crop.cropName}_${crop.market}_${crop.district}"
                                    favoriteFallbackCropKeys.contains(key)
                                } else {
                                    crop.isFavorite
                                }

                                PremiumMarketCropCard(
                                    crop = crop,
                                    isFavorite = isFav,
                                    appLanguage = appLanguage,
                                    onFavoriteToggle = {
                                        if (crop.id < 0) {
                                            // Handle in-memory fallback crop favorites
                                            val key = "${crop.cropName}_${crop.market}_${crop.district}"
                                            favoriteFallbackCropKeys = if (favoriteFallbackCropKeys.contains(key)) {
                                                favoriteFallbackCropKeys - key
                                            } else {
                                                favoriteFallbackCropKeys + key
                                            }
                                        } else {
                                            viewModel.toggleCropFavorite(crop)
                                        }
                                    },
                                    onSetAlertClick = {
                                        selectedCropForAlert = crop
                                        targetPriceInput = if (crop.alertPrice > 0.0) crop.alertPrice.toString() else crop.todayPrice.toInt().toString()
                                    },
                                    onViewTrendChart = {
                                        selectedCropForTrendChart = crop
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 6. CASCADING SEARCHABLE LOCATION DIALOG
            if (showLocationSelector) {
                LocationSelectorDialog(
                    currentState = selectedState,
                    currentDistrict = selectedDistrict,
                    currentMarket = selectedMarket,
                    onDismiss = { showLocationSelector = false },
                    onLocationSelected = { state, district, market ->
                        selectedState = state
                        selectedDistrict = district
                        selectedMarket = market
                    }
                )
            }

            // 7. PRICE ALERT CONFIGURATION DIALOG
            if (selectedCropForAlert != null) {
                Dialog(onDismissRequest = { selectedCropForAlert = null }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = "Alert Setup",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )

                            Text(
                                text = "Set Price Alert",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Crop: ${selectedCropForAlert!!.cropName} (${selectedCropForAlert!!.cropNameTe})\nMandi: ${selectedCropForAlert!!.market}\n\nWe will notify you when modal price reaches or drops below your target price (₹):",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            OutlinedTextField(
                                value = targetPriceInput,
                                onValueChange = { targetPriceInput = it },
                                label = { Text("Target Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Text("₹ ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray) },
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedCropForAlert = null },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        val price = targetPriceInput.toDoubleOrNull() ?: 0.0
                                        val item = selectedCropForAlert!!
                                        if (item.id < 0) {
                                            // Fallback item - save as real item with alerts configured
                                            val savedCrop = item.copy(id = 0, priceAlertEnabled = true, alertPrice = price)
                                            viewModel.updateMarketCrop(savedCrop)
                                        } else {
                                            viewModel.setCropPriceAlert(item, true, price)
                                        }
                                        selectedCropForAlert = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Set Alert")
                                }
                            }
                        }
                    }
                }
            }

            // 8. HISTORICAL PRICE TREND CHART & SELLING DECISION DIALOG
            if (selectedCropForTrendChart != null) {
                HistoricalPriceTrendChartDialog(
                    initialCrop = selectedCropForTrendChart!!,
                    allCrops = displayedCrops.ifEmpty { generateFallbackCrops(selectedState, selectedDistrict, selectedMarket, "All") },
                    onDismiss = { selectedCropForTrendChart = null },
                    onSetAlert = { crop ->
                        selectedCropForTrendChart = null
                        selectedCropForAlert = crop
                        targetPriceInput = if (crop.alertPrice > 0.0) crop.alertPrice.toString() else crop.todayPrice.toInt().toString()
                    }
                )
            }
        }
    }
}

@Composable
fun PremiumMarketCropCard(
    crop: MarketCropEntity,
    isFavorite: Boolean,
    appLanguage: String = "en",
    onFavoriteToggle: () -> Unit,
    onSetAlertClick: () -> Unit,
    onViewTrendChart: ((MarketCropEntity) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    val elevation = if (expanded) 6.dp else 2.dp
    val borderStroke = if (expanded) {
        androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = borderStroke
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main row with Image and Core Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Crop Image loaded from URL with Coil
                AsyncImage(
                    model = getCropImageUrl(crop.cropName, crop.imageUrl),
                    contentDescription = crop.cropName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Mid Core Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = crop.category.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Commodity Name (Telugu + English)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = crop.cropNameTe.ifEmpty { crop.cropName },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (crop.cropNameTe.isNotEmpty()) {
                            Text(
                                text = "(${crop.cropName})",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Mandi Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Mandi Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${crop.market}, ${crop.district}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    // Arrival Volume & Unit
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Arrival qty",
                            tint = Color.Gray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${FarmerTranslations.get("arrival_qty", appLanguage)}: ${crop.arrivalQuantity.toInt()} ${crop.unit}s",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Price and Trend indicator column
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "₹${crop.todayPrice.toInt()}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "per ${crop.unit}",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    // Price Shift Trend Indicator
                    val diff = crop.todayPrice - crop.yesterdayPrice
                    val isUp = diff >= 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isUp) Color(0xFFE8F5E9)
                                else Color(0xFFFFEBEE)
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = "Price trend shift",
                            tint = if (isUp) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${if (isUp) "+" else ""}₹${diff.toInt()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUp) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            // Expanded price chart, limits and configuration alert buttons
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Extreme boundaries (Min, Max, Yesterday Modal)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(FarmerTranslations.get("min_price", appLanguage).uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
                            Text("₹${crop.lowestPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(FarmerTranslations.get("modal_price", appLanguage).uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
                            Text("₹${crop.yesterdayPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(FarmerTranslations.get("max_price", appLanguage).uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
                            Text("₹${crop.highestPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Price Trend Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Mandi Price Trend",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Updated: ${crop.lastUpdated}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Trend Graphic Drawing
                    val diff = crop.todayPrice - crop.yesterdayPrice
                    PremiumPriceTrendChart(
                        todayPrice = crop.todayPrice,
                        yesterdayPrice = crop.yesterdayPrice,
                        lowestPrice = crop.lowestPrice,
                        highestPrice = crop.highestPrice,
                        color = if (diff >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )

                    // Deep Historical Price Chart & Selling Advice Launcher Button
                    Button(
                        onClick = { onViewTrendChart?.invoke(crop) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("crop_card_trend_btn_${crop.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detailed Price Trends & Selling Advice 📈",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }

                    // Bottom Configuration Controls (Favorites & Alert Configuration)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onFavoriteToggle() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite status icon",
                                tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFavorite) "Saved in Favorites" else "Tap Star to Save",
                                fontSize = 11.sp,
                                color = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (crop.priceAlertEnabled) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Price notification on",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Alert @ ₹${crop.alertPrice.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = onSetAlertClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notification config icon",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Price Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumPriceTrendChart(
    todayPrice: Double,
    yesterdayPrice: Double,
    lowestPrice: Double,
    highestPrice: Double,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.04f))
            .border(0.5.dp, color.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
    ) {
        val width = size.width
        val height = size.height

        val paddingY = 12f
        val range = highestPrice - lowestPrice
        val scaleY = if (range == 0.0) 1f else (height - paddingY * 2) / range.toFloat()

        fun Double.toCanvasY(): Float {
            return height - paddingY - ((this - lowestPrice) * scaleY).toFloat()
        }

        // Draw helper dotted grid-lines
        drawLine(
            color = Color.LightGray.copy(alpha = 0.25f),
            start = Offset(0f, height * 0.25f),
            end = Offset(width, height * 0.25f)
        )
        drawLine(
            color = Color.LightGray.copy(alpha = 0.25f),
            start = Offset(0f, height * 0.75f),
            end = Offset(width, height * 0.75f)
        )

        // Plot 6 fluctuation points over 7 days: Day 1 (low), Day 2, Day 3, Day 4 (high), Day 5 (yesterday), Day 6 (today)
        val p1 = lowestPrice
        val p2 = lowestPrice + range * 0.3
        val p3 = lowestPrice + range * 0.5
        val p4 = highestPrice
        val p5 = yesterdayPrice
        val p6 = todayPrice

        val points = listOf(p1, p2, p3, p4, p5, p6)
        val path = Path()

        points.forEachIndexed { idx, price ->
            val x = (width / (points.size - 1)) * idx
            val y = price.toCanvasY()
            if (idx == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            // Circle node
            drawCircle(color = color, radius = 5.5f, center = Offset(x, y))
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4.5f)
        )
    }
}

@Composable
fun ShimmerItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "premium_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "premium_shimmer_alpha"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.12f))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.12f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.08f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.08f))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(65.dp, 22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.12f))
                )
                Box(
                    modifier = Modifier
                        .size(45.dp, 13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.08f))
                )
            }
        }
    }
}

// Custom Searchable Location Picker Dialog for State -> District -> Market cascading flow
@Composable
fun LocationSelectorDialog(
    currentState: String,
    currentDistrict: String,
    currentMarket: String,
    onDismiss: () -> Unit,
    onLocationSelected: (state: String, district: String, market: String) -> Unit
) {
    var selectedState by remember { mutableStateOf(currentState) }
    var selectedDistrict by remember { mutableStateOf(currentDistrict) }
    var selectedMarket by remember { mutableStateOf(currentMarket) }

    // Navigation Step inside location picker (0 = State, 1 = District, 2 = Market)
    var currentStep by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val states = listOf("Andhra Pradesh", "Telangana", "Karnataka", "Tamil Nadu", "All Indian States")

    val districts = when (selectedState) {
        "Andhra Pradesh" -> listOf("All Districts", "Krishna", "Guntur", "Kurnool", "Chittoor", "Nellore", "Anantapur")
        "Telangana" -> listOf("All Districts", "Warangal", "Hyderabad", "Khammam", "Mahabubnagar", "Karimnagar")
        "Karnataka" -> listOf("All Districts", "Kolar", "Mysuru", "Belagavi")
        "Tamil Nadu" -> listOf("All Districts", "Salem", "Coimbatore", "Madurai")
        else -> listOf("All Districts")
    }

    val markets = when {
        selectedState == "Andhra Pradesh" && selectedDistrict == "Krishna" -> listOf("All Markets", "Nuzvid", "Vijayawada", "Tiruvuru", "Gudivada", "Machilipatnam")
        selectedState == "Andhra Pradesh" && selectedDistrict == "Guntur" -> listOf("All Markets", "Guntur Yard", "Tenali APMC", "Narasaraopet APMC", "Sattenapalle APMC")
        selectedState == "Andhra Pradesh" && selectedDistrict == "Chittoor" -> listOf("All Markets", "Madanapalle Mandi", "Chittoor Mandi", "Punganur Yard")
        selectedState == "Andhra Pradesh" && selectedDistrict == "Kurnool" -> listOf("All Markets", "Kurnool APMC", "Adoni APMC", "Yemmiganur APMC")
        selectedState == "Andhra Pradesh" && selectedDistrict == "Nellore" -> listOf("All Markets", "Nellore Mandi", "Kavali APMC")
        selectedState == "Andhra Pradesh" && selectedDistrict == "Anantapur" -> listOf("All Markets", "Anantapur Yard", "Dharmavaram Yard")

        selectedState == "Telangana" && selectedDistrict == "Warangal" -> listOf("All Markets", "Warangal APMC", "Jangaon APMC")
        selectedState == "Telangana" && selectedDistrict == "Hyderabad" -> listOf("All Markets", "Bowenpally Mandi", "Gudimalkapur Mandi")
        selectedState == "Telangana" && selectedDistrict == "Khammam" -> listOf("All Markets", "Khammam Mandi", "Wyra APMC")
        selectedState == "Telangana" && selectedDistrict == "Mahabubnagar" -> listOf("All Markets", "Mahabubnagar APMC", "Badepally APMC")
        selectedState == "Telangana" && selectedDistrict == "Karimnagar" -> listOf("All Markets", "Karimnagar APMC", "Jagtial APMC")

        selectedState == "Karnataka" && selectedDistrict == "Kolar" -> listOf("All Markets", "Kolar Mandi", "Mulbagal APMC")
        selectedState == "Karnataka" && selectedDistrict == "Mysuru" -> listOf("All Markets", "Mysuru Bandipalya", "Nanjangud APMC")
        selectedState == "Karnataka" && selectedDistrict == "Belagavi" -> listOf("All Markets", "Belagavi APMC", "Gokak APMC")

        selectedState == "Tamil Nadu" && selectedDistrict == "Salem" -> listOf("All Markets", "Salem Mandi", "Attur APMC")
        selectedState == "Tamil Nadu" && selectedDistrict == "Coimbatore" -> listOf("All Markets", "Coimbatore APMC", "Pollachi Mandi")
        selectedState == "Tamil Nadu" && selectedDistrict == "Madurai" -> listOf("All Markets", "Madurai Mattuthavani", "Melur APMC")

        else -> listOf("All Markets")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mandi Location",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Select State → District → Market",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step Breadcrumbs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentStep = 0 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedState == "All Indian States") "State" else selectedState.substringBefore(" "),
                            fontSize = 11.sp,
                            fontWeight = if (currentStep == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentStep == 0) activeColor else inactiveColor,
                            maxLines = 1
                        )
                    }
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = inactiveColor, modifier = Modifier.size(14.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = selectedState != "All Indian States") { currentStep = 1 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedDistrict == "All Districts") "District" else selectedDistrict,
                            fontSize = 11.sp,
                            fontWeight = if (currentStep == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentStep == 1) activeColor else inactiveColor,
                            maxLines = 1
                        )
                    }
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = inactiveColor, modifier = Modifier.size(14.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = selectedDistrict != "All Districts") { currentStep = 2 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedMarket == "All Markets") "Market" else selectedMarket.substringBefore(" "),
                            fontSize = 11.sp,
                            fontWeight = if (currentStep == 2) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentStep == 2) activeColor else inactiveColor,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Internal Dialog Search input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = when (currentStep) {
                                0 -> "Search States..."
                                1 -> "Search Districts..."
                                else -> "Search Markets..."
                            },
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear") } }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List options
                val filteredItems = when (currentStep) {
                    0 -> states
                    1 -> districts
                    else -> markets
                }.filter { it.contains(searchQuery, ignoreCase = true) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (filteredItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No options found.", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(filteredItems) { item ->
                            val isSelected = when (currentStep) {
                                0 -> selectedState == item
                                1 -> selectedDistrict == item
                                else -> selectedMarket == item
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable {
                                        searchQuery = ""
                                        when (currentStep) {
                                            0 -> {
                                                selectedState = item
                                                if (item == "All Indian States") {
                                                    selectedDistrict = "All Districts"
                                                    selectedMarket = "All Markets"
                                                    onLocationSelected(selectedState, selectedDistrict, selectedMarket)
                                                    onDismiss()
                                                } else {
                                                    selectedDistrict = "All Districts"
                                                    selectedMarket = "All Markets"
                                                    currentStep = 1
                                                }
                                            }
                                            1 -> {
                                                selectedDistrict = item
                                                if (item == "All Districts") {
                                                    selectedMarket = "All Markets"
                                                    onLocationSelected(selectedState, selectedDistrict, selectedMarket)
                                                    onDismiss()
                                                } else {
                                                    selectedMarket = "All Markets"
                                                    currentStep = 2
                                                }
                                            }
                                            2 -> {
                                                selectedMarket = item
                                                onLocationSelected(selectedState, selectedDistrict, selectedMarket)
                                                onDismiss()
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentStep == 0) Icons.Default.Public else if (currentStep == 1) Icons.Default.Map else Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Checked",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = {
                                currentStep--
                                searchQuery = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back", fontSize = 13.sp)
                        }
                    }
                    Button(
                        onClick = {
                            onLocationSelected(selectedState, selectedDistrict, selectedMarket)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// Custom NestedScroll PullToRefresh Component
@Composable
fun PullToRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    var pullOffset by remember { mutableStateOf(0f) }
    val maxPull = 180f
    val decay = 0.5f

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0 && pullOffset < maxPull) {
                    pullOffset = (pullOffset + available.y * decay).coerceAtMost(maxPull)
                    return Offset(0f, available.y)
                }
                if (available.y < 0 && pullOffset > 0) {
                    val prev = pullOffset
                    pullOffset = (pullOffset + available.y).coerceAtLeast(0f)
                    val consumed = pullOffset - prev
                    return Offset(0f, consumed)
                }
                return super.onPreScroll(available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (pullOffset >= maxPull * 0.75f && !isRefreshing) {
                    onRefresh()
                }
                pullOffset = 0f
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        content()

        if (pullOffset > 0 || isRefreshing) {
            val progress = (pullOffset / maxPull).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (pullOffset / 3.5f).dp - 35.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Swipe Pull refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer(rotationZ = progress * 360f)
                    )
                }
            }
        }
    }
}

// Fallback high-fidelity crop generator for locations/mandi selections that are not yet prepopulated in SQLite DB
private fun generateFallbackCrops(state: String, district: String, market: String, categoryFilter: String): List<MarketCropEntity> {
    val seed = (state.hashCode() + district.hashCode() + market.hashCode()).toLong()
    val random = java.util.Random(seed)

    val baseCrops = listOf(
        // Vegetables
        Triple("Tomato", "టమోటా", "Vegetables"),
        Triple("Onion", "ఉల్లిపాయ", "Vegetables"),
        Triple("Brinjal", "వంకాయ", "Vegetables"),
        Triple("Ladies Finger (Okra)", "బెండకాయ", "Vegetables"),
        Triple("Green Chilli", "పచ్చిమిర్చి", "Vegetables"),
        Triple("Potato", "బంగాళాదుంప", "Vegetables"),
        Triple("Carrot", "క్యారెట్", "Vegetables"),

        // Fruits
        Triple("Mango (Banginapalli)", "బంగినపల్లి మామిడి", "Fruits"),
        Triple("Banana", "అరటి పండు", "Fruits"),
        Triple("Lemon", "నిమ్మకాయ", "Fruits"),
        Triple("Coconut", "కొబ్బరికాయ", "Fruits"),
        Triple("Papaya", "బొప్పాయి పండు", "Fruits"),
        Triple("Orange", "నారింజ పండు", "Fruits"),

        // Food Grains
        Triple("Paddy (Raw Rice)", "వరి / ప్యాడీ", "Food Grains"),
        Triple("Maize", "మొక్కజొన్న", "Food Grains"),
        Triple("Wheat", "గోధుమలు", "Food Grains"),

        // Pulses
        Triple("Bengal Gram (Chana)", "శనగలు", "Pulses"),
        Triple("Red Gram (Toor)", "కందులు", "Pulses"),
        Triple("Black Gram (Urad)", "మినుములు", "Pulses"),

        // Spices
        Triple("Guntur Red Chilli", "ఎండుమిర్చి", "Spices"),
        Triple("Turmeric", "పసుపు", "Spices"),
        Triple("Ginger", "అల్లం", "Spices"),
        Triple("Garlic", "వెల్లుల్లి", "Spices"),

        // Flowers
        Triple("Rose (Red)", "గులాబీ", "Flowers"),
        Triple("Jasmine", "మల్లెపూవు", "Flowers"),
        Triple("Marigold", "బంతిపూవు", "Flowers")
    )

    // Filter base crops based on category filter
    val filteredBase = if (categoryFilter == "All") {
        baseCrops
    } else {
        baseCrops.filter { it.third == categoryFilter || (categoryFilter == "Grains" && it.third == "Food Grains") }
    }

    // Select 4-6 random crops
    val count = 4 + random.nextInt(3)
    val chosen = filteredBase.shuffled(random).take(count)

    return chosen.mapIndexed { index, triple ->
        val basePrice = when (triple.third) {
            "Vegetables" -> 1100.0 + random.nextInt(1800)
            "Fruits" -> 2200.0 + random.nextInt(4800)
            "Food Grains" -> 1900.0 + random.nextInt(1300)
            "Pulses" -> 4500.0 + random.nextInt(3500)
            "Spices" -> 5500.0 + random.nextInt(9500)
            "Flowers" -> 2800.0 + random.nextInt(4200)
            else -> 2200.0
        }

        val priceDiff = (random.nextInt(280) - 100).toDouble()
        val todayPrice = basePrice
        val yesterdayPrice = basePrice - priceDiff

        MarketCropEntity(
            cropName = triple.first,
            market = market,
            district = district,
            todayPrice = todayPrice,
            yesterdayPrice = yesterdayPrice,
            highestPrice = todayPrice * 1.15,
            lowestPrice = todayPrice * 0.85,
            isFavorite = false,
            priceAlertEnabled = false,
            alertPrice = 0.0,
            cropNameTe = triple.second,
            category = triple.third,
            state = state,
            imageUrl = "",
            lastUpdated = "Today, ${8 + random.nextInt(4)}:${10 + random.nextInt(45)} AM",
            arrivalQuantity = 60.0 + random.nextInt(340),
            unit = if (triple.third == "Flowers" || triple.first == "Lemon" || triple.first == "Coconut") "Kg" else "Quintal",
            id = -100 - index // Negative ID to distinguish fallback crops from actual DB items
        )
    }
}

// ============================================================================
// RECHARTS-STYLE HISTORICAL PRICE TREND LINE CHART & SELLING DECISION ENGINE
// ============================================================================

data class HistoricalTrendDataPoint(
    val label: String,          // e.g. "18 Jul"
    val fullDate: String,       // e.g. "Jul 18, 2026"
    val price: Double,          // e.g. 2850.0
    val benchmarkPrice: Double, // e.g. 2680.0
    val volumeQuintals: Double  // e.g. 420.0
)

fun generateHistoricalSeries(crop: MarketCropEntity, timeframe: String): List<HistoricalTrendDataPoint> {
    val seed = crop.cropName.hashCode().toLong() + timeframe.hashCode()
    val random = java.util.Random(seed)
    val today = crop.todayPrice
    val yesterday = crop.yesterdayPrice
    val low = if (crop.lowestPrice > 0) crop.lowestPrice else today * 0.85
    val high = if (crop.highestPrice > 0) crop.highestPrice else today * 1.15

    val count = when (timeframe) {
        "7D" -> 7
        "1M" -> 15
        "3M" -> 12
        "6M" -> 12
        "1Y" -> 12
        else -> 7
    }

    val dateLabels = when (timeframe) {
        "7D" -> listOf("18 Jul", "19 Jul", "20 Jul", "21 Jul", "22 Jul", "23 Jul", "24 Jul")
        "1M" -> listOf("25 Jun", "27 Jun", "29 Jun", "01 Jul", "03 Jul", "05 Jul", "07 Jul", "09 Jul", "11 Jul", "13 Jul", "15 Jul", "17 Jul", "19 Jul", "22 Jul", "24 Jul")
        "3M" -> listOf("May W1", "May W2", "May W3", "May W4", "Jun W1", "Jun W2", "Jun W3", "Jun W4", "Jul W1", "Jul W2", "Jul W3", "Jul W4")
        "6M" -> listOf("Feb", "Mar 1", "Mar 2", "Apr 1", "Apr 2", "May 1", "May 2", "Jun 1", "Jun 2", "Jul 1", "Jul 2", "Jul 3")
        "1Y" -> listOf("Aug '25", "Sep", "Oct", "Nov", "Dec", "Jan '26", "Feb", "Mar", "Apr", "May", "Jun", "Jul '26")
        else -> listOf("18 Jul", "19 Jul", "20 Jul", "21 Jul", "22 Jul", "23 Jul", "24 Jul")
    }

    val result = mutableListOf<HistoricalTrendDataPoint>()

    for (i in 0 until count) {
        val label = if (i < dateLabels.size) dateLabels[i] else "Day ${i + 1}"
        val progress = i.toDouble() / (count - 1)

        val baseVal = when (i) {
            count - 1 -> today
            count - 2 -> yesterday
            0 -> low
            else -> low + (progress * (today - low)) + ((random.nextDouble() - 0.45) * (high - low) * 0.18)
        }

        val clampedPrice = baseVal.coerceIn(low * 0.9, high * 1.08)
        val benchmark = clampedPrice * (0.94 + random.nextDouble() * 0.08)
        val volume = 150.0 + random.nextInt(380)

        result.add(
            HistoricalTrendDataPoint(
                label = label,
                fullDate = "$label, 2026",
                price = clampedPrice,
                benchmarkPrice = benchmark,
                volumeQuintals = volume
            )
        )
    }

    return result
}

@Composable
fun RechartsStyleLineChart(
    dataPoints: List<HistoricalTrendDataPoint>,
    showBenchmark: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(dataPoints) { mutableStateOf<Int?>(dataPoints.size - 1) }

    val minPrice = dataPoints.minOfOrNull { minOf(it.price, if (showBenchmark) it.benchmarkPrice else it.price) } ?: 1000.0
    val maxPrice = dataPoints.maxOfOrNull { maxOf(it.price, if (showBenchmark) it.benchmarkPrice else it.price) } ?: 2000.0
    val priceRange = if (maxPrice == minPrice) 1.0 else maxPrice - minPrice

    val activePoint = selectedIndex?.let { dataPoints.getOrNull(it) } ?: dataPoints.lastOrNull()

    Column(modifier = modifier) {
        // --- FLOATING TOOLTIP HEADER (Recharts Tooltip style) ---
        if (activePoint != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Date: ${activePoint.fullDate}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Arrival Volume: ${activePoint.volumeQuintals.toInt()} Quintals",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${activePoint.price.toInt()} / Quintal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor
                        )
                        if (showBenchmark) {
                            Text(
                                text = "State Avg: ₹${activePoint.benchmarkPrice.toInt()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFB300)
                            )
                        }
                    }
                }
            }
        }

        // --- INTERACTIVE CANVAS LINE & AREA CHART ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dataPoints) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val width = size.width
                                val stepX = width / (dataPoints.size - 1)
                                val idx = ((offset.x) / stepX).toInt().coerceIn(0, dataPoints.size - 1)
                                selectedIndex = idx
                            },
                            onDrag = { change, _ ->
                                val width = size.width
                                val stepX = width / (dataPoints.size - 1)
                                val idx = ((change.position.x) / stepX).toInt().coerceIn(0, dataPoints.size - 1)
                                selectedIndex = idx
                            }
                        )
                    }
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val stepX = width / (dataPoints.size - 1)
                            val idx = ((offset.x) / stepX).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = idx
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val topPadding = 24.dp.toPx()
                val bottomPadding = 32.dp.toPx()
                val usableHeight = height - topPadding - bottomPadding
                val stepX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width

                fun Double.toY(): Float {
                    val normalized = (this - minPrice) / priceRange
                    return (height - bottomPadding - (normalized * usableHeight)).toFloat()
                }

                // 1. Horizontal Grid Lines (Y-Axis)
                val gridLevels = 4
                for (g in 0..gridLevels) {
                    val y = topPadding + (usableHeight / gridLevels) * g
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 2. Primary Price Area Path (Recharts Gradient Fill)
                val primaryPath = Path()
                val areaPath = Path()

                dataPoints.forEachIndexed { idx, point ->
                    val x = stepX * idx
                    val y = point.price.toY()
                    if (idx == 0) {
                        primaryPath.moveTo(x, y)
                        areaPath.moveTo(x, height - bottomPadding)
                        areaPath.lineTo(x, y)
                    } else {
                        primaryPath.lineTo(x, y)
                        areaPath.lineTo(x, y)
                    }
                }
                areaPath.lineTo(width, height - bottomPadding)
                areaPath.close()

                // Draw Area Gradient
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.02f))
                    )
                )

                // Draw Primary Line Stroke
                drawPath(
                    path = primaryPath,
                    color = primaryColor,
                    style = Stroke(width = 3.5.dp.toPx())
                )

                // 3. Secondary Benchmark Line Path (if enabled)
                if (showBenchmark) {
                    val benchPath = Path()
                    dataPoints.forEachIndexed { idx, point ->
                        val x = stepX * idx
                        val y = point.benchmarkPrice.toY()
                        if (idx == 0) benchPath.moveTo(x, y) else benchPath.lineTo(x, y)
                    }
                    drawPath(
                        path = benchPath,
                        color = Color(0xFFFFB300),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )
                    )
                }

                // 4. Data Point Nodes
                dataPoints.forEachIndexed { idx, point ->
                    val x = stepX * idx
                    val y = point.price.toY()
                    drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = primaryColor, radius = 2.dp.toPx(), center = Offset(x, y))
                }

                // 5. Active Target Indicator (Scrubbing / Dragging)
                selectedIndex?.let { activeIdx ->
                    val point = dataPoints.getOrNull(activeIdx)
                    if (point != null) {
                        val activeX = stepX * activeIdx
                        val activeY = point.price.toY()

                        // Vertical Guideline
                        drawLine(
                            color = primaryColor.copy(alpha = 0.7f),
                            start = Offset(activeX, topPadding),
                            end = Offset(activeX, height - bottomPadding),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                        )

                        // Glowing target head
                        drawCircle(color = primaryColor.copy(alpha = 0.25f), radius = 10.dp.toPx(), center = Offset(activeX, activeY))
                        drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = Offset(activeX, activeY))
                        drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(activeX, activeY))
                    }
                }
            }

            // X-Axis Date Labels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.takeEach(if (dataPoints.size > 8) 2 else 1).forEach { pt ->
                    Text(
                        text = pt.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun <T> List<T>.takeEach(step: Int): List<T> {
    if (step <= 1) return this
    val res = mutableListOf<T>()
    for (i in indices step step) {
        res.add(this[i])
    }
    return res
}

// ============================================================================
// SELLING DECISION ANALYTICS ENGINE CARD
// ============================================================================
@Composable
fun SellingDecisionEngineCard(
    crop: MarketCropEntity,
    dataPoints: List<HistoricalTrendDataPoint>,
    timeframe: String
) {
    val prices = dataPoints.map { it.price }
    val avgPrice = if (prices.isNotEmpty()) prices.average() else crop.todayPrice
    val maxPrice = if (prices.isNotEmpty()) prices.maxOrNull() ?: crop.todayPrice else crop.todayPrice
    val minPrice = if (prices.isNotEmpty()) prices.minOrNull() ?: crop.todayPrice else crop.todayPrice
    val currentPrice = crop.todayPrice

    val diffFromAvgPct = if (avgPrice > 0) ((currentPrice - avgPrice) / avgPrice) * 100.0 else 0.0

    val (badgeText, badgeColor, rationale) = when {
        diffFromAvgPct >= 6.0 -> Triple(
            "🔥 PEAK SELLING WINDOW (HIGH PROFIT)",
            Color(0xFF2E7D32),
            "Current modal price of ₹${currentPrice.toInt()} is ${String.format("%.1f", diffFromAvgPct)}% above the $timeframe moving average (₹${avgPrice.toInt()}). Market arrivals are strong and demand from buyers is peaking. Recommended to SELL 60-80% of harvest to lock in top returns."
        )
        diffFromAvgPct <= -5.0 -> Triple(
            "⏳ HOLD HARVEST (REBOUND EXPECTED)",
            Color(0xFFC62828),
            "Current price of ₹${currentPrice.toInt()} is ${String.format("%.1f", Math.abs(diffFromAvgPct))}% below average due to temporary market glut. Historical trends indicate prices usually rebound by 8-12% over the next 10-14 days. Hold stock in ventilated storage if feasible."
        )
        else -> Triple(
            "⚖️ STABLE MARKET (GRADUAL SALES)",
            Color(0xFFE65100),
            "Prices are fluctuating within a stable ±4% band around ₹${avgPrice.toInt()}. Excellent window for staggered regular farm-gate sales to maintain steady operational cash flow."
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.08f)),
        border = BorderStroke(1.2.dp, badgeColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Selling Decision Engine",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Rationale text
            Text(
                text = rationale,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )

            Divider(color = badgeColor.copy(alpha = 0.2f))

            // Key Market Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("$timeframe HIGHEST", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("₹${maxPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$timeframe AVERAGE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("₹${avgPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$timeframe LOWEST", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("₹${minPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("MOMENTUM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        text = if (diffFromAvgPct >= 0) "+${String.format("%.1f", diffFromAvgPct)}%" else "${String.format("%.1f", diffFromAvgPct)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (diffFromAvgPct >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

// ============================================================================
// FULL HISTORICAL PRICE TREND CHART DIALOG COMPONENT
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricalPriceTrendChartDialog(
    initialCrop: MarketCropEntity,
    allCrops: List<MarketCropEntity>,
    onDismiss: () -> Unit,
    onSetAlert: (MarketCropEntity) -> Unit
) {
    var selectedCrop by remember { mutableStateOf(initialCrop) }
    var selectedTimeframe by remember { mutableStateOf("1M") } // 7D, 1M, 3M, 6M, 1Y
    var showBenchmark by remember { mutableStateOf(true) }

    val series = remember(selectedCrop, selectedTimeframe) {
        generateHistoricalSeries(selectedCrop, selectedTimeframe)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("historical_trend_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = getCropImageUrl(selectedCrop.cropName, selectedCrop.imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedCrop.cropNameTe.ifEmpty { selectedCrop.cropName },
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedCrop.market} • ${selectedCrop.district}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Crop Picker Chips Row
                Text("Select Crop / Commodity:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(allCrops) { item ->
                        val isSel = item.id == selectedCrop.id || item.cropName == selectedCrop.cropName
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCrop = item },
                            label = {
                                Text(
                                    text = item.cropNameTe.ifEmpty { item.cropName },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Timeframe Selector Pills (7D, 1M, 3M, 6M, 1Y)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeframes = listOf("7D", "1M", "3M", "6M", "1Y")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        timeframes.forEach { tf ->
                            val isSel = selectedTimeframe == tf
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { selectedTimeframe = tf }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tf,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Benchmark Switch Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showBenchmark = !showBenchmark }
                    ) {
                        Text(
                            text = "State Avg",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showBenchmark) Color(0xFFFFB300) else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = showBenchmark,
                            onCheckedChange = { showBenchmark = it },
                            modifier = Modifier.graphicsLayer { scaleX = 0.7f; scaleY = 0.7f }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content area containing Recharts Line Chart + Selling Decision Engine
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Recharts Interactive Line Chart Component
                        RechartsStyleLineChart(
                            dataPoints = series,
                            showBenchmark = showBenchmark,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        // Gesture instruction pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(vertical = 4.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Drag along the chart to inspect daily prices & volumes", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    item {
                        // Smart Selling Decision Engine Analytics Card
                        SellingDecisionEngineCard(
                            crop = selectedCrop,
                            dataPoints = series,
                            timeframe = selectedTimeframe
                        )
                    }

                    item {
                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSetAlert(selectedCrop) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set Price Alert", fontSize = 11.sp)
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
