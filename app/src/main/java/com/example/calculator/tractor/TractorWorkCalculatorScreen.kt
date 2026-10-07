package com.example.calculator.tractor

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.calculator.components.*
import com.example.calculator.util.IndianFormatter

@Composable
fun TractorWorkCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hourlyRateInput by remember { mutableStateOf("800") }
    var hoursInput by remember { mutableStateOf("5") }
    var minutesInput by remember { mutableStateOf("0") }
    var dieselCostInput by remember { mutableStateOf("0") }
    var operatorCostInput by remember { mutableStateOf("0") }
    var otherCostInput by remember { mutableStateOf("0") }

    var basicTractorCost by remember { mutableStateOf<Double?>(null) }
    var totalCost by remember { mutableStateOf<Double?>(null) }
    var totalFormattedTime by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val rate = hourlyRateInput.toDoubleOrNull()
        val hrs = hoursInput.toDoubleOrNull() ?: 0.0
        val mins = minutesInput.toDoubleOrNull() ?: 0.0

        if (rate == null || rate <= 0) {
            errorMessage = if (appLanguage == "te") "దయచేసి సరైన గంట రకం నమోదు చేయండి." else "Please enter a valid hourly rate."
            basicTractorCost = null
            totalCost = null
            return
        }

        if (hrs == 0.0 && mins == 0.0) {
            errorMessage = if (appLanguage == "te") "దయచేసి పనిచేసిన గంటలు/నిమిషాలు నమోదు చేయండి." else "Please enter working hours or minutes."
            basicTractorCost = null
            totalCost = null
            return
        }

        errorMessage = null
        val totalDecimalHours = hrs + (mins / 60.0)
        val basic = rate * totalDecimalHours
        val diesel = dieselCostInput.toDoubleOrNull() ?: 0.0
        val operator = operatorCostInput.toDoubleOrNull() ?: 0.0
        val other = otherCostInput.toDoubleOrNull() ?: 0.0

        val total = basic + diesel + operator + other

        basicTractorCost = basic
        totalCost = total
        totalFormattedTime = if (mins > 0) "${hrs.toInt()} hrs ${mins.toInt()} mins" else "${hrs.toInt()} hrs"
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Tractor Work Calculator",
                titleTe = "ట్రాక్టర్ పని లెక్కలు",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CalculatorNumberInput(
                value = hourlyRateInput,
                onValueChange = { hourlyRateInput = it },
                labelEn = "Tractor Hourly Rate (₹/hour)",
                labelTe = "గంటకు ట్రాక్టర్ ధర (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹",
                isError = errorMessage != null && hourlyRateInput.isEmpty()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalculatorNumberInput(
                    value = hoursInput,
                    onValueChange = { hoursInput = it },
                    labelEn = "Working Hours",
                    labelTe = "పనిచేసిన గంటలు",
                    appLanguage = appLanguage,
                    suffixText = "hrs",
                    modifier = Modifier.weight(1f)
                )

                CalculatorNumberInput(
                    value = minutesInput,
                    onValueChange = { minutesInput = it },
                    labelEn = "Optional Minutes",
                    labelTe = "నిమిషాలు",
                    appLanguage = appLanguage,
                    suffixText = "mins",
                    modifier = Modifier.weight(1f)
                )
            }

            CalculatorNumberInput(
                value = dieselCostInput,
                onValueChange = { dieselCostInput = it },
                labelEn = "Optional Diesel Cost (₹)",
                labelTe = "డీజిల్ ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = operatorCostInput,
                onValueChange = { operatorCostInput = it },
                labelEn = "Optional Operator / Labour Cost (₹)",
                labelTe = "డ్రైవర్ / కూలీ ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = otherCostInput,
                onValueChange = { otherCostInput = it },
                labelEn = "Optional Other Cost (₹)",
                labelTe = "ఇతర ఖర్చులు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    hourlyRateInput = ""
                    hoursInput = ""
                    minutesInput = ""
                    dieselCostInput = "0"
                    operatorCostInput = "0"
                    otherCostInput = "0"
                    basicTractorCost = null
                    totalCost = null
                    errorMessage = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalCost != null) {
                        onSaveCalculation(
                            "Tractor Work ($totalFormattedTime)",
                            "Machinery",
                            IndianFormatter.formatCurrency(totalCost!!),
                            "Rate: ₹$hourlyRateInput/hr, Time: $totalFormattedTime, Basic: ${IndianFormatter.formatCurrency(basicTractorCost!!)}"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "లెక్క భద్రపరచబడింది!" else "Calculation Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalCost?.let { total ->
                ResultCard(
                    mainResultTitleEn = "TOTAL TRACTOR COST",
                    mainResultTitleTe = "మొత్తం ట్రాక్టర్ ఖర్చు",
                    mainResultValue = IndianFormatter.formatCurrency(total),
                    appLanguage = appLanguage,
                    subtitleEn = "Tractor Time: $totalFormattedTime @ ₹$hourlyRateInput/hr",
                    subtitleTe = "పని సమయం: $totalFormattedTime @ ₹$hourlyRateInput/గంట",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "ట్రాక్టర్ ప్రాథమిక ధర" else "Basic Tractor Cost", IndianFormatter.formatCurrency(basicTractorCost ?: 0.0)),
                        Pair(if (appLanguage == "te") "డీజిల్ ఖర్చు" else "Diesel Cost", IndianFormatter.formatCurrency(dieselCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "డ్రైవర్ / కూలీ బత్తా" else "Operator Allowance", IndianFormatter.formatCurrency(operatorCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "ఇతర ఖర్చులు" else "Other Costs", IndianFormatter.formatCurrency(otherCostInput.toDoubleOrNull() ?: 0.0))
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
