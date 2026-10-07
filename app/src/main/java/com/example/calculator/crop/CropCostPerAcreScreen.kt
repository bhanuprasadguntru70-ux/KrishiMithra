package com.example.calculator.crop

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
fun CropCostPerAcreScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var totalCostInput by remember { mutableStateOf("45000") }
    var landAreaInput by remember { mutableStateOf("3") }

    var costPerAcre by remember { mutableStateOf<Double?>(null) }
    var costPerHectare by remember { mutableStateOf(0.0) }

    fun calculate() {
        val totalCost = totalCostInput.toDoubleOrNull() ?: 0.0
        val acres = landAreaInput.toDoubleOrNull() ?: 0.0

        if (totalCost <= 0 || acres <= 0) return

        val perAcre = totalCost / acres
        val perHectare = perAcre * 2.47105

        costPerAcre = perAcre
        costPerHectare = perHectare
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Crop Cost Per Acre Calculator",
                titleTe = "ఎకరానికి సగటు పంట ఖర్చు లెక్కలు",
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
                value = totalCostInput,
                onValueChange = { totalCostInput = it },
                labelEn = "Total Cultivation Cost (₹)",
                labelTe = "మొత్తం పంట పెట్టుబడి ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = landAreaInput,
                onValueChange = { landAreaInput = it },
                labelEn = "Total Land Area (Acres)",
                labelTe = "మొత్తం సాగు విస్తీర్ణం (ఎకరాలు)",
                appLanguage = appLanguage,
                suffixText = "Acres"
            )

            CalculateButtonRow(
                onReset = {
                    totalCostInput = ""
                    landAreaInput = ""
                    costPerAcre = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (costPerAcre != null) {
                        onSaveCalculation(
                            "Cost per Acre ($landAreaInput Acres)",
                            "Land & Cost",
                            IndianFormatter.formatCurrency(costPerAcre!!),
                            "Total Cost: ₹$totalCostInput, Area: $landAreaInput Acres, Cost/Hectare: ${IndianFormatter.formatCurrency(costPerHectare)}"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            costPerAcre?.let { perAcre ->
                ResultCard(
                    mainResultTitleEn = "AVERAGE COST PER ACRE",
                    mainResultTitleTe = "ఎకరానికి సగటు పెట్టుబడి ఖర్చు",
                    mainResultValue = IndianFormatter.formatCurrency(perAcre),
                    appLanguage = appLanguage,
                    subtitleEn = "Total Investment: ${IndianFormatter.formatCurrency(totalCostInput.toDoubleOrNull() ?: 0.0)} over $landAreaInput Acres",
                    subtitleTe = "మొత్తం పెట్టుబడి: ${IndianFormatter.formatCurrency(totalCostInput.toDoubleOrNull() ?: 0.0)} ($landAreaInput ఎకరాలు)",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "ఎకరానికి ఖర్చు" else "Cost per Acre", IndianFormatter.formatCurrency(perAcre)),
                        Pair(if (appLanguage == "te") "హెక్టారుకి ఖర్చు" else "Cost per Hectare", IndianFormatter.formatCurrency(costPerHectare))
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
