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
fun SeedCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var landAreaInput by remember { mutableStateOf("3") }
    var seedPerAcreInput by remember { mutableStateOf("8") }
    var pricePerKgInput by remember { mutableStateOf("180") }

    var totalSeedKg by remember { mutableStateOf<Double?>(null) }
    var totalCost by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val area = landAreaInput.toDoubleOrNull() ?: 0.0
        val kgPerAcre = seedPerAcreInput.toDoubleOrNull() ?: 0.0
        val price = pricePerKgInput.toDoubleOrNull() ?: 0.0

        if (area <= 0 || kgPerAcre <= 0) return

        val seedKg = area * kgPerAcre
        val cost = seedKg * price

        totalSeedKg = seedKg
        totalCost = cost
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Seed Quantity & Cost Calculator",
                titleTe = "విత్తనాల పరిమాణం & ఖర్చు లెక్కలు",
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
                value = landAreaInput,
                onValueChange = { landAreaInput = it },
                labelEn = "Land Area (Acres)",
                labelTe = "సాగు విస్తీర్ణం (ఎకరాలు)",
                appLanguage = appLanguage,
                suffixText = "Acres"
            )

            CalculatorNumberInput(
                value = seedPerAcreInput,
                onValueChange = { seedPerAcreInput = it },
                labelEn = "Seed Requirement per Acre (kg/Acre)",
                labelTe = "ఎకరానికి విత్తనాల అవసరం (కిలోలు)",
                appLanguage = appLanguage,
                suffixText = "kg/Acre"
            )

            CalculatorNumberInput(
                value = pricePerKgInput,
                onValueChange = { pricePerKgInput = it },
                labelEn = "Seed Price per Kg (₹)",
                labelTe = "కిలో విత్తనం ధర (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    landAreaInput = ""
                    seedPerAcreInput = ""
                    pricePerKgInput = ""
                    totalSeedKg = null
                    totalCost = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalCost != null) {
                        onSaveCalculation(
                            "Seed Cost ($landAreaInput Acres)",
                            "Farm Inputs",
                            IndianFormatter.formatCurrency(totalCost!!),
                            "Total Seed: ${IndianFormatter.formatNumber(totalSeedKg ?: 0.0)} kg, Rate: ₹$pricePerKgInput/kg"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalCost?.let { cost ->
                ResultCard(
                    mainResultTitleEn = "TOTAL SEED COST",
                    mainResultTitleTe = "మొత్తం విత్తనాల ఖర్చు",
                    mainResultValue = IndianFormatter.formatCurrency(cost),
                    appLanguage = appLanguage,
                    subtitleEn = "Total Seed Required: ${IndianFormatter.formatNumber(totalSeedKg ?: 0.0)} kg",
                    subtitleTe = "కావాల్సిన మొత్తం విత్తనం: ${IndianFormatter.formatNumber(totalSeedKg ?: 0.0)} కిలోలు",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "సాగు విస్తీర్ణం" else "Land Area", "$landAreaInput Acres"),
                        Pair(if (appLanguage == "te") "ఎకరానికి విత్తనం" else "Per Acre Requirement", "$seedPerAcreInput kg/Acre"),
                        Pair(if (appLanguage == "te") "కిలో ధర" else "Seed Price", "₹$pricePerKgInput / kg")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
