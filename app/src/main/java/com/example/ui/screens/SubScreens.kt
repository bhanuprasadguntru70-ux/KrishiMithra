package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MandiCropRate
import com.example.ui.theme.KrishiAccentOrange
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

import com.example.ui.components.CropPriceTrendChart
import com.example.ui.components.PricePoint

// MARKETS TAB
@Composable
fun LiveMandiRatesTabScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Verified Mandi Prices & Market Trends",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = KrishiHeaderGreen
                )
                Text(
                    text = "Official APMC rates & 7-day price analytics updated hourly",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Filter crop or market name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.filteredMandiRates) { crop ->
                MandiDetailCard(crop)
            }
        }
    }
}

@Composable
fun MandiDetailCard(crop: MandiCropRate) {
    var isExpanded by remember { mutableStateOf(false) }

    // Generate price trend history for chart based on current modalPrice
    val base = crop.modalPrice
    val history7d = remember(crop.id) {
        listOf(
            PricePoint("30 Jul", base * 0.94),
            PricePoint("31 Jul", base * 0.96),
            PricePoint("01 Aug", base * 0.95),
            PricePoint("02 Aug", base * 0.98),
            PricePoint("03 Aug", base * 0.97),
            PricePoint("04 Aug", base * 0.99),
            PricePoint("05 Aug", base)
        )
    }

    val history30d = remember(crop.id) {
        listOf(
            PricePoint("07 Jul", base * 0.88),
            PricePoint("12 Jul", base * 0.90),
            PricePoint("17 Jul", base * 0.92),
            PricePoint("22 Jul", base * 0.91),
            PricePoint("27 Jul", base * 0.95),
            PricePoint("01 Aug", base * 0.97),
            PricePoint("05 Aug", base)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
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
                    if (crop.imageResId != null) {
                        Image(
                            painter = painterResource(id = crop.imageResId),
                            contentDescription = crop.commodity,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(38.dp).clip(CircleShape)
                        )
                    } else {
                        Text("🌾", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(crop.commodity, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KrishiHeaderGreen)
                        Text("${crop.variety} • ${crop.market}", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${crop.modalPrice.toInt()}/qtl",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = KrishiHeaderGreen
                    )
                    Text(
                        text = "🟢 Verified APMC",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiPrimaryGreen
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Min: ₹${crop.minPrice.toInt()}", fontSize = 12.sp, color = Color.Gray)
                Text("Max: ₹${crop.maxPrice.toInt()}", fontSize = 12.sp, color = Color.Gray)
                Text("Arrival: ${crop.arrivalQty}", fontSize = 12.sp, color = KrishiHeaderGreen, fontWeight = FontWeight.Bold)

                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Chart ▲" else "Price Chart 📈",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiHeaderGreen
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                CropPriceTrendChart(
                    cropName = crop.commodity,
                    currentPrice = crop.modalPrice,
                    priceHistory7d = history7d,
                    priceHistory30d = history30d
                )
            }
        }
    }
}

// SELL TAB
@Composable
fun SellCropTabScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Farmer Crop Marketplace", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = KrishiHeaderGreen)
                Text("Connect directly with verified buyers", fontSize = 12.sp, color = Color.Gray)
            }

            Button(
                onClick = { viewModel.toggleSellCropModal(true) },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("+ Post Crop")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(uiState.cropListings) { listing ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(listing.cropName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KrishiHeaderGreen)
                                Text("Seller: ${listing.sellerName} (${listing.location})", fontSize = 12.sp, color = Color.DarkGray)
                            }
                            Text("₹${listing.pricePerQtl.toInt()}/qtl", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = KrishiAccentOrange)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quantity: ${listing.quantityQtl} Quintals", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = KrishiPrimaryGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Seller", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// MY BOOK TAB is now implemented in MoneyBookTabScreen.kt


// PROFILE TAB
@Composable
fun ProfileTabScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    val agriViewModel: com.example.ui.AgriViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    ProfileScreen(
        viewModel = agriViewModel,
        onBack = { viewModel.selectTab(0) },
        onAdminClick = {},
        onPremiumClick = {},
        onLogout = { viewModel.selectTab(0) }
    )
}
