package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketCropEntity
import com.example.ui.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    var isAdminLoggedIn by remember { mutableStateOf(false) }
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    val allCrops by viewModel.allMarketCrops.collectAsState()
    val allNews by viewModel.allNews.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Analytics, 1 = Prices, 2 = News

    if (!isAdminLoggedIn) {
        // Admin Login View
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.DarkGray,
                            Color.Black
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Logo",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = "Agri AI Admin Console",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Manager Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )

                        if (loginError != null) {
                            Text(text = loginError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Admin Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Secret Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (adminEmail == "admin@agri.ai" && adminPassword == "admin123") {
                                    isAdminLoggedIn = true
                                    loginError = null
                                } else {
                                    loginError = "Invalid administrative credentials."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("Sign In to Panel", color = Color.White)
                        }
                    }
                }

                TextButton(onClick = onBack) {
                    Text("Return to App Home", color = Color.LightGray)
                }
            }
        }
    } else {
        // Real Admin Console Dashboard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = { Text("Admin Panel Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { isAdminLoggedIn = false }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log out")
                    }
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            // Primary sub tabs selector
            PrimaryTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = activeSubTab == 0, onClick = { activeSubTab = 0 }, text = { Text("Analytics") })
                Tab(selected = activeSubTab == 1, onClick = { activeSubTab = 1 }, text = { Text("Prices") })
                Tab(selected = activeSubTab == 2, onClick = { activeSubTab = 2 }, text = { Text("News") })
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (activeSubTab) {
                0 -> AdminAnalyticsPanel(allCrops.size, allNews.size)
                1 -> AdminMandiPricesPanel(allCrops, onUpdateCrop = { viewModel.updateMarketCrop(it) })
                2 -> AdminNewsFeedPanel(onCreateNews = { t, c, cat -> viewModel.createAdminNews(t, c, cat) })
            }
        }
    }
}

@Composable
fun AdminAnalyticsPanel(cropCount: Int, newsCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Real-Time Cloud Diagnostics", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard("MANDIS", cropCount.toString(), Icons.Default.Storefront, Modifier.weight(1f))
            StatCard("NEWS ARTICLES", newsCount.toString(), Icons.Default.Article, Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard("SYSTEM STATUS", "Active (Green)", Icons.Default.VerifiedUser, Modifier.weight(1f))
            StatCard("REGISTRATIONS", "4,250", Icons.Default.People, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large analytics progress summary
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gemini AI Usage Volumes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LinearProgressIndicator(progress = { 0.65f }, modifier = Modifier.fillMaxWidth().height(8.dp))
                Text("6,420 / 10,000 requests processed successfully today (No errors detected).", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminMandiPricesPanel(
    crops: List<MarketCropEntity>,
    onUpdateCrop: (MarketCropEntity) -> Unit
) {
    var selectedCropToEdit by remember { mutableStateOf<MarketCropEntity?>(null) }
    var editPriceString by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Adjust Mandi Pricing Index", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        crops.forEach { crop ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = crop.cropName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "${crop.market} • Today: ₹${crop.todayPrice.toInt()}", fontSize = 12.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            selectedCropToEdit = crop
                            editPriceString = crop.todayPrice.toString()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Update Price", fontSize = 11.sp)
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedCropToEdit != null) {
            selectedCropToEdit?.let { target ->
                Card(
                    modifier = Modifier.padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Edit Price: ${target.cropName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = editPriceString,
                            onValueChange = { editPriceString = it },
                            label = { Text("New Price (₹ / Quintal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { selectedCropToEdit = null }, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    val newPrice = editPriceString.toDoubleOrNull() ?: target.todayPrice
                                    // Update shifting pricing logs
                                    onUpdateCrop(
                                        target.copy(
                                            yesterdayPrice = target.todayPrice,
                                            todayPrice = newPrice,
                                            highestPrice = maxOf(target.highestPrice, newPrice),
                                            lowestPrice = minOf(target.lowestPrice, newPrice)
                                        )
                                    )
                                    selectedCropToEdit = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Price")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNewsFeedPanel(
    onCreateNews: (String, String, String) -> Unit
) {
    var newsTitle by remember { mutableStateOf("") }
    var newsContent by remember { mutableStateOf("") }
    var newsCategory by remember { mutableStateOf("News") }

    val categories = listOf("News", "Government Scheme", "Weather Alert")
    var catExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "Publish Advisory Content", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newsTitle,
                    onValueChange = { newsTitle = it },
                    label = { Text("Advisory Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown category
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    OutlinedTextField(
                        value = newsCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("News Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    newsCategory = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = newsContent,
                    onValueChange = { newsContent = it },
                    label = { Text("Publish Content / Advisory Copy") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (newsTitle.isNotBlank() && newsContent.isNotBlank()) {
                            onCreateNews(newsTitle, newsContent, newsCategory)
                            newsTitle = ""
                            newsContent = ""
                            newsCategory = "News"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Publish to Farmer Feeds", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
