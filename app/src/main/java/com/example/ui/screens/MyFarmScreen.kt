package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FarmEntity
import com.example.ui.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFarmScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit = {}
) {
    val farms by viewModel.allFarms.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var isAddFarmOpen by remember { mutableStateOf(false) }

    // Dialog form states
    var crop by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var soil by remember { mutableStateOf("") }
    var plantingDate by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("My Registered Farms", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { isAddFarmOpen = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Farm", tint = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        if (farms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Landscape,
                        contentDescription = "Empty Farms",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Farms Registered Yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add your crops to configure daily reminders and track expected harvests.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { isAddFarmOpen = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Register")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Register My First Farm")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(farms) { farm ->
                    FarmCard(
                        farm = farm,
                        viewModel = viewModel,
                        onDelete = { viewModel.removeFarm(farm) },
                        onNavigateToPremium = onNavigateToPremium
                    )
                }
            }
        }
    }

    // Add Farm Modal Dialog
    if (isAddFarmOpen) {
        Dialog(onDismissRequest = { isAddFarmOpen = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Register New Farm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    OutlinedTextField(
                        value = crop,
                        onValueChange = { crop = it },
                        label = { Text("Crop Name (e.g. Paddy, Cotton)") },
                        leadingIcon = { Icon(Icons.Default.Spa, contentDescription = "Crop") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Farm Area (e.g. 3 Acres)") },
                        leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = "Area") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = soil,
                        onValueChange = { soil = it },
                        label = { Text("Soil Type (e.g. Black Soil, Sandy)") },
                        leadingIcon = { Icon(Icons.Default.Landscape, contentDescription = "Soil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = plantingDate,
                        onValueChange = { plantingDate = it },
                        label = { Text("Planting Date (e.g. 15 June)") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Planting") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = harvestDate,
                        onValueChange = { harvestDate = it },
                        label = { Text("Expected Harvest Date (e.g. Oct)") },
                        leadingIcon = { Icon(Icons.Default.Agriculture, contentDescription = "Harvest") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isAddFarmOpen = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (crop.isNotBlank() && area.isNotBlank()) {
                                    viewModel.addFarm(crop, area, soil, plantingDate, harvestDate)
                                    // Clear form
                                    crop = ""
                                    area = ""
                                    soil = ""
                                    plantingDate = ""
                                    harvestDate = ""
                                    isAddFarmOpen = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_farm_button")
                        ) {
                            Text("Save Farm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FarmCard(
    farm: FarmEntity,
    viewModel: AgriViewModel,
    onDelete: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var waterReminder by remember { mutableStateOf(farm.irrigationReminder) }
    var fertReminder by remember { mutableStateOf(farm.fertilizerReminder) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Landscape,
                            contentDescription = "Farm",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${farm.crop} Farm",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${farm.area} • ${farm.soil} Soil",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Farm", tint = Color.Red.copy(alpha = 0.8f))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

            // Planting & Harvest Timelines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "PLANTING TIMELINE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = farm.plantingDate.ifBlank { "N/A" }, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "EXPECTED HARVEST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = farm.expectedHarvest.ifBlank { "N/A" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

            // Smart reminders toggles
            Text(
                text = "Smart Reminders & Schedules",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Irrigation reminder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        .clickable { waterReminder = !waterReminder }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Checkbox(
                        checked = waterReminder,
                        onCheckedChange = { waterReminder = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Water Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                // Fertilizer reminder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        .clickable { fertReminder = !fertReminder }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Checkbox(
                        checked = fertReminder,
                        onCheckedChange = { fertReminder = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Fertilizer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

            var showPredictionDialog by remember { mutableStateOf(false) }
            var showPremiumLockedDialog by remember { mutableStateOf(false) }
            val isPredicting by viewModel.isPredictingYield.collectAsState()
            val predictionResult by viewModel.yieldPrediction.collectAsState()

            Button(
                onClick = {
                    if (currentUser?.isPremium == true) {
                        showPredictionDialog = true
                        viewModel.predictCropYield(
                            crop = farm.crop,
                            area = farm.area,
                            soil = farm.soil,
                            plantingDate = farm.plantingDate,
                            state = "Andhra Pradesh",
                            district = "Eluru"
                        )
                    } else {
                        showPremiumLockedDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = if (currentUser?.isPremium == true) Icons.Default.TrendingUp else Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentUser?.isPremium == true) "Predict Yield for this Farm" else "Predict Yield (PREMIUM)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showPremiumLockedDialog) {
                Dialog(onDismissRequest = { showPremiumLockedDialog = false }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Premium Feature",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                text = "Premium AI Yield Forecast",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Crop yield prediction is powered by Gemini and premium historical soil telemetry. Upgrade to Premium for unlimited forecasts and precise season diagnostics.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showPremiumLockedDialog = false },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Maybe Later", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        showPremiumLockedDialog = false
                                        onNavigateToPremium()
                                    },
                                    modifier = Modifier.weight(2f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Upgrade Now", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            if (showPredictionDialog) {
                Dialog(onDismissRequest = { 
                    showPredictionDialog = false
                    viewModel.clearYieldPrediction()
                }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Predicting Yield: ${farm.crop}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                            if (isPredicting) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("AI Calculating Yield Forecast...", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            } else {
                                Text(
                                    text = predictionResult ?: "Predicting yield...",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = { 
                                    showPredictionDialog = false
                                    viewModel.clearYieldPrediction()
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}
