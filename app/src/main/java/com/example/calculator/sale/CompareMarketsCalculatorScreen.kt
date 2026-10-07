package com.example.calculator.sale

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*
import com.example.calculator.util.IndianFormatter

data class MarketComparisonItem(
    val id: String,
    var marketName: String,
    var price: String,
    var distanceKm: String,
    var transportCost: String
)

@Composable
fun CompareMarketsCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var quantityInput by remember { mutableStateOf("30") } // quintals

    var marketA by remember { mutableStateOf(MarketComparisonItem("A", "Market A (Local Mandi)", "2450", "15", "1200")) }
    var marketB by remember { mutableStateOf(MarketComparisonItem("B", "Market B (District Yard)", "2600", "45", "3500")) }
    var marketC by remember { mutableStateOf(MarketComparisonItem("C", "Market C (State Hub)", "2720", "85", "6800")) }

    var calculatedResults by remember { mutableStateOf<List<Triple<MarketComparisonItem, Double, Double>>?>(null) } // Item, Gross, Net
    var bestMarketId by remember { mutableStateOf("") }

    fun calculate() {
        val qty = quantityInput.toDoubleOrNull() ?: 0.0
        if (qty <= 0) return

        val markets = listOf(marketA, marketB, marketC)
        val results = markets.map { m ->
            val p = m.price.toDoubleOrNull() ?: 0.0
            val t = m.transportCost.toDoubleOrNull() ?: 0.0
            val gross = qty * p
            val net = gross - t
            Triple(m, gross, net)
        }

        val maxNet = results.maxOfOrNull { it.third } ?: 0.0
        val winner = results.find { it.third == maxNet && it.third > 0 }

        calculatedResults = results
        bestMarketId = winner?.first?.id ?: ""
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Compare Markets Calculator",
                titleTe = "మార్కెట్ల పోలిక & ఉత్తమ ధర లెక్కలు",
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

            CalculatorNumberInput(
                value = quantityInput,
                onValueChange = { quantityInput = it },
                labelEn = "Total Produce Quantity (Quintals)",
                labelTe = "మొత్తం పంట పరిమాణం (క్వింటాళ్ళు)",
                appLanguage = appLanguage,
                suffixText = "quintals"
            )

            val renderMarketInputBlock = @Composable { item: MarketComparisonItem, label: String, onUpdate: (MarketComparisonItem) -> Unit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CalculatorNumberInput(
                                value = item.price,
                                onValueChange = { onUpdate(item.copy(price = it)) },
                                labelEn = "Price (₹/quintal)",
                                labelTe = "ధర (₹/క్వింటాలు)",
                                appLanguage = appLanguage,
                                prefixSymbol = "₹",
                                modifier = Modifier.weight(1f)
                            )

                            CalculatorNumberInput(
                                value = item.transportCost,
                                onValueChange = { onUpdate(item.copy(transportCost = it)) },
                                labelEn = "Transport Cost (₹)",
                                labelTe = "రవాణా ఖర్చు (₹)",
                                appLanguage = appLanguage,
                                prefixSymbol = "₹",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            renderMarketInputBlock(marketA, if (appLanguage == "te") "మార్కెట్ 1 (స్థానిక మండి)" else "Market 1") { marketA = it }
            renderMarketInputBlock(marketB, if (appLanguage == "te") "మార్కెట్ 2 (జిల్లా యార్డ్)" else "Market 2") { marketB = it }
            renderMarketInputBlock(marketC, if (appLanguage == "te") "మార్కెట్ 3 (రాష్ట్ర మార్కెట్)" else "Market 3") { marketC = it }

            CalculateButtonRow(
                onReset = {
                    quantityInput = ""
                    calculatedResults = null
                },
                onCalculate = { calculate() },
                appLanguage = appLanguage
            )

            calculatedResults?.let { results ->
                Text(
                    text = if (appLanguage == "te") "మార్కెట్ల నికర ఫలితాల పోలిక" else "Net Return Comparison Results",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                results.forEach { (m, gross, net) ->
                    val isBest = m.id == bestMarketId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isBest) 2.dp else 0.dp,
                                color = if (isBest) Color(0xFF2E7D32) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBest) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = m.marketName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (isBest) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF2E7D32)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (appLanguage == "te") "ఉత్తమ రాబడి మార్కెట్!" else "BEST NET RETURN!",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = if (appLanguage == "te") "రైతు నికర రాబడి" else "Net Estimated Return", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = IndianFormatter.formatCurrency(net),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isBest) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Gross: ${IndianFormatter.formatCurrency(gross)}", fontSize = 11.sp, color = Color.DarkGray)
                                    Text(text = "Transport: -${IndianFormatter.formatCurrency(m.transportCost.toDoubleOrNull() ?: 0.0)}", fontSize = 11.sp, color = Color.Red.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
