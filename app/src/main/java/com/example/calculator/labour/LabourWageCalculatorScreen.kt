package com.example.calculator.labour

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
fun LabourWageCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var workersInput by remember { mutableStateOf("5") }
    var dailyWageInput by remember { mutableStateOf("500") }
    var daysInput by remember { mutableStateOf("2") }
    var overtimeHoursInput by remember { mutableStateOf("0") }
    var overtimeRateInput by remember { mutableStateOf("0") }

    var baseWageCost by remember { mutableStateOf<Double?>(null) }
    var overtimeCost by remember { mutableStateOf(0.0) }
    var totalLabourCost by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val workers = workersInput.toDoubleOrNull() ?: 0.0
        val wage = dailyWageInput.toDoubleOrNull() ?: 0.0
        val days = daysInput.toDoubleOrNull() ?: 0.0
        val otHours = overtimeHoursInput.toDoubleOrNull() ?: 0.0
        val otRate = overtimeRateInput.toDoubleOrNull() ?: 0.0

        if (workers <= 0 || wage <= 0 || days <= 0) return

        val base = workers * wage * days
        val ot = workers * otHours * otRate
        val total = base + ot

        baseWageCost = base
        overtimeCost = ot
        totalLabourCost = total
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Labour Wage Calculator",
                titleTe = "కూలీల జీతం లెక్కలు",
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
                value = workersInput,
                onValueChange = { workersInput = it },
                labelEn = "Number of Workers",
                labelTe = "కూలీల సంఖ్య",
                appLanguage = appLanguage,
                suffixText = "workers"
            )

            CalculatorNumberInput(
                value = dailyWageInput,
                onValueChange = { dailyWageInput = it },
                labelEn = "Daily Wage per Worker (₹/day)",
                labelTe = "ఒక్క కూలీ రోజువారి బత్తా (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = daysInput,
                onValueChange = { daysInput = it },
                labelEn = "Number of Working Days",
                labelTe = "పనిచేసిన రోజుల సంఖ్య",
                appLanguage = appLanguage,
                suffixText = "days"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalculatorNumberInput(
                    value = overtimeHoursInput,
                    onValueChange = { overtimeHoursInput = it },
                    labelEn = "Optional Overtime (hrs)",
                    labelTe = "ఓవర్‌టైమ్ గంటలు",
                    appLanguage = appLanguage,
                    suffixText = "hrs",
                    modifier = Modifier.weight(1f)
                )

                CalculatorNumberInput(
                    value = overtimeRateInput,
                    onValueChange = { overtimeRateInput = it },
                    labelEn = "Overtime Rate (₹/hr)",
                    labelTe = "ఓవర్‌టైమ్ రేటు (₹/గంట)",
                    appLanguage = appLanguage,
                    prefixSymbol = "₹",
                    modifier = Modifier.weight(1f)
                )
            }

            CalculateButtonRow(
                onReset = {
                    workersInput = ""
                    dailyWageInput = ""
                    daysInput = ""
                    overtimeHoursInput = "0"
                    overtimeRateInput = "0"
                    totalLabourCost = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalLabourCost != null) {
                        onSaveCalculation(
                            "Labour Wage ($workersInput workers, $daysInput days)",
                            "Labour",
                            IndianFormatter.formatCurrency(totalLabourCost!!),
                            "Workers: $workersInput, Wage: ₹$dailyWageInput/day, Days: $daysInput"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalLabourCost?.let { total ->
                ResultCard(
                    mainResultTitleEn = "TOTAL LABOUR PAYABLE WAGE",
                    mainResultTitleTe = "కూలీలకు చెల్లించాల్సిన మొత్తం జీతం",
                    mainResultValue = IndianFormatter.formatCurrency(total),
                    appLanguage = appLanguage,
                    subtitleEn = "$workersInput workers × $daysInput days @ ₹$dailyWageInput / day",
                    subtitleTe = "$workersInput మంది కూలీలు × $daysInput రోజులు @ ₹$dailyWageInput / రోజుకి",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "సాధారణ కూలీ మొత్తం" else "Base Wage Total", IndianFormatter.formatCurrency(baseWageCost ?: 0.0)),
                        Pair(if (appLanguage == "te") "ఓవర్‌టైమ్ మొత్తం" else "Overtime Total", IndianFormatter.formatCurrency(overtimeCost)),
                        Pair(if (appLanguage == "te") "ఒక్కొక్కరికి వచ్చే జీతం" else "Per Worker Payable", IndianFormatter.formatCurrency(total / (workersInput.toDoubleOrNull() ?: 1.0)))
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
