package com.example.calculator.land

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
import com.example.calculator.models.LandUnit

@Composable
fun CostPerAcreCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var totalExpense by remember { mutableStateOf("60000") }
    var landArea by remember { mutableStateOf("2.5") }
    var selectedUnit by remember { mutableStateOf(LandUnit.ACRES) }

    var resultCostPerAcre by remember { mutableStateOf<Double?>(null) }
    var resultCostPerHectare by remember { mutableStateOf<Double?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        val total = totalExpense.toDoubleOrNull() ?: 0.0
        val area = landArea.toDoubleOrNull() ?: 1.0

        val acres = area * selectedUnit.toAcresRatio
        val costPerAcre = total / acres
        val costPerHectare = total / (acres / 2.47105)

        resultCostPerAcre = costPerAcre
        resultCostPerHectare = costPerHectare
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Cost Per Acre Calculator 🏞️",
                titleTel = "ఎకరానికి సగటు ఖర్చు లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            CurrencyInput(value = totalExpense, onValueChange = { totalExpense = it }, labelEng = "Total Expense / Investment", labelTel = "మొత్తం సాగు పెట్టుబడి", selectedLang = langState)
        }

        item {
            QuantityInput(value = landArea, onValueChange = { landArea = it }, labelEng = "Total Area Value", labelTel = "భూమి ప్రమాణం విలువ", selectedLang = langState)
        }

        item {
            Text("Select Unit (యూనిట్ ప్రమాణం):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LandUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = selectedUnit == unit,
                        onClick = { selectedUnit = unit },
                        label = { Text(unit.name, fontSize = 12.sp) }
                    )
                }
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = { resultCostPerAcre = null }
            )
        }

        if (resultCostPerAcre != null) {
            item {
                val formattedPerAcre = CalculatorUtils.formatCurrency(resultCostPerAcre!!)
                val formattedPerHectare = CalculatorUtils.formatCurrency(resultCostPerHectare ?: 0.0)

                CalculationResultCard(
                    mainAmountStr = "$formattedPerAcre / acre",
                    mainLabelEng = "Cost Per Acre Average",
                    mainLabelTel = "ఎకరానికి అయ్యే సగటు సాగు వ్యయం",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Cost Per Hectare / హెక్టారుకు ఖర్చు", "$formattedPerHectare / ha"),
                        Pair("Entered Total Cost / మొత్తం ఖర్చు", CalculatorUtils.formatCurrency(totalExpense.toDoubleOrNull() ?: 0.0))
                    ),
                    onSave = {
                        onSaveResult(
                            "Cost Per Acre Calculator",
                            formattedPerAcre,
                            mapOf("Cost/Acre" to formattedPerAcre, "Cost/Hectare" to formattedPerHectare)
                        )
                    }
                )
            }
        }
    }
}
