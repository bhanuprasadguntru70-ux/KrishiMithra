package com.example.calculator.tractor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*
import com.example.calculator.models.LandUnit
import com.example.ui.theme.KrishiHeaderGreen

@Composable
fun TractorWorkCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var hourlyRate by remember { mutableStateOf("800") }
    var hours by remember { mutableStateOf("5") }
    var minutes by remember { mutableStateOf("30") }
    var dieselCost by remember { mutableStateOf("0") }
    var operatorCost by remember { mutableStateOf("0") }
    var otherCost by remember { mutableStateOf("0") }

    var resultTotalCost by remember { mutableStateOf<Double?>(null) }
    var resultBasicTractorCost by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val rate = hourlyRate.toDoubleOrNull()
        val h = hours.toDoubleOrNull() ?: 0.0
        val m = minutes.toDoubleOrNull() ?: 0.0

        if (rate == null || rate <= 0.0 || (h <= 0.0 && m <= 0.0)) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన రేటు మరియు పని సమయాన్ని ఎంటర్ చేయండి." else "Please enter valid hourly rate and working time."
            resultTotalCost = null
            return
        }

        val totalHours = h + (m / 60.0)
        val basicCost = rate * totalHours
        val diesel = dieselCost.toDoubleOrNull() ?: 0.0
        val operator = operatorCost.toDoubleOrNull() ?: 0.0
        val other = otherCost.toDoubleOrNull() ?: 0.0

        val total = basicCost + diesel + operator + other

        resultBasicTractorCost = basicCost
        resultTotalCost = total
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Tractor Work Calculator 🚜",
                titleTel = "ట్రాక్టర్ పని లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            CurrencyInput(
                value = hourlyRate,
                onValueChange = { hourlyRate = it },
                labelEng = "Tractor Hourly Rate (₹/hr)",
                labelTel = "గంటకు ట్రాక్టర్ అద్దె (రూ/గంట)",
                selectedLang = langState
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = hours,
                    onValueChange = { hours = it },
                    labelEng = "Working Hours",
                    labelTel = "పని చేసిన గంటలు",
                    selectedLang = langState,
                    suffix = "hrs",
                    modifier = Modifier.weight(1f)
                )
                QuantityInput(
                    value = minutes,
                    onValueChange = { minutes = it },
                    labelEng = "Minutes",
                    labelTel = "నిమిషాలు",
                    selectedLang = langState,
                    suffix = "mins",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("Optional Costs (ఐచ్ఛిక ఖర్చులు):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KrishiHeaderGreen)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyInput(
                    value = dieselCost,
                    onValueChange = { dieselCost = it },
                    labelEng = "Diesel Cost",
                    labelTel = "డీజిల్ ఖర్చు",
                    selectedLang = langState,
                    modifier = Modifier.weight(1f)
                )
                CurrencyInput(
                    value = operatorCost,
                    onValueChange = { operatorCost = it },
                    labelEng = "Driver/Labour",
                    labelTel = "డ్రైవర్ కూలి",
                    selectedLang = langState,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CurrencyInput(
                value = otherCost,
                onValueChange = { otherCost = it },
                labelEng = "Other Cost",
                labelTel = "ఇతర ఖర్చులు",
                selectedLang = langState
            )
        }

        if (errorMessage != null) {
            item {
                Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    hourlyRate = ""
                    hours = ""
                    minutes = "0"
                    dieselCost = "0"
                    operatorCost = "0"
                    otherCost = "0"
                    resultTotalCost = null
                    errorMessage = null
                }
            )
        }

        if (resultTotalCost != null) {
            item {
                val formattedTotal = CalculatorUtils.formatCurrency(resultTotalCost!!)
                val formattedBasic = CalculatorUtils.formatCurrency(resultBasicTractorCost ?: 0.0)
                val h = hours.toDoubleOrNull() ?: 0.0
                val m = minutes.toDoubleOrNull() ?: 0.0

                CalculationResultCard(
                    mainAmountStr = formattedTotal,
                    mainLabelEng = "Total Tractor Expense",
                    mainLabelTel = "మొత్తం ట్రాక్టర్ ఖర్చు",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Working Time / పని సమయం", "${h.toInt()}h ${m.toInt()}m"),
                        Pair("Basic Tractor Rental / ట్రాక్టర్ అద్దె", formattedBasic),
                        Pair("Diesel Expense / డీజిల్ ఖర్చు", CalculatorUtils.formatCurrency(dieselCost.toDoubleOrNull() ?: 0.0)),
                        Pair("Driver Wage / డ్రైవర్ కూలి", CalculatorUtils.formatCurrency(operatorCost.toDoubleOrNull() ?: 0.0)),
                        Pair("Other Cost / ఇతర ఖర్చులు", CalculatorUtils.formatCurrency(otherCost.toDoubleOrNull() ?: 0.0))
                    ),
                    onSave = {
                        onSaveResult(
                            "Tractor Work Calculator",
                            formattedTotal,
                            mapOf("Rate" to "₹$hourlyRate/hr", "Time" to "${h.toInt()}h ${m.toInt()}m", "Total Cost" to formattedTotal)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun TractorAreaCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var areaInput by remember { mutableStateOf("4.5") }
    var selectedUnit by remember { mutableStateOf(LandUnit.ACRES) }
    var hourlyRate by remember { mutableStateOf("800") }
    var hrsPerAcre by remember { mutableStateOf("1.5") }

    var resultTotalCost by remember { mutableStateOf<Double?>(null) }
    var resultCostPerAcre by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val areaVal = areaInput.toDoubleOrNull()
        val rate = hourlyRate.toDoubleOrNull()
        val hrs = hrsPerAcre.toDoubleOrNull() ?: 1.5

        if (areaVal == null || areaVal <= 0.0 || rate == null || rate <= 0.0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన పొలం వైశాల్యం మరియు అద్దె నమోదు చేయండి." else "Please enter valid land area and hourly rate."
            resultTotalCost = null
            return
        }

        // Convert area to Acres
        val acres = areaVal * selectedUnit.toAcresRatio
        val totalHoursNeeded = acres * hrs
        val totalCost = totalHoursNeeded * rate
        val costPerAcre = totalCost / acres

        resultTotalCost = totalCost
        resultCostPerAcre = costPerAcre
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Tractor Area Calculator 🏞️",
                titleTel = "పొలం ఆధారిత ట్రాక్టర్ లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = areaInput,
                    onValueChange = { areaInput = it },
                    labelEng = "Land Area",
                    labelTel = "పొలం వైశాల్యం",
                    selectedLang = langState,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("Select Unit (యూనిట్ ఎంచుకోండి):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LandUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = selectedUnit == unit,
                        onClick = { selectedUnit = unit },
                        label = { Text(unit.name, fontSize = 11.sp) }
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyInput(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    labelEng = "Hourly Rate (₹/hr)",
                    labelTel = "గంటకు అద్దె",
                    selectedLang = langState,
                    modifier = Modifier.weight(1f)
                )
                QuantityInput(
                    value = hrsPerAcre,
                    onValueChange = { hrsPerAcre = it },
                    labelEng = "Hours / Acre",
                    labelTel = "ఎకరానికి సమయం",
                    selectedLang = langState,
                    suffix = "hrs",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (errorMessage != null) {
            item {
                Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    areaInput = ""
                    hourlyRate = "800"
                    hrsPerAcre = "1.5"
                    resultTotalCost = null
                    errorMessage = null
                }
            )
        }

        if (resultTotalCost != null) {
            item {
                val formattedTotal = CalculatorUtils.formatCurrency(resultTotalCost!!)
                val formattedPerAcre = CalculatorUtils.formatCurrency(resultCostPerAcre ?: 0.0)
                val acres = (areaInput.toDoubleOrNull() ?: 1.0) * selectedUnit.toAcresRatio

                CalculationResultCard(
                    mainAmountStr = formattedTotal,
                    mainLabelEng = "Total Field Tractor Cost",
                    mainLabelTel = "మొత్తం పొలం ట్రాక్టర్ ఖర్చు",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Total Area (Acres) / ఎకరాలు", CalculatorUtils.formatNumber(acres)),
                        Pair("Cost Per Acre / ఎకరానికి ఖర్చు", formattedPerAcre),
                        Pair("Estimated Total Hours / మొత్తం గంటలు", "${CalculatorUtils.formatNumber(acres * (hrsPerAcre.toDoubleOrNull() ?: 1.5))} hrs")
                    ),
                    onSave = {
                        onSaveResult(
                            "Tractor Area Calculator",
                            formattedTotal,
                            mapOf("Area" to "$areaInput ${selectedUnit.name}", "Total Cost" to formattedTotal, "Cost/Acre" to formattedPerAcre)
                        )
                    }
                )
            }
        }
    }
}
