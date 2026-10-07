package com.example.calculator.sale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*
import com.example.data.model.MandiCropRate
import com.example.ui.theme.KrishiAccentOrange
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen

@Composable
fun CropSaleCalculatorScreen(
    mandiRates: List<MandiCropRate>,
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var selectedCropRate by remember { mutableStateOf(mandiRates.firstOrNull()) }
    var quantityQtl by remember { mutableStateOf("25") }
    var mandiPricePerQtl by remember { mutableStateOf(selectedCropRate?.modalPrice?.toString() ?: "1620") }
    var transportCost by remember { mutableStateOf("1500") }
    var loadingUnloadingCost by remember { mutableStateOf("600") }
    var commissionPercent by remember { mutableStateOf("0") }

    var resultGrossAmount by remember { mutableStateOf<Double?>(null) }
    var resultTotalExpenses by remember { mutableStateOf<Double?>(null) }
    var resultNetAmount by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val qtl = quantityQtl.toDoubleOrNull()
        val price = mandiPricePerQtl.toDoubleOrNull()

        if (qtl == null || qtl <= 0.0 || price == null || price <= 0.0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన పరిమాణం మరియు క్వింటాల్ ధర నమోదు చేయండి." else "Please enter valid quantity and price per quintal."
            resultNetAmount = null
            return
        }

        val gross = qtl * price
        val trans = transportCost.toDoubleOrNull() ?: 0.0
        val loading = loadingUnloadingCost.toDoubleOrNull() ?: 0.0
        val commPct = commissionPercent.toDoubleOrNull() ?: 0.0
        val commAmt = (gross * commPct) / 100.0

        val totalExp = trans + loading + commAmt
        val net = gross - totalExp

        resultGrossAmount = gross
        resultTotalExpenses = totalExp
        resultNetAmount = net
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Crop Sale & Deduction Calculator 💰",
                titleTel = "పంట విక్రయం మినహాయింపుల లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        // Integrated Live Mandi Rate Picker
        if (mandiRates.isNotEmpty()) {
            item {
                Text("Auto-fill Verified Mandi Price (లైవ్ మార్కెట్ రేటు):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(mandiRates.size) { index ->
                        val crop = mandiRates[index]
                        FilterChip(
                            selected = selectedCropRate?.id == crop.id,
                            onClick = {
                                selectedCropRate = crop
                                mandiPricePerQtl = crop.modalPrice.toString()
                            },
                            label = { Text("${crop.commodity} (₹${crop.modalPrice.toInt()})", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = quantityQtl,
                    onValueChange = { quantityQtl = it },
                    labelEng = "Quantity",
                    labelTel = "పరిమాణం",
                    selectedLang = langState,
                    suffix = "Quintals",
                    modifier = Modifier.weight(1f)
                )
                CurrencyInput(
                    value = mandiPricePerQtl,
                    onValueChange = { mandiPricePerQtl = it },
                    labelEng = "Price per Quintal",
                    labelTel = "క్వింటాల్ ధర",
                    selectedLang = langState,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Text("Selling Deductions (రవాణా / ఇతర మినహాయింపులు):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyInput(value = transportCost, onValueChange = { transportCost = it }, labelEng = "Transport Cost", labelTel = "రవాణా చార్జీ", selectedLang = langState, modifier = Modifier.weight(1f))
                CurrencyInput(value = loadingUnloadingCost, onValueChange = { loadingUnloadingCost = it }, labelEng = "Hamali/Loading", labelTel = "హమాలీ చార్జీ", selectedLang = langState, modifier = Modifier.weight(1f))
            }
        }

        item {
            QuantityInput(value = commissionPercent, onValueChange = { commissionPercent = it }, labelEng = "Commission % (If applicable)", labelTel = "కమిషన్ %", selectedLang = langState, suffix = "%")
        }

        if (errorMessage != null) {
            item { Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    quantityQtl = "10"
                    mandiPricePerQtl = "2000"
                    transportCost = "0"
                    loadingUnloadingCost = "0"
                    commissionPercent = "0"
                    resultNetAmount = null
                }
            )
        }

        if (resultNetAmount != null) {
            item {
                val formattedNet = CalculatorUtils.formatCurrency(resultNetAmount!!)
                val formattedGross = CalculatorUtils.formatCurrency(resultGrossAmount ?: 0.0)
                val formattedExp = CalculatorUtils.formatCurrency(resultTotalExpenses ?: 0.0)

                CalculationResultCard(
                    mainAmountStr = formattedNet,
                    mainLabelEng = "Estimated Net Amount Received",
                    mainLabelTel = "అన్ని మినహాయింపుల తర్వాత చేతికి వచ్చే నికర సొమ్ము",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Gross Sale Value / స్థూల అమ్మకం విలువ", formattedGross),
                        Pair("Transport Cost / రవాణా చార్జీలు", "-${CalculatorUtils.formatCurrency(transportCost.toDoubleOrNull() ?: 0.0)}"),
                        Pair("Hamali Cost / హమాలీ చార్జీలు", "-${CalculatorUtils.formatCurrency(loadingUnloadingCost.toDoubleOrNull() ?: 0.0)}"),
                        Pair("Commission Deduction / కమిషన్", "-${CalculatorUtils.formatCurrency((resultGrossAmount ?: 0.0) * (commissionPercent.toDoubleOrNull() ?: 0.0) / 100.0)}"),
                        Pair("Total Deductions / మొత్తం మినహాయింపులు", "-$formattedExp")
                    ),
                    onSave = {
                        onSaveResult(
                            "Crop Sale Calculator",
                            formattedNet,
                            mapOf("Gross" to formattedGross, "Deductions" to formattedExp, "Net" to formattedNet)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CompareMarketsCalculatorScreen(
    mandiRates: List<MandiCropRate>,
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var quantityQtl by remember { mutableStateOf("30") }
    var marketA_Price by remember { mutableStateOf("2600") }
    var marketA_Transport by remember { mutableStateOf("2000") }

    var marketB_Price by remember { mutableStateOf("2450") }
    var marketB_Transport by remember { mutableStateOf("600") }

    var netA by remember { mutableStateOf<Double?>(null) }
    var netB by remember { mutableStateOf<Double?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        val qtl = quantityQtl.toDoubleOrNull() ?: 30.0
        val pA = marketA_Price.toDoubleOrNull() ?: 0.0
        val tA = marketA_Transport.toDoubleOrNull() ?: 0.0

        val pB = marketB_Price.toDoubleOrNull() ?: 0.0
        val tB = marketB_Transport.toDoubleOrNull() ?: 0.0

        netA = (qtl * pA) - tA
        netB = (qtl * pB) - tB
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Compare Markets Calculator ⚖️",
                titleTel = "మార్కెట్ల పోలిక లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            QuantityInput(value = quantityQtl, onValueChange = { quantityQtl = it }, labelEng = "Crop Quantity", labelTel = "పంట పరిమాణం", selectedLang = langState, suffix = "quintals")
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Market A (Distant/Higher Rate) - మార్కెట్ A", fontWeight = FontWeight.Bold, color = KrishiHeaderGreen, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CurrencyInput(value = marketA_Price, onValueChange = { marketA_Price = it }, labelEng = "Price/Qtl", labelTel = "ధర", selectedLang = langState, modifier = Modifier.weight(1f))
                        CurrencyInput(value = marketA_Transport, onValueChange = { marketA_Transport = it }, labelEng = "Transport Cost", labelTel = "రవాణా", selectedLang = langState, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Market B (Local/Lower Rate) - మార్కెట్ B", fontWeight = FontWeight.Bold, color = KrishiHeaderGreen, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CurrencyInput(value = marketB_Price, onValueChange = { marketB_Price = it }, labelEng = "Price/Qtl", labelTel = "ధర", selectedLang = langState, modifier = Modifier.weight(1f))
                        CurrencyInput(value = marketB_Transport, onValueChange = { marketB_Transport = it }, labelEng = "Transport Cost", labelTel = "రవాణా", selectedLang = langState, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = { netA = null; netB = null }
            )
        }

        if (netA != null && netB != null) {
            item {
                val bestA = netA!! >= netB!!
                val diff = Math.abs(netA!! - netB!!)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (bestA) "Market A gives higher net return by ${CalculatorUtils.formatCurrency(diff)}"
                            else "Market B gives higher net return by ${CalculatorUtils.formatCurrency(diff)}",
                            fontWeight = FontWeight.ExtraBold,
                            color = KrishiHeaderGreen,
                            fontSize = 14.sp
                        )
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Market A Net Value:", fontSize = 12.sp)
                            Text(CalculatorUtils.formatCurrency(netA!!), fontWeight = FontWeight.Bold, color = if (bestA) KrishiPrimaryGreen else Color.Gray)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Market B Net Value:", fontSize = 12.sp)
                            Text(CalculatorUtils.formatCurrency(netB!!), fontWeight = FontWeight.Bold, color = if (!bestA) KrishiPrimaryGreen else Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
