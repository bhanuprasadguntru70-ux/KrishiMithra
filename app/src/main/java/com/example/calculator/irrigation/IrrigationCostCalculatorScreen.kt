package com.example.calculator.irrigation

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
fun IrrigationCostCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hoursPerDayInput by remember { mutableStateOf("4") }
    var costPerHourInput by remember { mutableStateOf("120") }
    var irrigationDaysInput by remember { mutableStateOf("15") }

    var totalCost by remember { mutableStateOf<Double?>(null) }
    var totalHours by remember { mutableStateOf(0.0) }

    fun calculate() {
        val hrs = hoursPerDayInput.toDoubleOrNull() ?: 0.0
        val costHr = costPerHourInput.toDoubleOrNull() ?: 0.0
        val days = irrigationDaysInput.toDoubleOrNull() ?: 0.0

        if (hrs <= 0 || costHr <= 0 || days <= 0) return

        val totHrs = hrs * days
        val cost = totHrs * costHr

        totalHours = totHrs
        totalCost = cost
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Irrigation / Water Cost Calculator",
                titleTe = "నీటిపారుదల ఖర్చు లెక్కలు",
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
                value = hoursPerDayInput,
                onValueChange = { hoursPerDayInput = it },
                labelEn = "Pump Running Hours per Day",
                labelTe = "రోజుకు మోటార్ / పంపు నడిచే గంటలు",
                appLanguage = appLanguage,
                suffixText = "hrs/day"
            )

            CalculatorNumberInput(
                value = costPerHourInput,
                onValueChange = { costPerHourInput = it },
                labelEn = "Electricity / Diesel Cost per Hour (₹/hr)",
                labelTe = "గంటకు విద్యుత్ / డీజిల్ ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = irrigationDaysInput,
                onValueChange = { irrigationDaysInput = it },
                labelEn = "Number of Irrigation Days",
                labelTe = "తడులు ఇచ్చిన మొత్తం రోజుల సంఖ్య",
                appLanguage = appLanguage,
                suffixText = "days"
            )

            CalculateButtonRow(
                onReset = {
                    hoursPerDayInput = ""
                    costPerHourInput = ""
                    irrigationDaysInput = ""
                    totalCost = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalCost != null) {
                        onSaveCalculation(
                            "Irrigation ($irrigationDaysInput days)",
                            "IRRIGATION",
                            IndianFormatter.formatCurrency(totalCost!!),
                            "Total Hours: $totalHours hrs, Rate: ₹$costPerHourInput/hr"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalCost?.let { cost ->
                ResultCard(
                    mainResultTitleEn = "TOTAL IRRIGATION WATER COST",
                    mainResultTitleTe = "మొత్తం నీటిపారుదల ఖర్చు",
                    mainResultValue = IndianFormatter.formatCurrency(cost),
                    appLanguage = appLanguage,
                    subtitleEn = "Total Motor Runtime: ${IndianFormatter.formatNumber(totalHours)} Hours",
                    subtitleTe = "మొత్తం మోటార్ నడిచిన సమయం: ${IndianFormatter.formatNumber(totalHours)} గంటలు",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "రోజువారీ పని సమయం" else "Daily Runtime", "$hoursPerDayInput hrs/day"),
                        Pair(if (appLanguage == "te") "నీటి తడులు ఇచ్చిన రోజులు" else "Irrigation Days", "$irrigationDaysInput days"),
                        Pair(if (appLanguage == "te") "గంటకు అయ్యే ఖర్చు" else "Hourly Cost Rate", "₹$costPerHourInput / hr")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
