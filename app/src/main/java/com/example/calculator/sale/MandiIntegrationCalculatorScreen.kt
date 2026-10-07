package com.example.calculator.sale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*
import com.example.calculator.util.IndianFormatter
import com.example.data.model.MarketCropEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MandiIntegrationCalculatorScreen(
    marketCrops: List<MarketCropEntity>,
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onNavigateToSaleCalc: (cropName: String, price: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedCropName by remember { mutableStateOf(marketCrops.firstOrNull()?.cropName ?: "Maize") }
    var selectedMarketName by remember { mutableStateOf(marketCrops.firstOrNull()?.market ?: "Guntur Mandi") }
    var quantityInput by remember { mutableStateOf("10") } // quintals

    val selectedCropEntity = remember(selectedCropName, selectedMarketName, marketCrops) {
        marketCrops.find { it.cropName.equals(selectedCropName, ignoreCase = true) && it.market.equals(selectedMarketName, ignoreCase = true) }
            ?: marketCrops.find { it.cropName.equals(selectedCropName, ignoreCase = true) }
            ?: marketCrops.firstOrNull()
    }

    val availableCrops = remember(marketCrops) { marketCrops.map { it.cropName }.distinct() }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Live Mandi + Farm Calculator",
                titleTe = "మండి ధరల విక్రయ గణన",
                appLanguage = appLanguage,
                onLanguageSelected = onLanguageSelected,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (appLanguage == "te") "పంటను ఎంచుకోండి" else "Select Crop",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // Crop choices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCrops.take(4).forEach { crop ->
                    val isSelected = crop.equals(selectedCropName, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCropName = crop
                            selectedMarketName = marketCrops.find { it.cropName.equals(crop, ignoreCase = true) }?.market ?: ""
                        },
                        label = { Text(crop, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) { { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) } } else null
                    )
                }
            }

            if (selectedCropEntity != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedCropEntity.market,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF2E7D32)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "VERIFIED MANDI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = if (appLanguage == "te") "ప్రస్తుత మండి మోడల్ ధర" else "Latest Verified Price",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${selectedCropEntity.todayPrice.toInt()} / ${selectedCropEntity.unit}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "District: ${selectedCropEntity.district}\nUpdated: ${selectedCropEntity.lastUpdated}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CalculatorNumberInput(
                            value = quantityInput,
                            onValueChange = { quantityInput = it },
                            labelEn = "Your Crop Yield Quantity (${selectedCropEntity.unit}s)",
                            labelTe = "మీ వద్ద ఉన్న పంట పరిమాణం (${selectedCropEntity.unit}లు)",
                            appLanguage = appLanguage,
                            suffixText = selectedCropEntity.unit
                        )

                        val qty = quantityInput.toDoubleOrNull() ?: 0.0
                        val estGross = qty * selectedCropEntity.todayPrice

                        Spacer(modifier = Modifier.height(12.dp))

                        ResultCard(
                            mainResultTitleEn = "ESTIMATED PRODUCE VALUE",
                            mainResultTitleTe = "అంచనా ఉత్పత్తుల విలువ",
                            mainResultValue = IndianFormatter.formatCurrency(estGross),
                            appLanguage = appLanguage,
                            subtitleEn = "Based on Verified Live Market Rate",
                            subtitleTe = "లైవ్ ధృవీకరించిన మార్కెట్ ధర ఆధారంగా"
                        )

                        Button(
                            onClick = {
                                onNavigateToSaleCalc(selectedCropEntity.cropName, selectedCropEntity.todayPrice.toInt().toString())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage == "te") "రవాణా ఖర్చులతో పూర్తి లెక్క చేయండి" else "Calculate Net Return with Transport Costs",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (appLanguage == "te") "ప్రస్తుతం మార్కెట్ ధరలు అందుబాటులో లేవు." else "Market price unavailable right now.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
