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
fun PesticideCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var productName by remember { mutableStateOf("Insecticide / Fungicide Spray") }
    var unitsInput by remember { mutableStateOf("4") }
    var unitType by remember { mutableStateOf("litres / bottles") }
    var pricePerUnitInput by remember { mutableStateOf("650") }

    var totalCost by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val units = unitsInput.toDoubleOrNull() ?: 0.0
        val price = pricePerUnitInput.toDoubleOrNull() ?: 0.0

        if (units <= 0 || price <= 0) return

        totalCost = units * price
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Pesticide & Input Cost Calculator",
                titleTe = "పురుగుమందుల ఖర్చు లెక్కలు",
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
                    text = if (appLanguage == "te") "మందు పేరు (స్ప్రే/రసాయనం)" else "Product Name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            CalculatorNumberInput(
                value = unitsInput,
                onValueChange = { unitsInput = it },
                labelEn = "Number of Units / Bottles / Packets",
                labelTe = "బాటిళ్లు / ప్యాకెట్ల సంఖ్య",
                appLanguage = appLanguage
            )

            CalculatorNumberInput(
                value = pricePerUnitInput,
                onValueChange = { pricePerUnitInput = it },
                labelEn = "Price per Unit (₹)",
                labelTe = "ఒక్క బాటిల్/ప్యాకెట్ ధర (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    unitsInput = ""
                    pricePerUnitInput = ""
                    totalCost = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalCost != null) {
                        onSaveCalculation(
                            "Pesticide ($productName)",
                            "Farm Inputs",
                            IndianFormatter.formatCurrency(totalCost!!),
                            "Units: $unitsInput, Price/Unit: ₹$pricePerUnitInput"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalCost?.let { cost ->
                ResultCard(
                    mainResultTitleEn = "TOTAL PESTICIDE COST",
                    mainResultTitleTe = "మొత్తం పిచికారీ మందుల ఖర్చు",
                    mainResultValue = IndianFormatter.formatCurrency(cost),
                    appLanguage = appLanguage,
                    subtitleEn = "$unitsInput units @ ₹$pricePerUnitInput / unit",
                    subtitleTe = "$unitsInput యూనిట్లు @ ₹$pricePerUnitInput / ఒకదానికి",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "ఉత్పత్తి పేరు" else "Product Name", productName),
                        Pair(if (appLanguage == "te") "మొత్తం పరిమాణం" else "Total Quantity", "$unitsInput units"),
                        Pair(if (appLanguage == "te") "యూనిట్ ధర" else "Unit Price", "₹$pricePerUnitInput")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
