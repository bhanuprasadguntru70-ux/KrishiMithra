package com.example.calculator.financial

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.CalculatorNumberInput
import com.example.calculator.components.CalculatorTopHeader
import com.example.calculator.util.IndianFormatter
import kotlin.math.pow

@Composable
fun InterestCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (String, String, String, String) -> Unit,
    onRecordInMoneyBook: ((type: String, amount: Double, desc: String) -> Unit)? = null,
    onBack: () -> Unit
) {
    var interestType by remember { mutableStateOf("SIMPLE") } // "SIMPLE" or "COMPOUND"
    var principalInput by remember { mutableStateOf("50000") }
    var rateInput by remember { mutableStateOf("10") }
    var timeInput by remember { mutableStateOf("2") }
    var timeUnit by remember { mutableStateOf("YEARS") } // "MONTHS" or "YEARS"
    
    // Compounding frequency: 12 = Monthly, 4 = Quarterly, 2 = Half-yearly, 1 = Yearly
    var compoundingFreq by remember { mutableStateOf(1) } // Default Yearly

    // Parse inputs safely
    val principal = principalInput.toDoubleOrNull() ?: 0.0
    val rate = rateInput.toDoubleOrNull() ?: 0.0
    val timeValue = timeInput.toDoubleOrNull() ?: 0.0

    // Time in years
    val timeInYears = if (timeUnit == "MONTHS") timeValue / 12.0 else timeValue

    // Calculation
    val interestEarned: Double
    val totalAmount: Double

    if (interestType == "SIMPLE") {
        interestEarned = (principal * rate * timeInYears) / 100.0
        totalAmount = principal + interestEarned
    } else {
        // Compound Interest
        val n = compoundingFreq.toDouble()
        val r = rate / 100.0
        if (principal > 0 && r > 0 && timeInYears > 0) {
            totalAmount = principal * (1.0 + (r / n)).pow(n * timeInYears)
            interestEarned = totalAmount - principal
        } else {
            interestEarned = 0.0
            totalAmount = principal
        }
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Interest Calculator",
                titleTe = "వడ్డీ లెక్కలు",
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Calculator Mode Switcher
            TabRow(
                selectedTabIndex = if (interestType == "SIMPLE") 0 else 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("interest_type_tabs")
            ) {
                Tab(
                    selected = interestType == "SIMPLE",
                    onClick = { interestType = "SIMPLE" },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "సాధారణ వడ్డీ (Simple)" else "Simple Interest",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = interestType == "COMPOUND",
                    onClick = { interestType = "COMPOUND" },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "చక్రవడ్డీ (Compound)" else "Compound Interest",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Principal
                    CalculatorNumberInput(
                        value = principalInput,
                        onValueChange = { principalInput = it },
                        labelEn = "Principal Amount (₹)",
                        labelTe = "అసలు / మూలధనం (₹)",
                        appLanguage = appLanguage,
                        prefixSymbol = "₹",
                        placeholder = "50,000",
                        modifier = Modifier.testTag("principal_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interest Rate
                    CalculatorNumberInput(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        labelEn = "Interest Rate (% per year)",
                        labelTe = "వడ్డీ రేటు (% ఏడాదికి)",
                        appLanguage = appLanguage,
                        suffixText = "%",
                        placeholder = "10",
                        modifier = Modifier.testTag("rate_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time Value & Unit Selector
                    Text(
                        text = if (appLanguage == "te") "కాలవ్యవధి (Time Period)" else "Time Period",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = timeInput,
                            onValueChange = { timeInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("time_input"),
                            placeholder = { Text("2") },
                            singleLine = true
                        )

                        // Months / Years Switch
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        ) {
                            Surface(
                                color = if (timeUnit == "MONTHS") MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clickable { timeUnit = "MONTHS" }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (appLanguage == "te") "నెలులు" else "Months",
                                    fontSize = 12.sp,
                                    color = if (timeUnit == "MONTHS") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = if (timeUnit == "YEARS") MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clickable { timeUnit = "YEARS" }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (appLanguage == "te") "సంవత్సరాలు" else "Years",
                                    fontSize = 12.sp,
                                    color = if (timeUnit == "YEARS") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Compounding Frequency (only if COMPOUND)
                    AnimatedVisibility(visible = interestType == "COMPOUND") {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = if (appLanguage == "te") "చక్రవడ్డీ గడువు (Compounding Frequency)" else "Compounding Frequency",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val options = listOf(
                                    Triple(12, "Monthly", "నెలవారీ"),
                                    Triple(4, "Quarterly", "త్రైమాసికం"),
                                    Triple(2, "Half-Yearly", "అర-సంవత్సరం"),
                                    Triple(1, "Yearly", "ఏడాదికి")
                                )

                                options.forEach { (freq, labelEn, labelTe) ->
                                    val isSel = compoundingFreq == freq
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { compoundingFreq = freq },
                                        label = {
                                            Text(
                                                text = if (appLanguage == "te") labelTe else labelEn,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (appLanguage == "te") "లెక్కించిన వివరాలు (Calculation Result)" else "Calculation Summary",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1: Principal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (appLanguage == "te") "అసలు మొత్తము:" else "Principal Amount:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(principal),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: Interest
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (appLanguage == "te") "లభించు వడ్డీ:" else "Interest Earned:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(interestEarned),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Row 3: Total Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage == "te") "మొత్తం చెల్లించాల్సింది/లభించేది:" else "Total Amount:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(totalAmount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: Save & Record in My Book
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val typeTitle = if (interestType == "SIMPLE") "Simple Interest" else "Compound Interest"
                                onSaveCalculation(
                                    "$typeTitle (P: ${IndianFormatter.formatCurrency(principal)})",
                                    "Interest Calculator",
                                    IndianFormatter.formatCurrency(totalAmount),
                                    "Principal: ${IndianFormatter.formatCurrency(principal)}, Interest: ${IndianFormatter.formatCurrency(interestEarned)}, Rate: $rate%, Time: $timeValue $timeUnit"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_calc_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (appLanguage == "te") "దాచుకోండి" else "Save", fontSize = 12.sp)
                        }

                        if (onRecordInMoneyBook != null) {
                            OutlinedButton(
                                onClick = {
                                    onRecordInMoneyBook(
                                        "INCOME",
                                        interestEarned,
                                        "Interest Income ($interestType Interest on ${IndianFormatter.formatCurrency(principal)})"
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("record_moneybook_button")
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (appLanguage == "te") "ఖాతాలో రాయండి" else "Record in Book",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mandatory Financial Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (appLanguage == "te")
                            "గమనిక: ఇవి వినియోగదారు నమోదు చేసిన వివరాల ఆధారంగా వచ్చిన అంచనాలు మాత్రమే. అసలు బ్యాంక్ నిబంధనలు, చార్జీలు మరియు పన్నులు మారవచ్చు."
                        else
                            "These are mathematical estimates based on the values entered by the user. Actual loan terms, bank charges, taxes, fees and investment returns may differ.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
