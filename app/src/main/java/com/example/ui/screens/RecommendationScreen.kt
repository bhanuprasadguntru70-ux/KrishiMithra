package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AgriViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Crop Recommendation, 1 = Fertilizer, 2 = Yield Predictor
    val tabs = listOf("Crop Recommendation", "Fertilizer Advice", "Yield Predictor")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Smart AI Advisor", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        // Tab selection
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> CropRecommendationForm(viewModel = viewModel)
            1 -> FertilizerRecommendationForm(viewModel = viewModel)
            2 -> YieldPredictorForm(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationForm(viewModel: AgriViewModel) {
    var soilType by remember { mutableStateOf("Red Sandy Clay") }
    var stateInput by remember { mutableStateOf("Andhra Pradesh") }
    var districtInput by remember { mutableStateOf("Anantapur") }
    var seasonInput by remember { mutableStateOf("Kharif (Monsoon)") }
    var waterAvailability by remember { mutableStateOf("Canal Irrigation & Rainfed") }
    var farmSize by remember { mutableStateOf("2.5 Acres") }

    val isGenerating by viewModel.isGeneratingCrop.collectAsState()
    val cropResult by viewModel.cropRecommendation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Farm Soil & Field Analysis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Soil Type") },
                    leadingIcon = { Icon(Icons.Default.Landscape, contentDescription = "Soil") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = districtInput,
                        onValueChange = { districtInput = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stateInput,
                        onValueChange = { stateInput = it },
                        label = { Text("State") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = seasonInput,
                    onValueChange = { seasonInput = it },
                    label = { Text("Farming Season") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Season") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = waterAvailability,
                    onValueChange = { waterAvailability = it },
                    label = { Text("Water Source / Availability") },
                    leadingIcon = { Icon(Icons.Default.Water, contentDescription = "Water") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = farmSize,
                    onValueChange = { farmSize = it },
                    label = { Text("Farm Size") },
                    leadingIcon = { Icon(Icons.Default.SquareFoot, contentDescription = "Size") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.generateCropRecommendation(soilType, stateInput, districtInput, seasonInput, waterAvailability, farmSize)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(top = 8.dp)
                        .testTag("get_crop_recommendation_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("AI Consulting Agronomists...")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Crop Advisory", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Output Result card
        AnimatedVisibility(visible = cropResult != null) {
            cropResult?.let { advice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = "Verified Result",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Crop Recommendation Report",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                        Text(
                            text = advice,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerRecommendationForm(viewModel: AgriViewModel) {
    var cropName by remember { mutableStateOf("Paddy (Rice)") }
    var diseaseDeficiency by remember { mutableStateOf("Leaf Yellowing (Nitrogen Deficiency)") }
    var soilType by remember { mutableStateOf("Clay Loam") }
    var growthStage by remember { mutableStateOf("Tillering Stage") }

    val isGenerating by viewModel.isGeneratingFertilizer.collectAsState()
    val fertilizerResult by viewModel.fertilizerRecommendation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Crop & Nutrient Analysis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text("Active Crop") },
                    leadingIcon = { Icon(Icons.Default.Spa, contentDescription = "Crop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = diseaseDeficiency,
                    onValueChange = { diseaseDeficiency = it },
                    label = { Text("Observed Disease / Deficiency") },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = "Deficiency") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Soil Type") },
                    leadingIcon = { Icon(Icons.Default.Landscape, contentDescription = "Soil") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = growthStage,
                    onValueChange = { growthStage = it },
                    label = { Text("Crop Growth Stage") },
                    leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = "Stage") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.generateFertilizerRecommendation(cropName, diseaseDeficiency, soilType, growthStage)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(top = 8.dp)
                        .testTag("get_fertilizer_recommendation_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Gemini Calculating NPK Ratios...")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recommend Fertilizer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Output Result card
        AnimatedVisibility(visible = fertilizerResult != null) {
            fertilizerResult?.let { advice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Result",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Fertilizer Application Plan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                        Text(
                            text = advice,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YieldPredictorForm(viewModel: AgriViewModel) {
    val farms by viewModel.allFarms.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var cropInput by remember { mutableStateOf("") }
    var areaInput by remember { mutableStateOf("") }
    var soilInput by remember { mutableStateOf("") }
    var plantingDateInput by remember { mutableStateOf("") }
    var stateInput by remember { mutableStateOf(currentUser?.state ?: "Andhra Pradesh") }
    var districtInput by remember { mutableStateOf(currentUser?.district ?: "Eluru") }

    val isPredicting by viewModel.isPredictingYield.collectAsState()
    val predictionResult by viewModel.yieldPrediction.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Yield Prediction Parameters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                if (farms.isNotEmpty()) {
                    Text(
                        text = "Load data from registered farms:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 6.dp)
                    ) {
                        items(farms) { farm ->
                            SuggestionChip(
                                onClick = {
                                    cropInput = farm.crop
                                    areaInput = farm.area
                                    soilInput = farm.soil
                                    plantingDateInput = farm.plantingDate
                                },
                                label = { Text("${farm.crop} (${farm.area})") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Landscape,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                }

                OutlinedTextField(
                    value = cropInput,
                    onValueChange = { cropInput = it },
                    label = { Text("Crop Name (e.g. Paddy, Cotton)") },
                    leadingIcon = { Icon(Icons.Default.Spa, contentDescription = "Crop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = areaInput,
                        onValueChange = { areaInput = it },
                        label = { Text("Farm Area (Acre)") },
                        leadingIcon = { Icon(Icons.Default.SquareFoot, contentDescription = "Area") },
                        singleLine = true,
                        modifier = Modifier.weight(1.1f)
                    )
                    OutlinedTextField(
                        value = soilInput,
                        onValueChange = { soilInput = it },
                        label = { Text("Soil Type") },
                        leadingIcon = { Icon(Icons.Default.Landscape, contentDescription = "Soil") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = plantingDateInput,
                    onValueChange = { plantingDateInput = it },
                    label = { Text("Sowing/Planting Date (e.g., June 15)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Planting Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = districtInput,
                        onValueChange = { districtInput = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stateInput,
                        onValueChange = { stateInput = it },
                        label = { Text("State") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (predictionResult != null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearYieldPrediction()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear")
                        }
                    }

                    Button(
                        onClick = {
                            if (cropInput.isNotBlank() && areaInput.isNotBlank() && soilInput.isNotBlank()) {
                                viewModel.predictCropYield(
                                    crop = cropInput,
                                    area = areaInput,
                                    soil = soilInput,
                                    plantingDate = plantingDateInput,
                                    state = stateInput,
                                    district = districtInput
                                )
                            }
                        },
                        enabled = cropInput.isNotBlank() && areaInput.isNotBlank() && soilInput.isNotBlank(),
                        modifier = Modifier.weight(2f).height(50.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isPredicting) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Predicting...")
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Predict")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Predict Yield", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Output Result card
        AnimatedVisibility(visible = predictionResult != null) {
            predictionResult?.let { prediction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Yield Prediction Result",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Crop Yield Prediction Report",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                        Text(
                            text = prediction,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
