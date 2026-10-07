package com.example.calculator.crop

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*
import com.example.calculator.util.IndianFormatter

@Composable
fun CropYieldBagsCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var bagsInput by remember { mutableStateOf("50") }
    var weightPerBagInput by remember { mutableStateOf("40") }
    var pricePerUnitInput by remember { mutableStateOf("2500") }
    var selectedPriceUnit by remember { mutableStateOf("quintal") } // "kg", "quintal", "tonne"

    var totalKg by remember { mutableStateOf<Double?>(null) }
    var totalQuintals by remember { mutableStateOf(0.0) }
    var totalTonnes by remember { mutableStateOf(0.0) }
    var totalSaleAmount by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val bags = bagsInput.toDoubleOrNull() ?: 0.0
        val weight = weightPerBagInput.toDoubleOrNull() ?: 0.0
        val price = pricePerUnitInput.toDoubleOrNull() ?: 0.0

        if (bags <= 0 || weight <= 0) return

        val kg = bags * weight
        val qtl = kg / 100.0
        val ton = kg / 1000.0

        val totalVal = when (selectedPriceUnit) {
            "kg" -> kg * price
            "tonne" -> ton * price
            else -> qtl * price
        }

        totalKg = kg
        totalQuintals = qtl
        totalTonnes = ton
        totalSaleAmount = totalVal
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Crop Yield & Bags Calculator",
                titleTe = "పంట దిగుబడి & బస్తాల లెక్కలు",
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
                value = bagsInput,
                onValueChange = { bagsInput = it },
                labelEn = "Number of Bags",
                labelTe = "బస్తాల సంఖ్య",
                appLanguage = appLanguage,
                suffixText = "bags"
            )

            CalculatorNumberInput(
                value = weightPerBagInput,
                onValueChange = { weightPerBagInput = it },
                labelEn = "Weight per Bag (kg)",
                labelTe = "ఒక్క బస్తా బరువు (కిలోలు)",
                appLanguage = appLanguage,
                suffixText = "kg"
            )

            CalculatorNumberInput(
                value = pricePerUnitInput,
                onValueChange = { pricePerUnitInput = it },
                labelEn = "Crop Price (₹)",
                labelTe = "పంట మార్కెట్ ధర (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            Text(
                text = if (appLanguage == "te") "ధర రకం ఎంచుకోండి" else "Price Unit Rate Type",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            CalculatorUnitChipSelector(
                selectedUnit = selectedPriceUnit,
                options = listOf("kg", "quintal", "tonne"),
                onUnitSelected = { selectedPriceUnit = it }
            )

            CalculateButtonRow(
                onReset = {
                    bagsInput = ""
                    weightPerBagInput = ""
                    pricePerUnitInput = ""
                    totalKg = null
                    totalSaleAmount = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalSaleAmount != null) {
                        onSaveCalculation(
                            "Yield ($bagsInput bags @ $weightPerBagInput kg)",
                            "Crop Yield",
                            IndianFormatter.formatCurrency(totalSaleAmount!!),
                            "Bags: $bagsInput, Weight: ${IndianFormatter.formatNumber(totalKg ?: 0.0)} kg (${IndianFormatter.formatNumber(totalQuintals)} Quintals)"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalSaleAmount?.let { totalVal ->
                ResultCard(
                    mainResultTitleEn = "ESTIMATED TOTAL SALE VALUE",
                    mainResultTitleTe = "అంచనా మొత్తం అమ్మకపు ధర",
                    mainResultValue = IndianFormatter.formatCurrency(totalVal),
                    appLanguage = appLanguage,
                    subtitleEn = "$bagsInput Bags = ${IndianFormatter.formatNumber(totalQuintals)} Quintals",
                    subtitleTe = "$bagsInput బస్తాలు = ${IndianFormatter.formatNumber(totalQuintals)} క్వింటాళ్ళు",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "మొత్తం బరువు (కిలోల్లో)" else "Total Weight (kg)", "${IndianFormatter.formatNumber(totalKg ?: 0.0)} kg"),
                        Pair(if (appLanguage == "te") "మొత్తం క్వింటాళ్ళు" else "Total Quintals", "${IndianFormatter.formatNumber(totalQuintals)} Quintals"),
                        Pair(if (appLanguage == "te") "మొత్తం టన్నులు" else "Total Tonnes", "${IndianFormatter.formatNumber(totalTonnes)} Tonnes"),
                        Pair(if (appLanguage == "te") "ధర లెక్కల ప్రమాణం" else "Applied Rate", "₹$pricePerUnitInput per $selectedPriceUnit")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
