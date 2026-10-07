package com.example.calculator.crop

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
fun CropCostCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var cropName by remember { mutableStateOf("Tomato") }
    var landArea by remember { mutableStateOf("3") }
    var seedCost by remember { mutableStateOf("4500") }
    var fertilizerCost by remember { mutableStateOf("12000") }
    var pesticideCost by remember { mutableStateOf("8000") }
    var labourCost by remember { mutableStateOf("15000") }
    var machineryCost by remember { mutableStateOf("9000") }
    var irrigationCost by remember { mutableStateOf("3500") }
    var otherCost by remember { mutableStateOf("2000") }

    var resultTotalCost by remember { mutableStateOf<Double?>(null) }
    var resultCostPerAcre by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val acres = landArea.toDoubleOrNull()
        if (acres == null || acres <= 0.0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన పొలం ఎకరాల సంఖ్య ఎంటర్ చేయండి." else "Please enter valid land area in acres."
            resultTotalCost = null
            return
        }

        val total = (seedCost.toDoubleOrNull() ?: 0.0) +
                (fertilizerCost.toDoubleOrNull() ?: 0.0) +
                (pesticideCost.toDoubleOrNull() ?: 0.0) +
                (labourCost.toDoubleOrNull() ?: 0.0) +
                (machineryCost.toDoubleOrNull() ?: 0.0) +
                (irrigationCost.toDoubleOrNull() ?: 0.0) +
                (otherCost.toDoubleOrNull() ?: 0.0)

        resultTotalCost = total
        resultCostPerAcre = total / acres
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Crop Cultivation Cost Calculator 🌾",
                titleTel = "సాగు వ్యయం లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text(if (langState == "తెలుగు") "పంట పేరు" else "Crop Name") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                QuantityInput(
                    value = landArea,
                    onValueChange = { landArea = it },
                    labelEng = "Land Area",
                    labelTel = "ఎకరాలు",
                    selectedLang = langState,
                    suffix = "Acres",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Text("Cost Breakdowns (ఖర్చుల వివరాలు):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KrishiHeaderGreen) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyInput(value = seedCost, onValueChange = { seedCost = it }, labelEng = "Seeds", labelTel = "విత్తనాలు", selectedLang = langState, modifier = Modifier.weight(1f))
                CurrencyInput(value = fertilizerCost, onValueChange = { fertilizerCost = it }, labelEng = "Fertilizer", labelTel = "ఎరువులు", selectedLang = langState, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyInput(value = pesticideCost, onValueChange = { pesticideCost = it }, labelEng = "Pesticides", labelTel = "పురుగు మందులు", selectedLang = langState, modifier = Modifier.weight(1f))
                CurrencyInput(value = labourCost, onValueChange = { labourCost = it }, labelEng = "Labour Wage", labelTel = "కూలీల ఖర్చు", selectedLang = langState, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyInput(value = machineryCost, onValueChange = { machineryCost = it }, labelEng = "Machinery/Tractor", labelTel = "యంత్రాల అద్దె", selectedLang = langState, modifier = Modifier.weight(1f))
                CurrencyInput(value = irrigationCost, onValueChange = { irrigationCost = it }, labelEng = "Irrigation", labelTel = "నీటి పారుదల", selectedLang = langState, modifier = Modifier.weight(1f))
            }
        }

        item {
            CurrencyInput(value = otherCost, onValueChange = { otherCost = it }, labelEng = "Other / Misc Costs", labelTel = "ఇతర ఖర్చులు", selectedLang = langState)
        }

        if (errorMessage != null) {
            item { Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    cropName = ""
                    landArea = "1"
                    seedCost = "0"
                    fertilizerCost = "0"
                    pesticideCost = "0"
                    labourCost = "0"
                    machineryCost = "0"
                    irrigationCost = "0"
                    otherCost = "0"
                    resultTotalCost = null
                }
            )
        }

        if (resultTotalCost != null) {
            item {
                val formattedTotal = CalculatorUtils.formatCurrency(resultTotalCost!!)
                val formattedPerAcre = CalculatorUtils.formatCurrency(resultCostPerAcre ?: 0.0)

                CalculationResultCard(
                    mainAmountStr = formattedTotal,
                    mainLabelEng = "Total Cultivation Cost for $cropName",
                    mainLabelTel = "$cropName సాగు మొత్తం ఖర్చు",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Total Area / భూమి వైశాల్యం", "$landArea Acres"),
                        Pair("Cost Per Acre / ఎకరానికి సాగు వ్యయం", formattedPerAcre)
                    ),
                    onSave = {
                        onSaveResult(
                            "Crop Cultivation Cost Calculator",
                            formattedTotal,
                            mapOf("Crop" to cropName, "Area" to "$landArea Acres", "Total Cost" to formattedTotal, "Cost/Acre" to formattedPerAcre)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun BagsToRupeesCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var bagsCount by remember { mutableStateOf("50") }
    var weightPerBagKg by remember { mutableStateOf("40") }
    var pricePerQuintal by remember { mutableStateOf("2500") }
    var pricePerBagDirect by remember { mutableStateOf("") }

    var resultTotalKg by remember { mutableStateOf<Double?>(null) }
    var resultTotalQuintals by remember { mutableStateOf<Double?>(null) }
    var resultTotalAmount by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val bags = bagsCount.toDoubleOrNull()
        if (bags == null || bags <= 0.0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన బస్తాల సంఖ్య నమోదు చేయండి." else "Please enter valid number of bags."
            resultTotalAmount = null
            return
        }

        if (pricePerBagDirect.isNotBlank()) {
            val directRate = pricePerBagDirect.toDoubleOrNull() ?: 0.0
            resultTotalAmount = bags * directRate
            resultTotalKg = bags * (weightPerBagKg.toDoubleOrNull() ?: 50.0)
            resultTotalQuintals = (resultTotalKg ?: 0.0) / 100.0
            return
        }

        val weightBag = weightPerBagKg.toDoubleOrNull() ?: 50.0
        val priceQtl = pricePerQuintal.toDoubleOrNull() ?: 0.0

        val totalKg = bags * weightBag
        val totalQtl = totalKg / 100.0
        val totalAmount = totalQtl * priceQtl

        resultTotalKg = totalKg
        resultTotalQuintals = totalQtl
        resultTotalAmount = totalAmount
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Bags → Rupees Calculator 📦",
                titleTel = "బస్తాల లెక్కలు (బాగ్స్ → రూ)",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = bagsCount,
                    onValueChange = { bagsCount = it },
                    labelEng = "Number of Bags",
                    labelTel = "బస్తాల సంఖ్య",
                    selectedLang = langState,
                    suffix = "bags",
                    modifier = Modifier.weight(1f)
                )
                QuantityInput(
                    value = weightPerBagKg,
                    onValueChange = { weightPerBagKg = it },
                    labelEng = "Weight per Bag",
                    labelTel = "ఒక్క బస్తా బరువు",
                    selectedLang = langState,
                    suffix = "kg",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CurrencyInput(
                value = pricePerQuintal,
                onValueChange = { pricePerQuintal = it; pricePerBagDirect = "" },
                labelEng = "Market Price per Quintal (₹/100kg)",
                labelTel = "క్వింటాల్ ధర (రూ/క్వింటాల్)",
                selectedLang = langState
            )
        }

        item {
            Text("OR Direct Rate Per Bag (లేదా ఒక బస్తా రేటు):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
        }

        item {
            CurrencyInput(
                value = pricePerBagDirect,
                onValueChange = { pricePerBagDirect = it; pricePerQuintal = "" },
                labelEng = "Direct Price per Bag (Optional)",
                labelTel = "నేరుగా బస్తా రేటు",
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
                    bagsCount = ""
                    weightPerBagKg = "50"
                    pricePerQuintal = "2000"
                    pricePerBagDirect = ""
                    resultTotalAmount = null
                }
            )
        }

        if (resultTotalAmount != null) {
            item {
                val formattedAmount = CalculatorUtils.formatCurrency(resultTotalAmount!!)

                CalculationResultCard(
                    mainAmountStr = formattedAmount,
                    mainLabelEng = "Total Sale Amount for $bagsCount Bags",
                    mainLabelTel = "$bagsCount బస్తాల మొత్తం విక్రయ సొమ్ము",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Total Quantity (kg) / మొత్తం కిలోలు", "${CalculatorUtils.formatNumber(resultTotalKg ?: 0.0)} kg"),
                        Pair("Total Quintals / మొత్తం క్వింటాళ్లు", "${CalculatorUtils.formatNumber(resultTotalQuintals ?: 0.0)} qtl"),
                        Pair("Effective Price/Bag / బస్తా సగటు ధర", CalculatorUtils.formatCurrency(resultTotalAmount!! / (bagsCount.toDoubleOrNull() ?: 1.0)))
                    ),
                    onSave = {
                        onSaveResult(
                            "Bag → Rupees Calculator",
                            formattedAmount,
                            mapOf("Bags" to bagsCount, "Total Quantity" to "${resultTotalQuintals} qtl", "Total Value" to formattedAmount)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SalePriceTargetCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var totalCost by remember { mutableStateOf("50000") }
    var expectedProfit by remember { mutableStateOf("10000") }
    var expectedYieldQtl by remember { mutableStateOf("20") }

    var resultTargetPricePerQtl by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val cost = totalCost.toDoubleOrNull() ?: 0.0
        val profit = expectedProfit.toDoubleOrNull() ?: 0.0
        val qtl = expectedYieldQtl.toDoubleOrNull()

        if (qtl == null || qtl <= 0.0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి ఆశించిన దిగుబడి క్వింటాళ్ల సంఖ్య నమోదు చేయండి." else "Please enter valid expected yield quantity."
            resultTargetPricePerQtl = null
            return
        }

        val requiredPrice = (cost + profit) / qtl
        resultTargetPricePerQtl = requiredPrice
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Sale Price Target Calculator 🎯",
                titleTel = "లక్ష్య అమ్మకం ధర లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            CurrencyInput(
                value = totalCost,
                onValueChange = { totalCost = it },
                labelEng = "Total Cultivation Cost (₹)",
                labelTel = "మొత్తం సాగు ఖర్చు",
                selectedLang = langState
            )
        }

        item {
            CurrencyInput(
                value = expectedProfit,
                onValueChange = { expectedProfit = it },
                labelEng = "Expected Profit Amount (₹)",
                labelTel = "ఆశించిన లాభం",
                selectedLang = langState
            )
        }

        item {
            QuantityInput(
                value = expectedYieldQtl,
                onValueChange = { expectedYieldQtl = it },
                labelEng = "Expected Harvest Yield",
                labelTel = "ఆశించిన దిగుబడి (క్వింటాళ్లు)",
                selectedLang = langState,
                suffix = "quintals"
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
                    totalCost = "50000"
                    expectedProfit = "10000"
                    expectedYieldQtl = "20"
                    resultTargetPricePerQtl = null
                }
            )
        }

        if (resultTargetPricePerQtl != null) {
            item {
                val formattedTarget = CalculatorUtils.formatCurrency(resultTargetPricePerQtl!!)

                CalculationResultCard(
                    mainAmountStr = "$formattedTarget / quintal",
                    mainLabelEng = "Your Target Minimum Sale Price",
                    mainLabelTel = "మీ కనీస లక్ష్య అమ్మకం ధర (లాభం కోసం)",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Total Cost + Expected Profit", CalculatorUtils.formatCurrency((totalCost.toDoubleOrNull() ?: 0.0) + (expectedProfit.toDoubleOrNull() ?: 0.0))),
                        Pair("Total Production Yield", "$expectedYieldQtl Quintals"),
                        Pair("Target Selling Rate", "$formattedTarget / qtl")
                    ),
                    onSave = {
                        onSaveResult(
                            "Target Price Calculator",
                            formattedTarget,
                            mapOf("Cost" to totalCost, "Target Price" to formattedTarget)
                        )
                    }
                )
            }
        }
    }
}
