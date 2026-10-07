package com.example.calculator.labour

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
fun DailyWorkCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var workerName by remember { mutableStateOf("Raju / రాము") }
    var workType by remember { mutableStateOf("Weeding / నాట్లు / కోతలు") }
    var totalHoursInput by remember { mutableStateOf("8") }
    var breakMinutesInput by remember { mutableStateOf("60") }
    var hourlyRateInput by remember { mutableStateOf("75") }

    var netWorkingHours by remember { mutableStateOf<Double?>(null) }
    var totalPayable by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val grossHours = totalHoursInput.toDoubleOrNull() ?: 0.0
        val breakMins = breakMinutesInput.toDoubleOrNull() ?: 0.0
        val rate = hourlyRateInput.toDoubleOrNull() ?: 0.0

        if (grossHours <= 0 || rate <= 0) return

        val netHrs = grossHours - (breakMins / 60.0)
        val finalNet = if (netHrs > 0) netHrs else 0.0
        val payable = finalNet * rate

        netWorkingHours = finalNet
        totalPayable = payable
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Farmer Daily Work Calculator",
                titleTe = "దినసరి పని & సమయ లెక్కలు",
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
                    text = if (appLanguage == "te") "కూలీ పేరు (ఐచ్ఛికం)" else "Worker Name (Optional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = workerName,
                    onValueChange = { workerName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Column {
                Text(
                    text = if (appLanguage == "te") "పని రకం (ఉదా: నాట్లు, కోతలు, కలుపు)" else "Work Type (e.g. Sowing, Harvesting)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = workType,
                    onValueChange = { workType = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalculatorNumberInput(
                    value = totalHoursInput,
                    onValueChange = { totalHoursInput = it },
                    labelEn = "Total Hours on Site",
                    labelTe = "మొత్తం గడిపిన గంటలు",
                    appLanguage = appLanguage,
                    suffixText = "hrs",
                    modifier = Modifier.weight(1f)
                )

                CalculatorNumberInput(
                    value = breakMinutesInput,
                    onValueChange = { breakMinutesInput = it },
                    labelEn = "Break Time (Minutes)",
                    labelTe = "విశ్రాంతి నిమిషాలు",
                    appLanguage = appLanguage,
                    suffixText = "mins",
                    modifier = Modifier.weight(1f)
                )
            }

            CalculatorNumberInput(
                value = hourlyRateInput,
                onValueChange = { hourlyRateInput = it },
                labelEn = "Hourly Rate (₹/hour)",
                labelTe = "గంటకు కూలీ ధర (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    totalHoursInput = ""
                    breakMinutesInput = "0"
                    hourlyRateInput = ""
                    totalPayable = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalPayable != null) {
                        onSaveCalculation(
                            "Daily Work ($workerName - $workType)",
                            "Labour",
                            IndianFormatter.formatCurrency(totalPayable!!),
                            "Net Hours: ${IndianFormatter.formatNumber(netWorkingHours ?: 0.0)} hrs, Rate: ₹$hourlyRateInput/hr"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalPayable?.let { payable ->
                ResultCard(
                    mainResultTitleEn = "TOTAL PAYABLE AMOUNT FOR WORKER",
                    mainResultTitleTe = "కూలీకి చెల్లించాల్సిన నికర సొమ్ము",
                    mainResultValue = IndianFormatter.formatCurrency(payable),
                    appLanguage = appLanguage,
                    subtitleEn = "Net Work Time: ${IndianFormatter.formatNumber(netWorkingHours ?: 0.0)} Hours @ ₹$hourlyRateInput / hr",
                    subtitleTe = "నికర పని సమయం: ${IndianFormatter.formatNumber(netWorkingHours ?: 0.0)} గంటలు @ ₹$hourlyRateInput / గంట",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "కూలీ పేరు" else "Worker Name", workerName),
                        Pair(if (appLanguage == "te") "పని వివరాలు" else "Work Type", workType),
                        Pair(if (appLanguage == "te") "విశ్రాంతి తీసివేసిన నికర సమయం" else "Net Working Hours", "${IndianFormatter.formatNumber(netWorkingHours ?: 0.0)} hrs")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
