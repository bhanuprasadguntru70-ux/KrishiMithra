package com.example.calculator.crop

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
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
fun FertilizerCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var fertilizerName by remember { mutableStateOf("Urea / DAP / Complex") }
    var bagsInput by remember { mutableStateOf("6") }
    var weightPerBagInput by remember { mutableStateOf("45") }
    var pricePerBagInput by remember { mutableStateOf("1350") }

    var totalWeightKg by remember { mutableStateOf<Double?>(null) }
    var totalCost by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val bags = bagsInput.toDoubleOrNull() ?: 0.0
        val weight = weightPerBagInput.toDoubleOrNull() ?: 0.0
        val price = pricePerBagInput.toDoubleOrNull() ?: 0.0

        if (bags <= 0 || price <= 0) return

        val kg = bags * weight
        val cost = bags * price

        totalWeightKg = kg
        totalCost = cost
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Fertilizer Cost Calculator",
                titleTe = "ఎరువుల ఖర్చు లెక్కలు",
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

            Column {
                Text(
                    text = if (appLanguage == "te") "ఎరువు పేరు (ఉదా: డిఎపి, యూరియా, 20-20-0)" else "Fertilizer Name (e.g. DAP, Urea, Complex)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = fertilizerName,
                    onValueChange = { fertilizerName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            CalculatorNumberInput(
                value = bagsInput,
                onValueChange = { bagsInput = it },
                labelEn = "Number of Fertilizer Bags",
                labelTe = "ఎరువుల బస్తాల సంఖ్య",
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
                value = pricePerBagInput,
                onValueChange = { pricePerBagInput = it },
                labelEn = "Price per Bag (₹)",
                labelTe = "బస్తా ధర (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    bagsInput = ""
                    pricePerBagInput = ""
                    totalCost = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalCost != null) {
                        onSaveCalculation(
                            "Fertilizer ($fertilizerName)",
                            "Farm Inputs",
                            IndianFormatter.formatCurrency(totalCost!!),
                            "Bags: $bagsInput, Total Wt: ${IndianFormatter.formatNumber(totalWeightKg ?: 0.0)} kg, Rate: ₹$pricePerBagInput/bag"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalCost?.let { cost ->
                ResultCard(
                    mainResultTitleEn = "TOTAL FERTILIZER COST",
                    mainResultTitleTe = "మొత్తం ఎరువుల ఖర్చు",
                    mainResultValue = IndianFormatter.formatCurrency(cost),
                    appLanguage = appLanguage,
                    subtitleEn = "Total Weight: ${IndianFormatter.formatNumber(totalWeightKg ?: 0.0)} kg ($bagsInput Bags)",
                    subtitleTe = "మొత్తం బరువు: ${IndianFormatter.formatNumber(totalWeightKg ?: 0.0)} కిలోలు ($bagsInput బస్తాలు)",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "ఎరువు రకం" else "Fertilizer Name", fertilizerName),
                        Pair(if (appLanguage == "te") "బస్తాల సంఖ్య" else "No. of Bags", "$bagsInput Bags"),
                        Pair(if (appLanguage == "te") "బస్తా ధర" else "Price/Bag", "₹$pricePerBagInput")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
