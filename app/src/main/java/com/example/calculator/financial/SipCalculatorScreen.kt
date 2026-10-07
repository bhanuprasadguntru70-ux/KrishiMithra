package com.example.calculator.financial

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
fun SipCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var isStepUpSip by remember { mutableStateOf(false) }

    // Inputs for Regular SIP
    var monthlySipInput by remember { mutableStateOf("5000") }
    var expectedReturnInput by remember { mutableStateOf("12") }
    var investmentPeriodInput by remember { mutableStateOf("5") }

    // Inputs for Step-Up SIP
    var stepUpPercentInput by remember { mutableStateOf("10") }

    val monthlySip = monthlySipInput.toDoubleOrNull() ?: 0.0
    val expectedReturn = expectedReturnInput.toDoubleOrNull() ?: 0.0
    val years = investmentPeriodInput.toDoubleOrNull() ?: 0.0
    val stepUpPercent = stepUpPercentInput.toDoubleOrNull() ?: 0.0

    // Calculations
    val totalInvested: Double
    val estimatedFutureValue: Double
    val estimatedReturns: Double

    if (!isStepUpSip) {
        // Regular SIP Formula: M = P × ({[1 + i]^n - 1} / i) × (1 + i)
        val i = (expectedReturn / 100.0) / 12.0
        val n = years * 12.0

        if (monthlySip > 0 && i > 0 && n > 0) {
            estimatedFutureValue = monthlySip * (((1.0 + i).pow(n) - 1.0) / i) * (1.0 + i)
            totalInvested = monthlySip * n
            estimatedReturns = estimatedFutureValue - totalInvested
        } else {
            totalInvested = monthlySip * (years * 12.0)
            estimatedFutureValue = totalInvested
            estimatedReturns = 0.0
        }
    } else {
        // Step-Up SIP calculation month by month
        val r = (expectedReturn / 100.0) / 12.0
        val totalMonths = (years * 12).toInt()
        var currentMonthlySip = monthlySip
        var runningFutureValue = 0.0
        var runningTotalInvested = 0.0

        for (m in 1..totalMonths) {
            // Check if a new year starts (month 13, 25, 37, etc.)
            if (m > 1 && (m - 1) % 12 == 0) {
                currentMonthlySip += currentMonthlySip * (stepUpPercent / 100.0)
            }

            runningTotalInvested += currentMonthlySip
            // Compound existing corpus for 1 month + add current SIP
            runningFutureValue = (runningFutureValue + currentMonthlySip) * (1.0 + r)
        }

        totalInvested = runningTotalInvested
        estimatedFutureValue = runningFutureValue
        estimatedReturns = (estimatedFutureValue - totalInvested).coerceAtLeast(0.0)
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "SIP Calculator",
                titleTe = "SIP లెక్కలు (క్రమానుగత పెట్టుబడి)",
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
            // Mode Selector: Regular SIP vs Step-Up SIP
            TabRow(
                selectedTabIndex = if (!isStepUpSip) 0 else 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("sip_type_tabs")
            ) {
                Tab(
                    selected = !isStepUpSip,
                    onClick = { isStepUpSip = false },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "సాధారణ SIP" else "Regular SIP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = isStepUpSip,
                    onClick = { isStepUpSip = true },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "స్టెప్-అప్ SIP (Step-up)" else "Step-Up SIP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Monthly Investment
                    CalculatorNumberInput(
                        value = monthlySipInput,
                        onValueChange = { monthlySipInput = it },
                        labelEn = if (isStepUpSip) "Starting Monthly SIP (₹)" else "Monthly Investment (₹)",
                        labelTe = if (isStepUpSip) "ప్రారంభ నెలవారీ SIP (₹)" else "నెలవారీ పెట్టుబడి (₹)",
                        appLanguage = appLanguage,
                        prefixSymbol = "₹",
                        placeholder = "5,000",
                        modifier = Modifier.testTag("monthly_sip_input")
                    )

                    AnimatedVisibility(visible = isStepUpSip) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            CalculatorNumberInput(
                                value = stepUpPercentInput,
                                onValueChange = { stepUpPercentInput = it },
                                labelEn = "Annual Step-Up Percentage (%)",
                                labelTe = "ఏటా పెంచే శాతం (Annual Step-up %)",
                                appLanguage = appLanguage,
                                suffixText = "%",
                                placeholder = "10",
                                modifier = Modifier.testTag("step_up_percent_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expected Annual Return
                    CalculatorNumberInput(
                        value = expectedReturnInput,
                        onValueChange = { expectedReturnInput = it },
                        labelEn = "Expected Annual Return (%)",
                        labelTe = "ఆశించిన వార్షిక రాబడి (%)",
                        appLanguage = appLanguage,
                        suffixText = "%",
                        placeholder = "12",
                        modifier = Modifier.testTag("sip_return_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Investment Period (Years)
                    CalculatorNumberInput(
                        value = investmentPeriodInput,
                        onValueChange = { investmentPeriodInput = it },
                        labelEn = "Investment Period (Years)",
                        labelTe = "పెట్టుబడి కాలవ్యవధి (సంవత్సరాలు)",
                        appLanguage = appLanguage,
                        suffixText = if (appLanguage == "te") "సంవత్సరాలు" else "Years",
                        placeholder = "5",
                        modifier = Modifier.testTag("sip_period_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Output Results Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (appLanguage == "te") "అంచనా వేసిన భవిష్యత్తు నిధి (Estimated Growth)" else "Estimated Value",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estimated Future Value Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (appLanguage == "te") "అంచనా భవిష్యత్తు విలువ (Estimated Future Value)" else "Estimated Future Value",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = IndianFormatter.formatCurrency(estimatedFutureValue),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Monthly Investment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (appLanguage == "te") "నెలవారీ పెట్టుబడి:" else "Monthly Investment:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(monthlySip),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Total Investment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (appLanguage == "te") "మొత్తం మీరు పెట్టిన పెట్టుబడి:" else "Total Investment:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(totalInvested),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Estimated Returns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (appLanguage == "te") "అంచనా లాభ రాబడి (Estimated Returns):" else "Estimated Returns:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(estimatedReturns),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val sipTitle = if (isStepUpSip) "Step-Up SIP" else "Regular SIP"
                            onSaveCalculation(
                                "$sipTitle (${IndianFormatter.formatCurrency(monthlySip)}/mo)",
                                "SIP Calculator",
                                "Estimated Value: ${IndianFormatter.formatCurrency(estimatedFutureValue)}",
                                "Invested: ${IndianFormatter.formatCurrency(totalInvested)}, Returns: ${IndianFormatter.formatCurrency(estimatedReturns)}, Period: $years Yrs @ $expectedReturn%"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_sip_calc"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (appLanguage == "te") "లెక్కలను సేవ్ చేసుకోండి" else "Save Calculation", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mandatory Non-Guaranteed Return Disclaimers
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (appLanguage == "te") "రాబడులు హామీ ఇవ్వబడలేదు (Returns are not guaranteed)" else "Returns are not guaranteed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (appLanguage == "te")
                            "యూజర్ నమోదు చేసిన వడ్డీ శాతం ఆధారంగా అంచనా వేసిన విలువ మాత్రమే. మార్కెట్ హెచ్చుతగ్గుల వల్ల అసలు రాబడులు మారవచ్చు."
                        else
                            "Estimated value based on the return rate entered by the user. Actual returns may vary.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
