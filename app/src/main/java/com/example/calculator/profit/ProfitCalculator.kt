package com.example.calculator.profit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*

@Composable
fun ProfitLossCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var totalCost by remember { mutableStateOf("45000") }
    var totalSaleIncome by remember { mutableStateOf("72000") }
    var additionalExpenses by remember { mutableStateOf("3000") }

    var resultProfitAmount by remember { mutableStateOf<Double?>(null) }
    var isProfitStatus by remember { mutableStateOf<Boolean?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val cost = totalCost.toDoubleOrNull()
        val income = totalSaleIncome.toDoubleOrNull()
        val extra = additionalExpenses.toDoubleOrNull() ?: 0.0

        if (cost == null || income == null) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన ఖర్చు మరియు రాబడి మొత్తాలను ఎంటర్ చేయండి." else "Please enter valid cost and income amounts."
            resultProfitAmount = null
            return
        }

        val totalInvestment = cost + extra
        val netDiff = income - totalInvestment

        resultProfitAmount = Math.abs(netDiff)
        isProfitStatus = netDiff >= 0.0
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Profit & Loss Calculator 📈",
                titleTel = "లాభ నష్టాల లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            CurrencyInput(
                value = totalCost,
                onValueChange = { totalCost = it },
                labelEng = "Total Cultivation / Farm Cost",
                labelTel = "మొత్తం పంట సాగు ఖర్చు",
                selectedLang = langState
            )
        }

        item {
            CurrencyInput(
                value = totalSaleIncome,
                onValueChange = { totalSaleIncome = it },
                labelEng = "Total Crop Sale Income",
                labelTel = "పంట అమ్మకం ద్వారా వచ్చిన రాబడి",
                selectedLang = langState
            )
        }

        item {
            CurrencyInput(
                value = additionalExpenses,
                onValueChange = { additionalExpenses = it },
                labelEng = "Extra / Post-Harvest Expenses",
                labelTel = "అదనపు ఖర్చులు",
                selectedLang = langState
            )
        }

        if (errorMessage != null) {
            item { Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    totalCost = ""
                    totalSaleIncome = ""
                    additionalExpenses = "0"
                    resultProfitAmount = null
                    isProfitStatus = null
                }
            )
        }

        if (resultProfitAmount != null && isProfitStatus != null) {
            item {
                val formattedAmount = CalculatorUtils.formatCurrency(resultProfitAmount!!)
                val costVal = (totalCost.toDoubleOrNull() ?: 0.0) + (additionalExpenses.toDoubleOrNull() ?: 0.0)
                val incomeVal = totalSaleIncome.toDoubleOrNull() ?: 0.0

                CalculationResultCard(
                    mainAmountStr = formattedAmount,
                    mainLabelEng = if (isProfitStatus!!) "Net Profit Amount" else "Net Loss Amount",
                    mainLabelTel = if (isProfitStatus!!) "మొత్తం నికర లాభం" else "మొత్తం నికర నష్టం",
                    selectedLang = langState,
                    isProfit = isProfitStatus,
                    breakdown = listOf(
                        Pair("Total Income / మొత్తం రాబడి", CalculatorUtils.formatCurrency(incomeVal)),
                        Pair("Total Expenses / మొత్తం వ్యయం", CalculatorUtils.formatCurrency(costVal)),
                        Pair("Margin % / లాభ శాతం", "${CalculatorUtils.formatNumber(if (costVal > 0) ((incomeVal - costVal) / costVal) * 100 else 0.0)}%")
                    ),
                    onSave = {
                        onSaveResult(
                            "Profit & Loss Calculator",
                            formattedAmount,
                            mapOf("Income" to CalculatorUtils.formatCurrency(incomeVal), "Expenses" to CalculatorUtils.formatCurrency(costVal), "Status" to if (isProfitStatus!!) "PROFIT" else "LOSS")
                        )
                    }
                )
            }
        }
    }
}
