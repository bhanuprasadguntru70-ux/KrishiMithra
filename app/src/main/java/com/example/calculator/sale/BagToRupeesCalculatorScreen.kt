package com.example.calculator.sale

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
fun BagToRupeesCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var bagsInput by remember { mutableStateOf("100") }
    var weightPerBagInput by remember { mutableStateOf("50") }
    var rateMode by remember { mutableStateOf("Per Bag") } // "Per Bag", "Per Quintal", "Per Kg"
    var rateInput by remember { mutableStateOf("1500") }

    var totalBags by remember { mutableStateOf<Double?>(null) }
    var totalKg by remember { mutableStateOf(0.0) }
    var totalQuintals by remember { mutableStateOf(0.0) }
    var totalSaleValue by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val bags = bagsInput.toDoubleOrNull() ?: 0.0
        val wt = weightPerBagInput.toDoubleOrNull() ?: 50.0
        val rate = rateInput.toDoubleOrNull() ?: 0.0

        if (bags <= 0 || rate <= 0) return

        val kg = bags * wt
        val qtl = kg / 100.0

        val valTotal = when (rateMode) {
            "Per Bag" -> bags * rate
            "Per Quintal" -> qtl * rate
            else -> kg * rate
        }

        totalBags = bags
        totalKg = kg
        totalQuintals = qtl
        totalSaleValue = valTotal
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Bag → Rupees Calculator",
                titleTe = "బస్తా → రూపాయల లెక్కలు",
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

            Text(
                text = if (appLanguage == "te") "ధర రకం (బస్తాకా / క్వింటాలుకా)" else "Rate Type",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            CalculatorUnitChipSelector(
                selectedUnit = rateMode,
                options = listOf("Per Bag", "Per Quintal", "Per Kg"),
                onUnitSelected = { rateMode = it }
            )

            CalculatorNumberInput(
                value = rateInput,
                onValueChange = { rateInput = it },
                labelEn = "Rate ($rateMode) (₹)",
                labelTe = "ధర ($rateMode) (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    bagsInput = ""
                    rateInput = ""
                    totalSaleValue = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalSaleValue != null) {
                        onSaveCalculation(
                            "Bag Sale ($bagsInput bags)",
                            "SALES & PROFIT",
                            IndianFormatter.formatCurrency(totalSaleValue!!),
                            "Bags: $bagsInput, Rate: ₹$rateInput ($rateMode), Wt: ${IndianFormatter.formatNumber(totalKg)} kg"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalSaleValue?.let { totalVal ->
                ResultCard(
                    mainResultTitleEn = "TOTAL SALE VALUE",
                    mainResultTitleTe = "మొత్తం అమ్మకపు సొమ్ము",
                    mainResultValue = IndianFormatter.formatCurrency(totalVal),
                    appLanguage = appLanguage,
                    subtitleEn = "$bagsInput Bags @ ₹$rateInput / $rateMode",
                    subtitleTe = "$bagsInput బస్తాలు @ ₹$rateInput / $rateMode",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "మొత్తం బస్తాలు" else "Total Bags", "$bagsInput bags"),
                        Pair(if (appLanguage == "te") "మొత్తం బరువు (కిలోల్లో)" else "Total Weight (kg)", "${IndianFormatter.formatNumber(totalKg)} kg"),
                        Pair(if (appLanguage == "te") "మొత్తం క్వింటాళ్ళు" else "Total Quintals", "${IndianFormatter.formatNumber(totalQuintals)} Quintals")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
