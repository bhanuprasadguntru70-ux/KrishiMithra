package com.example.calculator.profit

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
fun ProfitLossCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var cultivationCostInput by remember { mutableStateOf("45000") }
    var saleAmountInput by remember { mutableStateOf("70000") }
    var otherExpensesInput by remember { mutableStateOf("3000") }

    var netResult by remember { mutableStateOf<Double?>(null) }
    var isProfit by remember { mutableStateOf(true) }
    var profitPercentage by remember { mutableStateOf(0.0) }

    fun calculate() {
        val cost = cultivationCostInput.toDoubleOrNull() ?: 0.0
        val sale = saleAmountInput.toDoubleOrNull() ?: 0.0
        val other = otherExpensesInput.toDoubleOrNull() ?: 0.0

        val totalInvestment = cost + other
        val diff = sale - totalInvestment

        netResult = diff
        isProfit = diff >= 0
        profitPercentage = if (totalInvestment > 0) (Math.abs(diff) / totalInvestment) * 100.0 else 0.0
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Profit / Loss Calculator",
                titleTe = "లాభం / నష్టం లెక్కలు",
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
                value = cultivationCostInput,
                onValueChange = { cultivationCostInput = it },
                labelEn = "Total Cultivation Cost / Investment (₹)",
                labelTe = "మొత్తం సాగు పెట్టుబడి ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = saleAmountInput,
                onValueChange = { saleAmountInput = it },
                labelEn = "Total Crop Sale Income (₹)",
                labelTe = "మొత్తం పంట అమ్మకం ద్వారా వచ్చిన ఆదాయం (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = otherExpensesInput,
                onValueChange = { otherExpensesInput = it },
                labelEn = "Other Additional Expenses (₹)",
                labelTe = "ఇతర అదనపు ఖర్చులు (మండీ/రవాణా) (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    cultivationCostInput = ""
                    saleAmountInput = ""
                    otherExpensesInput = "0"
                    netResult = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (netResult != null) {
                        val status = if (isProfit) "PROFIT" else "LOSS"
                        onSaveCalculation(
                            "Crop $status (${IndianFormatter.formatCurrency(netResult!!)})",
                            "SALES & PROFIT",
                            "${if (isProfit) "PROFIT" else "LOSS"}: ${IndianFormatter.formatCurrency(Math.abs(netResult!!))}",
                            "Income: ₹$saleAmountInput, Cost: ₹$cultivationCostInput, Return: ${IndianFormatter.formatNumber(profitPercentage)}%"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            netResult?.let { res ->
                val absRes = Math.abs(res)
                ResultCard(
                    mainResultTitleEn = if (isProfit) "NET PROFIT" else "NET LOSS",
                    mainResultTitleTe = if (isProfit) "నికర లాభం" else "నికర నష్టం",
                    mainResultValue = IndianFormatter.formatCurrency(absRes),
                    appLanguage = appLanguage,
                    badgeText = if (isProfit) "PROFIT (${IndianFormatter.formatNumber(profitPercentage)}%)" else "LOSS (${IndianFormatter.formatNumber(profitPercentage)}%)",
                    badgeColor = if (isProfit) androidx.compose.ui.graphics.Color(0xFF2E7D32) else androidx.compose.ui.graphics.Color(0xFFC62828),
                    isProfit = isProfit,
                    subtitleEn = if (isProfit) "Congratulations! Income exceeds total investment." else "Warning: Total investment exceeds sale income.",
                    subtitleTe = if (isProfit) "అభినందనలు! రాబడి పెట్టుబడి కంటే ఎక్కువగా ఉంది." else "గమనిక: పెట్టుబడి రాబడి కంటే ఎక్కువగా ఉంది.",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "మొత్తం ఆదాయం" else "Total Income", IndianFormatter.formatCurrency(saleAmountInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "మొత్తం పెట్టుబడి ఖర్చు" else "Total Investment Cost", IndianFormatter.formatCurrency((cultivationCostInput.toDoubleOrNull() ?: 0.0) + (otherExpensesInput.toDoubleOrNull() ?: 0.0))),
                        Pair(if (appLanguage == "te") "లాభ శాతము" else "Return Margin", "${IndianFormatter.formatNumber(profitPercentage)} %")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
