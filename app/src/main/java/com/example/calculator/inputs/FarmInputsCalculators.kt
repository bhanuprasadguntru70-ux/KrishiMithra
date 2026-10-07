package com.example.calculator.inputs

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
fun SeedCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var landArea by remember { mutableStateOf("4") }
    var seedReqPerAcreKg by remember { mutableStateOf("12") }
    var seedPricePerKg by remember { mutableStateOf("180") }

    var resultTotalSeedKg by remember { mutableStateOf<Double?>(null) }
    var resultTotalCost by remember { mutableStateOf<Double?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        val acres = landArea.toDoubleOrNull() ?: 1.0
        val req = seedReqPerAcreKg.toDoubleOrNull() ?: 0.0
        val price = seedPricePerKg.toDoubleOrNull() ?: 0.0

        val totalKg = acres * req
        val totalCost = totalKg * price

        resultTotalSeedKg = totalKg
        resultTotalCost = totalCost
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Seed Requirement & Cost Calculator 🌱",
                titleTel = "విత్తనాల పరిమాణం మరియు ఖర్చు లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            QuantityInput(value = landArea, onValueChange = { landArea = it }, labelEng = "Land Area", labelTel = "భూమి వైశాల్యం", selectedLang = langState, suffix = "acres")
        }

        item {
            QuantityInput(value = seedReqPerAcreKg, onValueChange = { seedReqPerAcreKg = it }, labelEng = "Seed Requirement / Acre", labelTel = "ఎకరానికి విత్తనాల అవసరం", selectedLang = langState, suffix = "kg/acre")
        }

        item {
            CurrencyInput(value = seedPricePerKg, onValueChange = { seedPricePerKg = it }, labelEng = "Seed Price per Kg", labelTel = "కిలో విత్తనం ధర", selectedLang = langState)
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = { resultTotalCost = null }
            )
        }

        if (resultTotalCost != null) {
            item {
                val formattedCost = CalculatorUtils.formatCurrency(resultTotalCost!!)

                CalculationResultCard(
                    mainAmountStr = formattedCost,
                    mainLabelEng = "Total Seed Expenditure",
                    mainLabelTel = "మొత్తం విత్తనాల ఖర్చు",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Total Seed Required / కావలసిన విత్తనాలు", "${CalculatorUtils.formatNumber(resultTotalSeedKg ?: 0.0)} kg"),
                        Pair("Land Area / ఎకరాలు", "$landArea Acres"),
                        Pair("Price per Kg / కిలో ధర", CalculatorUtils.formatCurrency(seedPricePerKg.toDoubleOrNull() ?: 0.0))
                    ),
                    onSave = {
                        onSaveResult(
                            "Seed Calculator",
                            formattedCost,
                            mapOf("Seed Required" to "${resultTotalSeedKg} kg", "Total Cost" to formattedCost)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FertilizerCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var fertName by remember { mutableStateOf("Urea (యూరియా)") }
    var bagsCount by remember { mutableStateOf("8") }
    var pricePerBag by remember { mutableStateOf("267") }

    var resultTotalCost by remember { mutableStateOf<Double?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        val bags = bagsCount.toDoubleOrNull() ?: 0.0
        val price = pricePerBag.toDoubleOrNull() ?: 0.0
        resultTotalCost = bags * price
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Fertilizer Cost Calculator 🧪",
                titleTel = "ఎరువుల ఖర్చు లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            OutlinedTextField(
                value = fertName,
                onValueChange = { fertName = it },
                label = { Text(if (langState == "తెలుగు") "ఎరువు పేరు (उदा. DAP, Urea)" else "Fertilizer Name") },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(value = bagsCount, onValueChange = { bagsCount = it }, labelEng = "Number of Bags", labelTel = "బస్తాల సంఖ్య", selectedLang = langState, suffix = "bags", modifier = Modifier.weight(1f))
                CurrencyInput(value = pricePerBag, onValueChange = { pricePerBag = it }, labelEng = "Price per Bag", labelTel = "బస్తా ధర", selectedLang = langState, modifier = Modifier.weight(1f))
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = { resultTotalCost = null }
            )
        }

        if (resultTotalCost != null) {
            item {
                val formattedCost = CalculatorUtils.formatCurrency(resultTotalCost!!)

                CalculationResultCard(
                    mainAmountStr = formattedCost,
                    mainLabelEng = "Total Fertilizer Expense",
                    mainLabelTel = "మొత్తం ఎరువుల ఖర్చు",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Fertilizer / ఎరువు", fertName),
                        Pair("Bags Count / బస్తాల సంఖ్య", "$bagsCount bags")
                    ),
                    onSave = {
                        onSaveResult(
                            "Fertilizer Calculator",
                            formattedCost,
                            mapOf("Fertilizer" to fertName, "Bags" to bagsCount, "Total Cost" to formattedCost)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun IrrigationCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var hoursPerDay by remember { mutableStateOf("6") }
    var costPerHour by remember { mutableStateOf("95") }
    var totalDays by remember { mutableStateOf("15") }

    var resultTotalWaterCost by remember { mutableStateOf<Double?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        val hrs = hoursPerDay.toDoubleOrNull() ?: 0.0
        val rate = costPerHour.toDoubleOrNull() ?: 0.0
        val days = totalDays.toDoubleOrNull() ?: 0.0

        val totalCost = hrs * rate * days
        resultTotalWaterCost = totalCost
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Irrigation / Water Cost Calculator 💧",
                titleTel = "నీటి పారుదల / పంప్ ఖర్చు లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(value = hoursPerDay, onValueChange = { hoursPerDay = it }, labelEng = "Pump Hours / Day", labelTel = "రోజుకు పంప్ గంటలు", selectedLang = langState, suffix = "hrs", modifier = Modifier.weight(1f))
                CurrencyInput(value = costPerHour, onValueChange = { costPerHour = it }, labelEng = "Fuel/Power Cost (₹/hr)", labelTel = "గంటకు డీజిల్/కరెంట్ ఖర్చు", selectedLang = langState, modifier = Modifier.weight(1f))
            }
        }

        item {
            QuantityInput(value = totalDays, onValueChange = { totalDays = it }, labelEng = "Total Irrigation Days", labelTel = "నీరు పెట్టిన మొత్తం రోజులు", selectedLang = langState, suffix = "days")
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = { resultTotalWaterCost = null }
            )
        }

        if (resultTotalWaterCost != null) {
            item {
                val formattedCost = CalculatorUtils.formatCurrency(resultTotalWaterCost!!)
                val totalHours = (hoursPerDay.toDoubleOrNull() ?: 0.0) * (totalDays.toDoubleOrNull() ?: 0.0)

                CalculationResultCard(
                    mainAmountStr = formattedCost,
                    mainLabelEng = "Total Irrigation Cost",
                    mainLabelTel = "మొత్తం నీటి పారుదల ఖర్చు",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Total Pump Hours / మొత్తం గంటలు", "$totalHours hrs"),
                        Pair("Rate / Hour", CalculatorUtils.formatCurrency(costPerHour.toDoubleOrNull() ?: 0.0))
                    ),
                    onSave = {
                        onSaveResult(
                            "Irrigation Calculator",
                            formattedCost,
                            mapOf("Total Hours" to "$totalHours hrs", "Total Cost" to formattedCost)
                        )
                    }
                )
            }
        }
    }
}
