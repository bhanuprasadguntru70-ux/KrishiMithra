package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FarmToolItem
import com.example.ui.theme.KrishiAccentOrange
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun LocationSelectorDialog(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    val locations = listOf(
        "Eluru, Andhra Pradesh",
        "Tadepalligudem, Andhra Pradesh",
        "Guntur, Andhra Pradesh",
        "Vijayawada, Andhra Pradesh",
        "Kurnool, Andhra Pradesh",
        "Visakhapatnam, Andhra Pradesh",
        "Anantapur, Andhra Pradesh",
        "Warangal, Telangana",
        "Khammam, Telangana",
        "Nalgonda, Telangana"
    )

    AlertDialog(
        onDismissRequest = { viewModel.toggleLocationSelector(false) },
        title = { Text("Select Your Market / Location", fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) },
        text = {
            LazyColumn(modifier = Modifier.height(260.dp)) {
                items(locations) { loc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateLocation(loc) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.selectedLocation == loc,
                            onClick = { viewModel.updateLocation(loc) },
                            colors = RadioButtonDefaults.colors(selectedColor = KrishiHeaderGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(loc, fontSize = 14.sp, fontWeight = if (uiState.selectedLocation == loc) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.toggleLocationSelector(false) }) {
                Text("Close", color = KrishiHeaderGreen)
            }
        }
    )
}

@Composable
fun VoiceAssistantDialog(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.toggleVoiceAssistant(false) },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = KrishiPrimaryGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Telugu / English Voice Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(KrishiPrimaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = KrishiPrimaryGreen, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(uiState.voiceTranscript, fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Text(uiState.voiceResponse, modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = KrishiHeaderGreen)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.toggleVoiceAssistant(false) },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen)
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
fun NotificationsDialog(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    val alerts = listOf(
        "🔴 Tomato rate increased by ₹200/qtl in Kolar Market today!",
        "🌧️ Weather Alert: Light rain expected in West Godavari tomorrow morning.",
        "🏛️ PM-KISAN 17th Installment credit confirmation received."
    )

    AlertDialog(
        onDismissRequest = { viewModel.toggleNotifications(false) },
        title = { Text("Recent Alerts & Updates", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alerts.forEach { alert ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F5F1))) {
                        Text(alert, modifier = Modifier.padding(10.dp), fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.toggleNotifications(false) }) {
                Text("Dismiss", color = KrishiHeaderGreen)
            }
        }
    )
}

@Composable
fun CalculatorToolModal(
    tool: FarmToolItem,
    onDismiss: () -> Unit
) {
    var val1 by remember { mutableStateOf("10") }
    var val2 by remember { mutableStateOf("1500") }
    var result by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(tool.name, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tool.description, fontSize = 12.sp, color = Color.Gray)

                OutlinedTextField(
                    value = val1,
                    onValueChange = { val1 = it },
                    label = { Text("Input 1 (e.g. Acres / Bags / Rate)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = val2,
                    onValueChange = { val2 = it },
                    label = { Text("Input 2 (e.g. Price per unit / Hours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val num1 = val1.toDoubleOrNull() ?: 0.0
                        val num2 = val2.toDoubleOrNull() ?: 0.0
                        val calc = num1 * num2
                        result = "Total Calculated Amount: ₹${String.format("%.2f", calc)}"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calculate Now")
                }

                if (result != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                        Text(result!!, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = KrishiHeaderGreen)
            }
        }
    )
}

@Composable
fun AgriDoctorModal(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.toggleAgriDoctor(false) },
        title = { Text("AI Crop Health Doctor 🍃", fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Take or upload a leaf photo to diagnose crop diseases automatically.", fontSize = 12.sp, color = Color.Gray)

                Button(
                    onClick = { viewModel.scanLeafImage() },
                    colors = ButtonDefaults.buttonColors(containerColor = KrishiPrimaryGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simulate Leaf Photo Scan")
                }

                if (uiState.isScanningLeaf) {
                    CircularProgressIndicator(color = KrishiPrimaryGreen)
                } else if (uiState.diseaseScanResult != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))) {
                        Text(uiState.diseaseScanResult!!, modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.toggleAgriDoctor(false) }) {
                Text("Done", color = KrishiHeaderGreen)
            }
        }
    )
}

@Composable
fun SellCropModal(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    var cropName by remember { mutableStateOf("Tomato") }
    var qtl by remember { mutableStateOf("25") }
    var price by remember { mutableStateOf("1620") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { viewModel.toggleSellCropModal(false) },
        title = { Text("Post Crop For Sale 🌱", fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = cropName, onValueChange = { cropName = it }, label = { Text("Crop Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qtl, onValueChange = { qtl = it }, label = { Text("Quantity (Quintals)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Asking Price (₹/Quintal)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.postCropListing(cropName, qtl.toIntOrNull() ?: 10, price.toDoubleOrNull() ?: 1000.0, phone)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen)
            ) {
                Text("Publish Post")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.toggleSellCropModal(false) }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddBookEntryModal(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("Fertilizers") }
    var cropName by remember { mutableStateOf("Paddy") }
    var fieldName by remember { mutableStateOf("North Field (2.5 Ac)") }
    var notes by remember { mutableStateOf("") }

    val categories = if (isIncome) {
        listOf("Crop Sale", "Subsidies & Govt", "Custom Hire Return", "Other Income")
    } else {
        listOf("Seeds", "Fertilizers", "Pesticides", "Labour Wage", "Tractor & Diesel", "Irrigation", "Equipment", "Other Expense")
    }

    val crops = listOf("Paddy", "Tomato", "Chilli", "Cotton", "Maize", "Onion", "General / All")
    val fields = listOf("North Field (2.5 Ac)", "South Field (2.0 Ac)", "East Field (1.5 Ac)", "Main Farm", "General")

    AlertDialog(
        onDismissRequest = { viewModel.toggleAddBookEntry(false) },
        title = { Text("Add Ledger Entry 📓", fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEAF5ED))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { isIncome = false; category = "Fertilizers" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isIncome) Color(0xFFC62828) else Color.Transparent,
                                contentColor = if (!isIncome) Color.White else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Expense (-)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { isIncome = true; category = "Crop Sale" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncome) KrishiHeaderGreen else Color.Transparent,
                                contentColor = if (isIncome) Color.White else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Income (+)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Description (e.g. DAP Purchase)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    Text("Crop Name:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        crops.take(4).forEach { crp ->
                            FilterChip(
                                selected = cropName == crp,
                                onClick = { cropName = crp },
                                label = { Text(crp, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    Text("Field / Plot:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        fields.take(3).forEach { fld ->
                            FilterChip(
                                selected = fieldName == fld,
                                onClick = { fieldName = fld },
                                label = { Text(fld.take(12), fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0) {
                        viewModel.addMoneyBookEntry(
                            title = title,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            isIncome = isIncome,
                            category = category,
                            cropName = cropName,
                            fieldName = fieldName,
                            notes = notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.toggleAddBookEntry(false) }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WeatherDetailModal(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.toggleWeatherDetail(false) },
        title = { Text("Weather Forecast ⛅", fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(uiState.selectedLocation, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Temperature: ${uiState.weather.tempC}°C • ${uiState.weather.condition}", fontSize = 14.sp)
                Text("Humidity: ${uiState.weather.humidity}% • Wind: ${uiState.weather.windSpeedKm} km/h", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Text(uiState.weather.advisory, modifier = Modifier.padding(10.dp), fontSize = 12.sp, color = KrishiHeaderGreen)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.toggleWeatherDetail(false) }) {
                Text("Close", color = KrishiHeaderGreen)
            }
        }
    )
}
