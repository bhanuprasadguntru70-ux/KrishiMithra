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
fun SalePriceTargetScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var totalCostInput by remember { mutableStateOf("50000") }
    var expectedProfitInput by remember { mutableStateOf("10000") }
    var expectedQuantityInput by remember { mutableStateOf("20") }
    var quantityUnit by remember { mutableStateOf("quintal") } // "quintal", "bag", "kg"

    var targetPrice by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val cost = totalCostInput.toDoubleOrNull() ?: 0.0
        val profit = expectedProfitInput.toDoubleOrNull() ?: 0.0
        val qty = expectedQuantityInput.toDoubleOrNull() ?: 0.0

        if (qty <= 0) return

        targetPrice = (cost + profit) / qty
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Sale Price Target Calculator",
                titleTe = "లక్ష్య అమ్మకపు ధర లెక్కలు",
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
                value = expectedProfitInput,
                onValueChange = { expectedProfitInput = it },
                labelEn = "Expected Target Profit (₹)",
                labelTe = "మీరు కోరుకునే నికర లాభం (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = expectedQuantityInput,
                onValueChange = { expectedQuantityInput = it },
                labelEn = "Expected Production Quantity",
                labelTe = "అంచనా దిగుబడి పరిమాణం",
                appLanguage = appLanguage
            )

            Text(
                text = if (appLanguage == "te") "దిగుబడి కొలత ప్రమాణం" else "Production Quantity Unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            CalculatorUnitChipSelector(
                selectedUnit = quantityUnit,
                options = listOf("quintal", "bag", "kg"),
                onUnitSelected = { quantityUnit = it }
            )

            CalculateButtonRow(
                onReset = {
                    totalCostInput = ""
                    expectedProfitInput = ""
                    expectedQuantityInput = ""
                    targetPrice = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (targetPrice != null) {
                        onSaveCalculation(
                            "Target Price (₹${IndianFormatter.formatNumber(targetPrice!!)}/$quantityUnit)",
                            "SALES & PROFIT",
                            "₹${IndianFormatter.formatNumber(targetPrice!!)} / $quantityUnit",
                            "Cost: ₹$totalCostInput, Expected Profit: ₹$expectedProfitInput, Yield: $expectedQuantityInput $quantityUnit"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            targetPrice?.let { price ->
                ResultCard(
                    mainResultTitleEn = "YOUR REQUIRED TARGET SALE PRICE",
                    mainResultTitleTe = "మీకు రావాల్సిన కనీస లక్ష్య ధర",
                    mainResultValue = "₹${IndianFormatter.formatNumber(price)} / $quantityUnit",
                    appLanguage = appLanguage,
                    subtitleEn = "* Note: This is your personal break-even target price, NOT an official government or mandi price.",
                    subtitleTe = "* గమనిక: ఇది మీ వ్యక్తిగత పెట్టుబడి + లాభ లక్ష్య ధర మాత్రమే, ప్రభుత్వ లేదా మార్కెట్ ధర కాదు.",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "లక్ష్య అమ్మకపు ధర" else "Target Sale Price", "₹${IndianFormatter.formatNumber(price)} / $quantityUnit"),
                        Pair(if (appLanguage == "te") "మొత్తం ఖర్చు + లాభం" else "Cost + Desired Profit", IndianFormatter.formatCurrency((totalCostInput.toDoubleOrNull() ?: 0.0) + (expectedProfitInput.toDoubleOrNull() ?: 0.0))),
                        Pair(if (appLanguage == "te") "దిగుబడి పరిమాణం" else "Expected Yield", "$expectedQuantityInput $quantityUnit")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
