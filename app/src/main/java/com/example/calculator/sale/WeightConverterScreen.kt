package com.example.calculator.sale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*
import com.example.calculator.util.IndianFormatter
import com.example.calculator.util.WeightUnitConversions

@Composable
fun WeightConverterScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var inputValue by remember { mutableStateOf("100") }
    var selectedSourceUnit by remember { mutableStateOf("kg") }
    var kgPerBagInput by remember { mutableStateOf("50") }

    val rawVal = inputValue.toDoubleOrNull() ?: 0.0
    val bagWt = kgPerBagInput.toDoubleOrNull() ?: 50.0

    val totalKg = WeightUnitConversions.toKilograms(rawVal, selectedSourceUnit, bagWt)
    val totalQuintals = WeightUnitConversions.kgToQuintals(totalKg)
    val totalTonnes = WeightUnitConversions.kgToTonnes(totalKg)
    val totalBags = if (bagWt > 0) totalKg / bagWt else 0.0

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Kg / Quintal / Tonne Converter",
                titleTe = "కిలో / క్వింటాల్ / టన్ను మార్పిడి లెక్కలు",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CalculatorNumberInput(
                value = inputValue,
                onValueChange = { inputValue = it },
                labelEn = "Enter Weight Quantity",
                labelTe = "బరువు పరిమాణం నమోదు చేయండి",
                appLanguage = appLanguage
            )

            Text(
                text = if (appLanguage == "te") "మీరు నమోదు చేసిన కొలత రకం" else "Select Source Weight Unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            CalculatorUnitChipSelector(
                selectedUnit = selectedSourceUnit,
                options = listOf("kg", "quintal", "tonne", "bag"),
                onUnitSelected = { selectedSourceUnit = it }
            )

            if (selectedSourceUnit == "bag") {
                CalculatorNumberInput(
                    value = kgPerBagInput,
                    onValueChange = { kgPerBagInput = it },
                    labelEn = "Bag Standard Weight (kg)",
                    labelTe = "ఒక్క బస్తా బరువు (కిలోలు)",
                    appLanguage = appLanguage,
                    suffixText = "kg"
                )
            }

            ResultCard(
                mainResultTitleEn = "EQUIVALENT WEIGHT CONVERSIONS",
                mainResultTitleTe = "సమానమైన బరువు కొలతలు",
                mainResultValue = "${IndianFormatter.formatNumber(totalQuintals)} Quintals",
                appLanguage = appLanguage,
                subtitleEn = "Live Dynamic Conversion",
                subtitleTe = "వెంటనే లెక్కించే లైవ్ మార్పిడి",
                breakdownItems = listOf(
                    Pair(if (appLanguage == "te") "కిలోగ్రాములు (Kg)" else "Kilograms (Kg)", "${IndianFormatter.formatNumber(totalKg)} kg"),
                    Pair(if (appLanguage == "te") "క్వింటాళ్ళు (Quintal)" else "Quintals (Quintal)", "${IndianFormatter.formatNumber(totalQuintals)} quintals"),
                    Pair(if (appLanguage == "te") "టన్నులు (Tonne)" else "Tonnes (Tonne)", "${IndianFormatter.formatNumber(totalTonnes)} tonnes"),
                    Pair(if (appLanguage == "te") "బస్తాల అంచనా ($bagWt kg/బస్తా)" else "Estimated Bags ($bagWt kg/bag)", "${IndianFormatter.formatNumber(totalBags)} bags")
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
