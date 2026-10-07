package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AgriShopItem
import com.example.ui.AgriShopsViewModel
import com.example.ui.AgriViewModel
import com.example.ui.ShopViewMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriShopsScreen(
    agriViewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isTelugu by remember { mutableStateOf(false) }

    val shopsViewModel: AgriShopsViewModel = viewModel()
    val currentLat by shopsViewModel.currentLat.collectAsStateWithLifecycle()
    val currentLng by shopsViewModel.currentLng.collectAsStateWithLifecycle()
    val locationName by shopsViewModel.locationName.collectAsStateWithLifecycle()
    val isGpsActive by shopsViewModel.isGpsActive.collectAsStateWithLifecycle()
    val isPermissionGranted by shopsViewModel.isPermissionGranted.collectAsStateWithLifecycle()
    val radiusKm by shopsViewModel.searchRadiusKm.collectAsStateWithLifecycle()
    val selectedCategory by shopsViewModel.selectedCategory.collectAsStateWithLifecycle()
    val viewMode by shopsViewModel.viewMode.collectAsStateWithLifecycle()
    val searchQuery by shopsViewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by shopsViewModel.isLoading.collectAsStateWithLifecycle()
    val shopsList by shopsViewModel.shopsList.collectAsStateWithLifecycle()
    val selectedMapShop by shopsViewModel.selectedMapShop.collectAsStateWithLifecycle()
    val errorMessage by shopsViewModel.errorMessage.collectAsStateWithLifecycle()

    var showManualSearchInput by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            shopsViewModel.requestGpsLocation(context)
        } else {
            Toast.makeText(
                context,
                if (isTelugu) "GPS అనుమతి నిరాకరించబడింది. మీరు నేరుగా శోధించవచ్చు." else "GPS Permission denied. Searching manually around selected location.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            shopsViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTelugu) "పక్కనే ఉన్న వ్యవసాయ దుకాణాలు" else "Nearby Agricultural Shops",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isTelugu) "ఎరువులు • విత్తనాలు • యంత్రాలు • క్రిమిసంహారకాలు" else "Fertilizers • Seeds • Pesticides • Equipment",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("shops_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFB74D).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { isTelugu = !isTelugu }
                            .testTag("shops_language_toggle_chip")
                    ) {
                        Text(
                            text = if (isTelugu) "తెలుగు" else "English",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB74D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF81C784).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { shopsViewModel.setTestLocationSurepalle() }
                            .testTag("surepalle_test_preset_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Surepalle Test",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D2214))
            )
        },
        containerColor = Color(0xFF06180E)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("agri_shops_screen_container")
        ) {
            // --- LOCATION & SEARCH HEADER CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("location_search_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13281B)),
                border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isGpsActive) Color(0xFF81C784).copy(alpha = 0.2f) else Color(0xFFFFB74D).copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isGpsActive) Icons.Default.MyLocation else Icons.Default.PinDrop,
                                    contentDescription = null,
                                    tint = if (isGpsActive) Color(0xFF81C784) else Color(0xFFFFB74D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTelugu) "వర్తమాన ప్రాంతం:" else "Current Center Location:",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = locationName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isGpsActive) Color(0xFF81C784).copy(alpha = 0.2f) else Color(0xFFFFB74D).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isGpsActive) "GPS Live" else "Test / Custom",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGpsActive) Color(0xFF81C784) else Color(0xFFFFB74D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Action buttons row for Location
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isPermissionGranted) {
                                    shopsViewModel.requestGpsLocation(context)
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("use_gps_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTelugu) "లైవ్ జిపిఎస్ వాడండి" else "Use Live GPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showManualSearchInput = !showManualSearchInput },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("search_village_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81C784))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTelugu) "గ్రామం శోధించండి" else "Search Village",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Expandable Manual Search Bar
                    AnimatedVisibility(visible = showManualSearchInput) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { shopsViewModel.setSearchQueryText(it) },
                                placeholder = {
                                    Text(
                                        text = if (isTelugu) "గ్రామం/మండలం పేరు (ఉదా: Surepalle, Musunuru, Eluru)" else "e.g. Surepalle, Musunuru, Eluru",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("manual_location_input_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = Color(0xFF81C784),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    shopsViewModel.searchManualLocation(searchQuery)
                                })
                            )

                            Button(
                                onClick = { shopsViewModel.searchManualLocation(searchQuery) },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
                            ) {
                                Text(
                                    text = if (isTelugu) "వెతుకు" else "Find",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- RADIUS & CATEGORY CONTROLS BAR ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Radius Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isTelugu) "పరిధి:" else "Radius:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        listOf(5, 10, 25, 50).forEach { r ->
                            val isSelected = radiusKm == r
                            FilterChip(
                                selected = isSelected,
                                onClick = { shopsViewModel.setRadius(r) },
                                label = {
                                    Text(
                                        text = "${r}km",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF81C784),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    labelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = Color(0xFF81C784)
                                ),
                                modifier = Modifier.testTag("radius_chip_${r}km")
                            )
                        }
                    }

                    // View Mode Switcher Toggle (List vs Map)
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { shopsViewModel.setViewMode(ShopViewMode.LIST) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (viewMode == ShopViewMode.LIST) Color(0xFF81C784) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .testTag("list_view_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = "List View",
                                tint = if (viewMode == ShopViewMode.LIST) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { shopsViewModel.setViewMode(ShopViewMode.MAP) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (viewMode == ShopViewMode.MAP) Color(0xFF81C784) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .testTag("map_view_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map View",
                                tint = if (viewMode == ShopViewMode.MAP) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Category Filter Scrollable Chips
                val categories = remember {
                    listOf(
                        "All" to if (isTelugu) "అన్ని దుకాణాలు" else "All Shops",
                        "Fertilizer" to if (isTelugu) "ఎరువులు" else "Fertilizers",
                        "Seed" to if (isTelugu) "విత్తనాలు" else "Seeds",
                        "Pesticide" to if (isTelugu) "పురుగుమందులు" else "Pesticides",
                        "Equipment" to if (isTelugu) "పరికరాలు" else "Equipment",
                        "Machinery" to if (isTelugu) "ట్రాక్టర్లు / యంత్రాలు" else "Machinery"
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { (catKey, catName) ->
                        val isSelected = selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { shopsViewModel.setCategory(catKey) },
                            label = {
                                Text(
                                    text = catName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                val icon = when (catKey) {
                                    "Fertilizer" -> Icons.Default.Science
                                    "Seed" -> Icons.Default.Grass
                                    "Pesticide" -> Icons.Default.BugReport
                                    "Equipment" -> Icons.Default.Build
                                    "Machinery" -> Icons.Default.PrecisionManufacturing
                                    else -> Icons.Default.Storefront
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) Color.Black else Color(0xFF81C784)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF81C784),
                                selectedLabelColor = Color.Black,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.White.copy(alpha = 0.2f),
                                selectedBorderColor = Color(0xFF81C784)
                            ),
                            modifier = Modifier.testTag("category_chip_$catKey")
                        )
                    }
                }
            }

            // --- CONTENT AREA (LIST vs MAP) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF81C784))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isTelugu) "సమీప నిజమైన వ్యవసాయ దుకాణాలను వెతుకుతోంది..." else "Searching real agricultural shops around location...",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (viewMode == ShopViewMode.LIST) {
                    // LIST VIEW
                    ShopsListView(
                        shops = shopsList,
                        radiusKm = radiusKm,
                        isTelugu = isTelugu,
                        onCall = { phone ->
                            if (!phone.isNull_or_empty()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isTelugu) "ఫోన్ నంబర్ లభ్యం కాలేదు. నేరుగా మార్గాన్ని చూడండి." else "Phone number unavailable for this shop. Use Directions.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onDirections = { shop ->
                            val uri = Uri.parse("geo:${shop.latitude},${shop.longitude}?q=${shop.latitude},${shop.longitude}(${Uri.encode(shop.name)})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                // Fallback browser directions
                                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${shop.latitude},${shop.longitude}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                            }
                        }
                    )
                } else {
                    // MAP VIEW
                    InteractiveComposeMapView(
                        centerLat = currentLat,
                        centerLng = currentLng,
                        radiusKm = radiusKm,
                        shops = shopsList,
                        selectedShop = selectedMapShop,
                        isTelugu = isTelugu,
                        onShopSelected = { shop -> shopsViewModel.selectMapShop(shop) },
                        onCall = { phone ->
                            if (!phone.isNull_or_empty()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Phone number unavailable", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDirections = { shop ->
                            val uri = Uri.parse("geo:${shop.latitude},${shop.longitude}?q=${shop.latitude},${shop.longitude}(${Uri.encode(shop.name)})")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    )
                }
            }
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.isBlank()

@Composable
fun ShopsListView(
    shops: List<AgriShopItem>,
    radiusKm: Int,
    isTelugu: Boolean,
    onCall: (String?) -> Unit,
    onDirections: (AgriShopItem) -> Unit
) {
    if (shops.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isTelugu) "ఈ పరిధిలో ($radiusKm km) వ్యవసాయ దుకాణాలు కనుగొనబడలేదు." else "No agricultural shops found within $radiusKm km.",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isTelugu) "దయచేసి పరిధిని 25km లేదా 50km కి పెంచండి." else "Please increase radius to 25 km or 50 km to find district dealers.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("shops_list_lazy_column")
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTelugu) "${shops.size} నిజమైన దుకాణాలు కనుగొనబడ్డాయి" else "${shops.size} Real Shops Found ($radiusKm km)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                    Text(
                        text = if (isTelugu) "దూరం ప్రకారం క్రమబద్ధీకరించబడింది" else "Sorted by Distance ⬆",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            items(shops) { shop ->
                ShopItemCard(
                    shop = shop,
                    isTelugu = isTelugu,
                    onCall = { onCall(shop.phone) },
                    onDirections = { onDirections(shop) }
                )
            }
        }
    }
}

@Composable
fun ShopItemCard(
    shop: AgriShopItem,
    isTelugu: Boolean,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    val categoryColor = when (shop.category) {
        "Fertilizer" -> Color(0xFF29B6F6)
        "Seed" -> Color(0xFF81C784)
        "Pesticide" -> Color(0xFFEF5350)
        "Machinery" -> Color(0xFFFFB74D)
        "Equipment" -> Color(0xFFAB47BC)
        else -> Color(0xFF26A69A)
    }

    val displayTitle = if (isTelugu && shop.nameTe.isNotBlank()) shop.nameTe else shop.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_card_${shop.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13281B)),
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Category Badge + Distance Badge + Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = shop.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${shop.distanceKm} km away",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (shop.rating != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD54F).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${shop.rating}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    }
                }
            }

            // Title & Address
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = displayTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = shop.address,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Info Details (Phone & Hours)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = shop.phone ?: if (isTelugu) "లభ్యం కా లేదు" else "Not Listed",
                        fontSize = 11.sp,
                        color = if (shop.phone != null) Color.White else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = shop.openingHours ?: "8:00 AM - 8:00 PM",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Action Buttons (Call & Get Directions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("call_shop_button_${shop.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!shop.phone.isNull_or_empty()) Color(0xFF2E7D32) else Color.Gray.copy(alpha = 0.4f)
                    ),
                    enabled = !shop.phone.isNull_or_empty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTelugu) "కాల్ చేయండి" else "Call Shop",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDirections,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("get_directions_button_${shop.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF81C784)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81C784))
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTelugu) "దారి చూపించు" else "Get Directions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveComposeMapView(
    centerLat: Double,
    centerLng: Double,
    radiusKm: Int,
    shops: List<AgriShopItem>,
    selectedShop: AgriShopItem?,
    isTelugu: Boolean,
    onShopSelected: (AgriShopItem) -> Unit,
    onCall: (String?) -> Unit,
    onDirections: (AgriShopItem) -> Unit
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("interactive_map_view_container")
    ) {
        // Interactive Canvas Rendering Map
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF091F13))
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val center = Offset(width / 2f, height / 2f)

                        // Find closest shop pin to tap
                        var closestShop: AgriShopItem? = null
                        var minTapDist = 60f

                        shops.forEach { shop ->
                            val latDiff = shop.latitude - centerLat
                            val lngDiff = shop.longitude - centerLng
                            val scale = (min(width, height) / (radiusKm * 2.2f)) * zoomLevel

                            val pinX = center.x + (lngDiff * 111.0 * scale).toFloat()
                            val pinY = center.y - (latDiff * 111.0 * scale).toFloat()

                            val dx = tapOffset.x - pinX
                            val dy = tapOffset.y - pinY
                            val tapDist = kotlin.math.sqrt(dx * dx + dy * dy)

                            if (tapDist < minTapDist) {
                                minTapDist = tapDist
                                closestShop = shop
                            }
                        }

                        closestShop?.let { onShopSelected(it) }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height / 2f)
            val minDim = min(width, height)

            // Draw Map Grid Lines
            val gridSpacing = 60f
            for (x in 0..(width / gridSpacing).toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(x * gridSpacing, 0f),
                    end = Offset(x * gridSpacing, height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(height / gridSpacing).toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y * gridSpacing),
                    end = Offset(width, y * gridSpacing),
                    strokeWidth = 1f
                )
            }

            // Draw Search Radius Circles (e.g. 5km, 10km, 25km)
            val scale = (minDim / (radiusKm * 2.2f)) * zoomLevel
            val radiusPx = radiusKm * scale

            drawCircle(
                color = Color(0xFF81C784).copy(alpha = 0.08f),
                radius = radiusPx,
                center = center
            )
            drawCircle(
                color = Color(0xFF81C784).copy(alpha = 0.3f),
                radius = radiusPx,
                center = center,
                style = Stroke(width = 2f)
            )

            // Draw User GPS / Test Location Center Pin
            drawCircle(
                color = Color(0xFF29B6F6).copy(alpha = 0.2f),
                radius = 28f,
                center = center
            )
            drawCircle(
                color = Color(0xFF29B6F6),
                radius = 10f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = center
            )

            // Draw Shop Markers
            shops.forEach { shop ->
                val latDiff = shop.latitude - centerLat
                val lngDiff = shop.longitude - centerLng

                val pinX = center.x + (lngDiff * 111.0 * scale).toFloat()
                val pinY = center.y - (latDiff * 111.0 * scale).toFloat()
                val pinOffset = Offset(pinX, pinY)

                val isSelected = selectedShop?.id == shop.id
                val pinColor = when (shop.category) {
                    "Fertilizer" -> Color(0xFF29B6F6)
                    "Seed" -> Color(0xFF81C784)
                    "Pesticide" -> Color(0xFFEF5350)
                    "Machinery" -> Color(0xFFFFB74D)
                    else -> Color(0xFFAB47BC)
                }

                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = 20f,
                        center = pinOffset
                    )
                    drawCircle(
                        color = pinColor,
                        radius = 16f,
                        center = pinOffset
                    )
                } else {
                    drawCircle(
                        color = pinColor,
                        radius = 12f,
                        center = pinOffset
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.5f),
                        radius = 12f,
                        center = pinOffset,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }

        // Map Overlay Info Badge (Top Left)
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF29B6F6), CircleShape))
                Text(
                    text = if (isTelugu) "కేంద్రం (సురేపల్లె / GPS)" else "Center: $radiusKm km Radius",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Map Zoom Controls (Top Right)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomLevel = (zoomLevel * 1.25f).coerceAtMost(2.5f) },
                modifier = Modifier.size(36.dp),
                containerColor = Color.Black.copy(alpha = 0.8f),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            FloatingActionButton(
                onClick = { zoomLevel = (zoomLevel / 1.25f).coerceAtLeast(0.6f) },
                modifier = Modifier.size(36.dp),
                containerColor = Color.Black.copy(alpha = 0.8f),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }
        }

        // Selected Shop Preview Callout Sheet (Bottom Overlay)
        selectedShop?.let { shop ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                ShopItemCard(
                    shop = shop,
                    isTelugu = isTelugu,
                    onCall = { onCall(shop.phone) },
                    onDirections = { onDirections(shop) }
                )
            }
        }
    }
}
