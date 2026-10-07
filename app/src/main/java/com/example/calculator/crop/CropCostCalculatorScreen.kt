package com.example.calculator.crop

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
fun CropCostCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var cropName by remember { mutableStateOf("Maize / జొన్న/మొక్కజొన్న") }
    var landAreaInput by remember { mutableStateOf("3") }
    var seedCostInput by remember { mutableStateOf("4500") }
    var fertilizerCostInput by remember { mutableStateOf("8000") }
    var pesticideCostInput by remember { mutableStateOf("3500") }
    var labourCostInput by remember { mutableStateOf("12000") }
    var machineryCostInput by remember { mutableStateOf("6000") }
    var irrigationCostInput by remember { mutableStateOf("2500") }
    var otherCostInput by remember { mutableStateOf("1500") }

    var totalCost by remember { mutableStateOf<Double?>(null) }
    var costPerAcre by remember { mutableStateOf(0.0) }

    fun calculate() {
        val seed = seedCostInput.toDoubleOrNull() ?: 0.0
        val fert = fertilizerCostInput.toDoubleOrNull() ?: 0.0
        val pest = pesticideCostInput.toDoubleOrNull() ?: 0.0
        val labour = labourCostInput.toDoubleOrNull() ?: 0.0
        val mach = machineryCostInput.toDoubleOrNull() ?: 0.0
        val irrig = irrigationCostInput.toDoubleOrNull() ?: 0.0
        val other = otherCostInput.toDoubleOrNull() ?: 0.0
        val acres = landAreaInput.toDoubleOrNull() ?: 1.0

        val total = seed + fert + pest + labour + mach + irrig + other
        totalCost = total
        costPerAcre = if (acres > 0) total / acres else total
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Crop Cultivation Cost Calculator",
                titleTe = "పంట సాగు వ్యయ లెక్కలు",
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CalculatorNumberInput(
                value = landAreaInput,
                onValueChange = { landAreaInput = it },
                labelEn = "Land Area (Acres)",
                labelTe = "సాగు విస్తీర్ణం (ఎకరాలు)",
                appLanguage = appLanguage,
                suffixText = "Acres"
            )

            CalculatorNumberInput(
                value = seedCostInput,
                onValueChange = { seedCostInput = it },
                labelEn = "Seed Cost (₹)",
                labelTe = "విత్తనాల ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = fertilizerCostInput,
                onValueChange = { fertilizerCostInput = it },
                labelEn = "Fertilizer Cost (₹)",
                labelTe = "ఎరువుల ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = pesticideCostInput,
                onValueChange = { pesticideCostInput = it },
                labelEn = "Pesticide / Spray Cost (₹)",
                labelTe = "పురుగు మందుల ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = labourCostInput,
                onValueChange = { labourCostInput = it },
                labelEn = "Labour Cost (₹)",
                labelTe = "కూలీల ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = machineryCostInput,
                onValueChange = { machineryCostInput = it },
                labelEn = "Tractor / Machinery Cost (₹)",
                labelTe = "యంత్రాలు / ట్రాక్టర్ ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = irrigationCostInput,
                onValueChange = { irrigationCostInput = it },
                labelEn = "Irrigation / Electricity / Diesel Cost (₹)",
                labelTe = "నీటిపారుదల ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = otherCostInput,
                onValueChange = { otherCostInput = it },
                labelEn = "Other Misc Expenses (₹)",
                labelTe = "ఇతర చిన్న ఖర్చులు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    seedCostInput = "0"
                    fertilizerCostInput = "0"
                    pesticideCostInput = "0"
                    labourCostInput = "0"
                    machineryCostInput = "0"
                    irrigationCostInput = "0"
                    otherCostInput = "0"
                    totalCost = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (totalCost != null) {
                        onSaveCalculation(
                            "Crop Cost ($landAreaInput Acres)",
                            "Crop",
                            IndianFormatter.formatCurrency(totalCost!!),
                            "Area: $landAreaInput Acres, Cost/Acre: ${IndianFormatter.formatCurrency(costPerAcre)}"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            totalCost?.let { total ->
                ResultCard(
                    mainResultTitleEn = "TOTAL CULTIVATION COST",
                    mainResultTitleTe = "మొత్తం సాగు ఖర్చు",
                    mainResultValue = IndianFormatter.formatCurrency(total),
                    appLanguage = appLanguage,
                    subtitleEn = "Cost Per Acre: ${IndianFormatter.formatCurrency(costPerAcre)}",
                    subtitleTe = "ఎకరానికి సగటు ఖర్చు: ${IndianFormatter.formatCurrency(costPerAcre)}",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "విత్తనాలు" else "Seed Cost", IndianFormatter.formatCurrency(seedCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "ఎరువులు" else "Fertilizer Cost", IndianFormatter.formatCurrency(fertilizerCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "పురుగు మందులు" else "Pesticide Cost", IndianFormatter.formatCurrency(pesticideCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "కూలీలు" else "Labour Cost", IndianFormatter.formatCurrency(labourCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "యంత్రాలు" else "Machinery Cost", IndianFormatter.formatCurrency(machineryCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "నీటిపారుదల" else "Irrigation Cost", IndianFormatter.formatCurrency(irrigationCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "ఇతర ఖర్చులు" else "Other Costs", IndianFormatter.formatCurrency(otherCostInput.toDoubleOrNull() ?: 0.0))
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
