package com.example.calculator.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KrishiAccentOrange
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen
import java.text.NumberFormat
import java.util.Locale

object CalculatorUtils {
    fun formatCurrency(amount: Double): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 2
            formatter.minimumFractionDigits = 0
            formatter.format(amount)
        } catch (e: Exception) {
            "₹${String.format("%.2f", amount)}"
        }
    }

    fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }
}

@Composable
fun CalculatorHeader(
    titleEng: String,
    titleTel: String,
    selectedLang: String,
    onLangToggle: (String) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (selectedLang == "తెలుగు") titleTel else titleEng,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = KrishiHeaderGreen
            )
            Text(
                text = if (selectedLang == "తెలుగు") titleEng else titleTel,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Language switch inside calculator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEAF5ED))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selectedLang == "English") KrishiHeaderGreen else Color.Transparent)
                        .clickable { onLangToggle("English") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ENG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selectedLang == "English") Color.White else KrishiHeaderGreen)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selectedLang == "తెలుగు") KrishiHeaderGreen else Color.Transparent)
                        .clickable { onLangToggle("తెలుగు") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("తెలుగు", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selectedLang == "తెలుగు") Color.White else KrishiHeaderGreen)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun CurrencyInput(
    value: String,
    onValueChange: (String) -> Unit,
    labelEng: String,
    labelTel: String,
    selectedLang: String,
    modifier: Modifier = Modifier,
    placeholder: String = "0"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { char -> char.isDigit() || char == '.' }) },
        label = { Text(if (selectedLang == "తెలుగు") "$labelTel ($labelEng)" else "$labelEng ($labelTel)", fontSize = 13.sp) },
        leadingIcon = { Text("₹", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = KrishiHeaderGreen) },
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = KrishiHeaderGreen,
            unfocusedBorderColor = Color.LightGray
        ),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun QuantityInput(
    value: String,
    onValueChange: (String) -> Unit,
    labelEng: String,
    labelTel: String,
    selectedLang: String,
    modifier: Modifier = Modifier,
    suffix: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { char -> char.isDigit() || char == '.' }) },
        label = { Text(if (selectedLang == "తెలుగు") "$labelTel ($labelEng)" else "$labelEng ($labelTel)", fontSize = 13.sp) },
        trailingIcon = { if (suffix.isNotEmpty()) Text(suffix, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = KrishiHeaderGreen,
            unfocusedBorderColor = Color.LightGray
        ),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CalculateActionButtons(
    selectedLang: String,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onSave: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(0.35f)
        ) {
            Text(if (selectedLang == "తెలుగు") "రీసెట్" else "RESET", fontSize = 12.sp, color = Color.DarkGray)
        }

        Button(
            onClick = onCalculate,
            colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(0.65f)
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (selectedLang == "తెలుగు") "లెక్కించు (Calculate)" else "CALCULATE NOW",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun CalculationResultCard(
    mainAmountStr: String,
    mainLabelEng: String,
    mainLabelTel: String,
    selectedLang: String,
    breakdown: List<Pair<String, String>> = emptyList(),
    isProfit: Boolean? = null,
    onSave: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (isProfit) {
                true -> Color(0xFFE8F5E9)
                false -> Color(0xFFFFEBEE)
                null -> Color(0xFFEAF5ED)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (isProfit) {
                true -> KrishiPrimaryGreen
                false -> Color.Red.copy(alpha = 0.5f)
                null -> KrishiHeaderGreen.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedLang == "తెలుగు") mainLabelTel else mainLabelEng,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = mainAmountStr,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = when (isProfit) {
                            true -> KrishiHeaderGreen
                            false -> Color(0xFFC62828)
                            null -> KrishiHeaderGreen
                        }
                    )
                }

                if (isProfit != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isProfit) KrishiHeaderGreen else Color(0xFFC62828)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (isProfit) {
                                if (selectedLang == "తెలుగు") "లాభం (PROFIT)" else "PROFIT 📈"
                            } else {
                                if (selectedLang == "తెలుగు") "నష్టం (LOSS)" else "LOSS 📉"
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (breakdown.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.Gray.copy(alpha = 0.2f))

                breakdown.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontSize = 12.sp, color = Color.DarkGray)
                        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
                    }
                }
            }

            if (onSave != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = KrishiPrimaryGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (selectedLang == "తెలుగు") "సేవ్ చేయండి" else "Save Calculation", fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTopHeader(
    titleEn: String,
    titleTe: String,
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = if (appLanguage == "te" || appLanguage == "తెలుగు") titleTe else titleEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = KrishiHeaderGreen
                )
                Text(
                    text = if (appLanguage == "te" || appLanguage == "తెలుగు") titleEn else titleTe,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KrishiHeaderGreen)
            }
        },
        actions = {
            TextButton(
                onClick = {
                    val nextLang = if (appLanguage == "te" || appLanguage == "తెలుగు") "en" else "te"
                    onLanguageSelected(nextLang)
                }
            ) {
                Text(
                    text = if (appLanguage == "te" || appLanguage == "తెలుగు") "English" else "తెలుగు",
                    fontWeight = FontWeight.Bold,
                    color = KrishiHeaderGreen
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun CalculatorNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    labelEn: String = "",
    labelTe: String = "",
    appLanguage: String = "en",
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    prefixSymbol: String? = null,
    suffixText: String? = null,
    isError: Boolean = false,
    label: String = if (appLanguage == "te" || appLanguage == "తెలుగు") labelTe else labelEn
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = {
            val textToDisplay = if (label.isNotBlank()) label else if (appLanguage == "te" || appLanguage == "తెలుగు") labelTe else labelEn
            if (textToDisplay.isNotBlank()) {
                Text(text = textToDisplay, fontSize = 13.sp)
            }
        },
        placeholder = if (placeholder != null) {
            { Text(placeholder, fontSize = 12.sp, color = Color.Gray) }
        } else null,
        isError = isError,
        leadingIcon = if (prefixSymbol != null) {
            { Text(prefixSymbol, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) }
        } else null,
        trailingIcon = if (suffixText != null) {
            { Text(suffixText, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp)) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = KrishiHeaderGreen,
            unfocusedBorderColor = Color.LightGray
        ),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CalculateButtonRow(
    onReset: () -> Unit,
    onCalculate: () -> Unit,
    onSave: (() -> Unit)? = null,
    appLanguage: String = "en"
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(0.3f)
        ) {
            Text(if (appLanguage == "te" || appLanguage == "తెలుగు") "రీసెట్" else "Reset", fontSize = 12.sp, color = Color.DarkGray)
        }

        Button(
            onClick = onCalculate,
            colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(0.7f)
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                if (appLanguage == "te" || appLanguage == "తెలుగు") "లెక్కించు (Calculate)" else "Calculate",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ResultCard(
    mainResultTitleEn: String = "",
    mainResultTitleTe: String = "",
    mainResultValue: String = "",
    appLanguage: String = "en",
    subtitleEn: String? = null,
    subtitleTe: String? = null,
    breakdownItems: List<Pair<String, String>> = emptyList(),
    isProfit: Boolean? = null,
    badgeText: String? = null,
    badgeColor: Color? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (isProfit) {
                true -> Color(0xFFE8F5E9)
                false -> Color(0xFFFFEBEE)
                null -> Color(0xFFEAF5ED)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (isProfit) {
                true -> KrishiPrimaryGreen
                false -> Color.Red.copy(alpha = 0.5f)
                null -> KrishiHeaderGreen.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (badgeText != null) {
                Surface(
                    color = badgeColor ?: KrishiHeaderGreen,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = if (appLanguage == "te" || appLanguage == "తెలుగు") mainResultTitleTe else mainResultTitleEn,
                fontSize = 12.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = mainResultValue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = when (isProfit) {
                    true -> KrishiHeaderGreen
                    false -> Color(0xFFC62828)
                    null -> KrishiHeaderGreen
                }
            )

            if (subtitleEn != null || subtitleTe != null) {
                val sub = if (appLanguage == "te" || appLanguage == "తెలుగు") (subtitleTe ?: subtitleEn) else (subtitleEn ?: subtitleTe)
                Text(sub ?: "", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
            }

            if (breakdownItems.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))
                breakdownItems.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontSize = 12.sp, color = Color.DarkGray)
                        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiHeaderGreen)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorUnitChipSelector(
    selectedUnit: String = "",
    onUnitSelected: (String) -> Unit = {},
    units: List<String> = emptyList(),
    options: List<String> = emptyList(),
    appLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val listToUse = if (options.isNotEmpty()) options else units
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listToUse.forEach { unit ->
            FilterChip(
                selected = selectedUnit == unit,
                onClick = { onUnitSelected(unit) },
                label = { Text(unit, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KrishiHeaderGreen,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun SavedHistoryItemCard(
    item: com.example.data.model.SavedCalculationEntity = com.example.data.model.SavedCalculationEntity(title = "", calculatorType = "", resultFormatted = "", summaryDetails = ""),
    calculation: com.example.data.model.SavedCalculationEntity = item,
    onDelete: Any = {}
) {
    val calc = if (item.title.isNotBlank()) item else calculation
    val deleteAction: () -> Unit = when (onDelete) {
        is Function0<*> -> { { (onDelete as () -> Unit)() } }
        is Function1<*, *> -> { { (onDelete as (Int) -> Unit)(calc.id) } }
        else -> { {} }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(calc.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KrishiHeaderGreen)
                Text(calc.resultFormatted, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = KrishiPrimaryGreen)
                if (calc.summaryDetails.isNotBlank()) {
                    Text(calc.summaryDetails, fontSize = 11.sp, color = Color.Gray)
                }
            }
            IconButton(onClick = deleteAction) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}
