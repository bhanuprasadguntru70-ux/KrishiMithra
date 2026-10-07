package com.example.calculator.tractor

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
import com.example.calculator.util.LandUnitConversions

@Composable
fun TractorAreaCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var areaInput by remember { mutableStateOf("2") }
    var selectedUnit by remember { mutableStateOf(LandUnitConversions.UNIT_ACRE) }
    var workingHoursInput by remember { mutableStateOf("4") }
    var hourlyRateInput by remember { mutableStateOf("800") }

    var totalCost by remember { mutableStateOf<Double?>(null) }
    var areaInAcres by remember { mutableStateOf(0.0) }
    var costPerAcre by remember { mutableStateOf(0.0) }

    fun calculate() {
        val rawArea = areaInput.toDoubleOrNull() ?: 0.0
        val hrs = workingHoursInput.toDoubleOrNull() ?: 0.0
        val rate = hourlyRateInput.toDoubleOrNull() ?: 0.0

        if (rawArea <= 0 || rate <= 0) return

        val acres = LandUnitConversions.toAcres(rawArea, selectedUnit)
        val total = hrs * rate
        val perAcre = if (acres > 0) total / acres else 0.0

        areaInAcres = acres
        totalCost = total
        costPerAcre = perAcre
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Tractor Area Cost Calculator",
                titleTe = "ఎకరం ట్రాక్టర్ దుక్కి లెక్కలు",
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
                value = areaInput,
                onValueChange = { areaInput = it },
                labelEn = "Land Area",
                labelTe = "భూమి విస్తీర్ణం",
                appLanguage = appLanguage
            )

            Text(
                text = if (appLanguage == "te") "కొలత ప్రమాణం ఎంచుకోండి" else "Select Land Unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            CalculatorUnitChipSelector(
                selectedUnit = selectedUnit,
                options = LandUnitConversions.SUPPORTED_UNITS,
                onUnitSelected = { selectedUnit = it }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalculatorNumberInput(
                    value = workingHoursInput,
                    onValueChange = { workingHoursInput = it },
                    labelEn = "Working Hours",
                    labelTe = "పట్టే సమయం (గంటలు)",
                    appLanguage = appLanguage,
                    suffixText = "hrs",
                    modifier = Modifier.weight(1f)
                )

                CalculatorNumberInput(
                    value = hourlyRateInput,
                    onValueChange = { hourlyRateInput = it },
                    labelEn = "Rate per Hour (₹)",
                    labelTe = "గంటకు రేటు (₹)",
                    appLanguage = appLanguage,
                    prefixSymbol = "₹",
                    modifier = Modifier.weight(1f)
                )
            }

            CalculateButtonRow(
                onReset = {
                    areaInput = ""
                    workingHoursInput = ""
                    hourlyRateInput = ""
                    totalCost = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalCost != null) {
                        onSaveCalculation(
                            "Tractor Area ($areaInput $selectedUnit)",
                            "Land & Machinery",
                            IndianFormatter.formatCurrency(totalCost!!),
                            "Area: $areaInput $selectedUnit (${IndianFormatter.formatNumber(areaInAcres)} Acres), Cost/Acre: ${IndianFormatter.formatCurrency(costPerAcre)}"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalCost?.let { total ->
                ResultCard(
                    mainResultTitleEn = "TOTAL TRACTOR PLOUGHING COST",
                    mainResultTitleTe = "మొత్తం దుక్కి ఖర్చు",
                    mainResultValue = IndianFormatter.formatCurrency(total),
                    appLanguage = appLanguage,
                    subtitleEn = "Cost per Acre: ${IndianFormatter.formatCurrency(costPerAcre)}",
                    subtitleTe = "ఎకరానికి సగటు ఖర్చు: ${IndianFormatter.formatCurrency(costPerAcre)}",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "విస్తీర్ణం (ఎకరాల్లో)" else "Converted Area (Acres)", "${IndianFormatter.formatNumber(areaInAcres)} Acres"),
                        Pair(if (appLanguage == "te") "పనిచేసిన గంటలు" else "Working Hours", "$workingHoursInput hrs"),
                        Pair(if (appLanguage == "te") "గంటకు ధర" else "Rate/Hour", "₹$hourlyRateInput/hr")
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
