package com.example.calculator.weight

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
import com.example.calculator.models.WeightUnit

@Composable
fun WeightConverterScreen(
    selectedLang: String,
    onClose: () -> Unit
) {
    var inputValue by remember { mutableStateOf("2500") }
    var inputUnit by remember { mutableStateOf(WeightUnit.KG) }

    var kgVal by remember { mutableStateOf(2500.0) }
    var qtlVal by remember { mutableStateOf(25.0) }
    var tonneVal by remember { mutableStateOf(2.5) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun updateConversion(valueStr: String, unit: WeightUnit) {
        inputValue = valueStr
        inputUnit = unit
        val num = valueStr.toDoubleOrNull() ?: 0.0

        val inKg = num * unit.toKgRatio
        kgVal = inKg
        qtlVal = inKg / 100.0
        tonneVal = inKg / 1000.0
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Agricultural Weight Converter ⚖️",
                titleTel = "వ్యవసాయ బరువుల మార్పిడి (కిలో/క్వింటాల్/టన్ను)",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            QuantityInput(
                value = inputValue,
                onValueChange = { updateConversion(it, inputUnit) },
                labelEng = "Enter Value to Convert",
                labelTel = "మార్చవలసిన బరువు ఎంటర్ చేయండి",
                selectedLang = langState
            )
        }

        item {
            Text("Select Input Unit (యూనిట్ ఎంచుకోండి):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeightUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = inputUnit == unit,
                        onClick = { updateConversion(inputValue, unit) },
                        label = { Text(unit.name, fontSize = 12.sp) }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF5ED)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Instant Conversion Results (తక్షణ మార్పిడి ఫలితం):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = com.example.ui.theme.KrishiHeaderGreen)
                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kilograms (kg) / కిలోలు:", fontSize = 13.sp)
                        Text("${CalculatorUtils.formatNumber(kgVal)} kg", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = com.example.ui.theme.KrishiHeaderGreen)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Quintals (qtl) / క్వింటాళ్లు:", fontSize = 13.sp)
                        Text("${CalculatorUtils.formatNumber(qtlVal)} qtl", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = com.example.ui.theme.KrishiHeaderGreen)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tonnes (t) / టన్నులు:", fontSize = 13.sp)
                        Text("${CalculatorUtils.formatNumber(tonneVal)} tonnes", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = com.example.ui.theme.KrishiHeaderGreen)
                    }
                }
            }
        }
    }
}
