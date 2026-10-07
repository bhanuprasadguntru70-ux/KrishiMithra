package com.example.calculator.labour

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
import com.example.ui.theme.KrishiHeaderGreen

@Composable
fun LabourWageCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var workersCount by remember { mutableStateOf("5") }
    var dailyWage by remember { mutableStateOf("500") }
    var daysCount by remember { mutableStateOf("3") }
    var overtimeHours by remember { mutableStateOf("0") }
    var overtimeRate by remember { mutableStateOf("80") }

    var resultTotalWage by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val workers = workersCount.toDoubleOrNull()
        val wage = dailyWage.toDoubleOrNull()
        val days = daysCount.toDoubleOrNull() ?: 1.0

        if (workers == null || workers <= 0.0 || wage == null || wage <= 0.0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన కూలీల సంఖ్య మరియు రోజువారీ కూలీ నమోదు చేయండి." else "Please enter valid workers count and daily wage."
            resultTotalWage = null
            return
        }

        val baseTotal = workers * wage * days
        val otHrs = overtimeHours.toDoubleOrNull() ?: 0.0
        val otRate = overtimeRate.toDoubleOrNull() ?: 0.0
        val otTotal = workers * otHrs * otRate

        resultTotalWage = baseTotal + otTotal
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Labour Wage Calculator 👨‍🌾",
                titleTel = "కూలీల లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = workersCount,
                    onValueChange = { workersCount = it },
                    labelEng = "Number of Workers",
                    labelTel = "కూలీల సంఖ్య",
                    selectedLang = langState,
                    modifier = Modifier.weight(1f)
                )
                CurrencyInput(
                    value = dailyWage,
                    onValueChange = { dailyWage = it },
                    labelEng = "Daily Wage (₹/day)",
                    labelTel = "రోజువారీ కూలీ",
                    selectedLang = langState,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            QuantityInput(
                value = daysCount,
                onValueChange = { daysCount = it },
                labelEng = "Number of Days",
                labelTel = "పనిచేసిన రోజులు",
                selectedLang = langState,
                suffix = "days"
            )
        }

        item { Text("Optional Overtime (ఐచ్ఛిక ఓవర్‌టైమ్):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = overtimeHours,
                    onValueChange = { overtimeHours = it },
                    labelEng = "OT Hours per Worker",
                    labelTel = "ఓవర్‌టైమ్ గంటలు",
                    selectedLang = langState,
                    suffix = "hrs",
                    modifier = Modifier.weight(1f)
                )
                CurrencyInput(
                    value = overtimeRate,
                    onValueChange = { overtimeRate = it },
                    labelEng = "OT Rate (₹/hr)",
                    labelTel = "OT గంట రేటు",
                    selectedLang = langState,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (errorMessage != null) {
            item { Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    workersCount = ""
                    dailyWage = ""
                    daysCount = "1"
                    overtimeHours = "0"
                    resultTotalWage = null
                }
            )
        }

        if (resultTotalWage != null) {
            item {
                val formattedTotal = CalculatorUtils.formatCurrency(resultTotalWage!!)
                val workers = workersCount.toDoubleOrNull() ?: 1.0
                val wage = dailyWage.toDoubleOrNull() ?: 0.0
                val days = daysCount.toDoubleOrNull() ?: 1.0

                CalculationResultCard(
                    mainAmountStr = formattedTotal,
                    mainLabelEng = "Total Payable Wage",
                    mainLabelTel = "చెల్లించవలసిన మొత్తం కూలీ సొమ్ము",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Workers Count / కూలీల సంఖ్య", "$workersCount workers"),
                        Pair("Daily Rate per Worker", CalculatorUtils.formatCurrency(wage)),
                        Pair("Working Days / రోజులు", "$daysCount days"),
                        Pair("Base Wages / సాధారణ కూలీ", CalculatorUtils.formatCurrency(workers * wage * days)),
                        Pair("Overtime Total / ఓవర్‌టైమ్ మొత్తం", CalculatorUtils.formatCurrency(workers * (overtimeHours.toDoubleOrNull() ?: 0.0) * (overtimeRate.toDoubleOrNull() ?: 0.0)))
                    ),
                    onSave = {
                        onSaveResult(
                            "Labour Wage Calculator",
                            formattedTotal,
                            mapOf("Workers" to workersCount, "Days" to daysCount, "Total Wage" to formattedTotal)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FarmerDailyWorkCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var workerName by remember { mutableStateOf("Srinivas") }
    var workType by remember { mutableStateOf("Weeding / కలుపు తీత") }
    var startHour by remember { mutableStateOf("8") }
    var endHour by remember { mutableStateOf("17") }
    var breakMins by remember { mutableStateOf("60") }
    var hourlyRate by remember { mutableStateOf("70") }

    var resultWorkingHours by remember { mutableStateOf<Double?>(null) }
    var resultTotalPayable by remember { mutableStateOf<Double?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        val start = startHour.toDoubleOrNull() ?: 8.0
        val end = endHour.toDoubleOrNull() ?: 17.0
        val breakH = (breakMins.toDoubleOrNull() ?: 60.0) / 60.0
        val rate = hourlyRate.toDoubleOrNull() ?: 70.0

        val netHrs = Math.max(0.0, (end - start) - breakH)
        val payable = netHrs * rate

        resultWorkingHours = netHrs
        resultTotalPayable = payable
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Daily Work & Hourly Calculator 👷",
                titleTel = "రోజువారీ పని గంటల లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            OutlinedTextField(
                value = workerName,
                onValueChange = { workerName = it },
                label = { Text(if (langState == "తెలుగు") "కూలీ/పనిమనిషి పేరు" else "Worker Name") },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(value = startHour, onValueChange = { startHour = it }, labelEng = "Start Hour (24h)", labelTel = "ప్రారంభ గంట", selectedLang = langState, modifier = Modifier.weight(1f))
                QuantityInput(value = endHour, onValueChange = { endHour = it }, labelEng = "End Hour (24h)", labelTel = "ముగింపు గంట", selectedLang = langState, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(value = breakMins, onValueChange = { breakMins = it }, labelEng = "Break (mins)", labelTel = "విరామం (నిమి)", selectedLang = langState, modifier = Modifier.weight(1f))
                CurrencyInput(value = hourlyRate, onValueChange = { hourlyRate = it }, labelEng = "Hourly Rate", labelTel = "గంట కూలీ", selectedLang = langState, modifier = Modifier.weight(1f))
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = { resultTotalPayable = null }
            )
        }

        if (resultTotalPayable != null) {
            item {
                val formattedPayable = CalculatorUtils.formatCurrency(resultTotalPayable!!)

                CalculationResultCard(
                    mainAmountStr = formattedPayable,
                    mainLabelEng = "Total Daily Payable to $workerName",
                    mainLabelTel = "$workerName కు ఇవ్వవలసిన దినసరి కూలీ",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Net Working Hours / పని గంటలు", "${CalculatorUtils.formatNumber(resultWorkingHours ?: 0.0)} hrs"),
                        Pair("Hourly Wage Rate / గంట రేటు", "₹$hourlyRate / hr")
                    ),
                    onSave = {
                        onSaveResult(
                            "Daily Work Calculator",
                            formattedPayable,
                            mapOf("Worker" to workerName, "Hours" to "${resultWorkingHours} hrs", "Payable" to formattedPayable)
                        )
                    }
                )
            }
        }
    }
}
