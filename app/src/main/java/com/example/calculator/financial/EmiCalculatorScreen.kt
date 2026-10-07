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
fun EmiCalculatorScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (String, String, String, String) -> Unit,
    onRecordInMoneyBook: ((type: String, amount: Double, desc: String) -> Unit)? = null,
    onBack: () -> Unit
) {
    var loanAmountInput by remember { mutableStateOf("200000") }
    var interestRateInput by remember { mutableStateOf("10") }
    var tenureInput by remember { mutableStateOf("3") }
    var tenureUnit by remember { mutableStateOf("YEARS") } // "MONTHS" or "YEARS"
    var showRepaymentSummary by remember { mutableStateOf(false) }

    val loanAmount = loanAmountInput.toDoubleOrNull() ?: 0.0
    val annualRate = interestRateInput.toDoubleOrNull() ?: 0.0
    val tenureVal = tenureInput.toDoubleOrNull() ?: 0.0

    // Total tenure in months
    val totalMonths = if (tenureUnit == "YEARS") (tenureVal * 12).toInt() else tenureVal.toInt()

    // Monthly interest rate
    val r = annualRate / (12.0 * 100.0)

    // Calculation
    val monthlyEmi: Double
    val totalAmountPayable: Double
    val totalInterest: Double

    if (loanAmount > 0 && r > 0 && totalMonths > 0) {
        val emiFactor = (1.0 + r).pow(totalMonths.toDouble())
        monthlyEmi = loanAmount * r * (emiFactor / (emiFactor - 1.0))
        totalAmountPayable = monthlyEmi * totalMonths
        totalInterest = totalAmountPayable - loanAmount
    } else {
        monthlyEmi = 0.0
        totalAmountPayable = loanAmount
        totalInterest = 0.0
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "EMI Calculator",
                titleTe = "EMI లెక్కలు (రుణ వాయిదాలు)",
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
            // Inputs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Loan Amount
                    CalculatorNumberInput(
                        value = loanAmountInput,
                        onValueChange = { loanAmountInput = it },
                        labelEn = "Loan Amount (₹)",
                        labelTe = "రుణం మొత్తం (₹)",
                        appLanguage = appLanguage,
                        prefixSymbol = "₹",
                        placeholder = "2,00,000",
                        modifier = Modifier.testTag("loan_amount_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interest Rate
                    CalculatorNumberInput(
                        value = interestRateInput,
                        onValueChange = { interestRateInput = it },
                        labelEn = "Interest Rate (% per year)",
                        labelTe = "వడ్డీ రేటు (% ఏడాదికి)",
                        appLanguage = appLanguage,
                        suffixText = "%",
                        placeholder = "10",
                        modifier = Modifier.testTag("emi_rate_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tenure
                    Text(
                        text = if (appLanguage == "te") "రుణ కాలపరిమితి (Loan Tenure)" else "Loan Tenure",
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
                            value = tenureInput,
                            onValueChange = { tenureInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tenure_input"),
                            placeholder = { Text("3") },
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        ) {
                            Surface(
                                color = if (tenureUnit == "MONTHS") MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clickable { tenureUnit = "MONTHS" }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (appLanguage == "te") "నెలులు" else "Months",
                                    fontSize = 12.sp,
                                    color = if (tenureUnit == "MONTHS") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = if (tenureUnit == "YEARS") MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clickable { tenureUnit = "YEARS" }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (appLanguage == "te") "సంవత్సరాలు" else "Years",
                                    fontSize = 12.sp,
                                    color = if (tenureUnit == "YEARS") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (appLanguage == "te") "EMI ఫలితాలు (Calculation Estimate)" else "EMI Calculations",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Monthly EMI Large Banner
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
                                text = if (appLanguage == "te") "నెలకు చెల్లించాల్సిన EMI" else "Monthly EMI Payable",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = IndianFormatter.formatCurrency(monthlyEmi),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row Breakdown: Loan Amount vs Total Interest
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (appLanguage == "te") "అసలు రుణం (Principal):" else "Principal Loan Amount:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(loanAmount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (appLanguage == "te") "మొత్తం వడ్డీ ఖర్చు:" else "Total Interest Payable:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(totalInterest),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage == "te") "మొత్తం చెల్లింపు (Total Payment):" else "Total Payment:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(totalAmountPayable),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle Repayment Schedule Summary
                    OutlinedButton(
                        onClick = { showRepaymentSummary = !showRepaymentSummary },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (showRepaymentSummary) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showRepaymentSummary)
                                (if (appLanguage == "te") "సంవత్సరాల వారీ పట్టికను మూయండి" else "Hide Repayment Summary")
                            else
                                (if (appLanguage == "te") "సంవత్సరాల వారీ EMI పట్టిక చూడండి" else "Show Repayment Summary")
                        )
                    }

                    // Optional Yearly Breakdown Table
                    AnimatedVisibility(visible = showRepaymentSummary) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = if (appLanguage == "te") "ఏడాది వారీ అంచనా పట్టిక:" else "Yearly Repayment Schedule:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val yearsCount = (totalMonths / 12).coerceAtLeast(1)
                            val yearlyEmiTotal = monthlyEmi * 12

                            var remainingBalance = loanAmount

                            for (yr in 1..yearsCount) {
                                val yearlyInterest = remainingBalance * (annualRate / 100.0)
                                val yearlyPrincipal = (yearlyEmiTotal - yearlyInterest).coerceAtLeast(0.0)
                                remainingBalance = (remainingBalance - yearlyPrincipal).coerceAtLeast(0.0)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${if (appLanguage == "te") "సంవత్సరం" else "Year"} $yr",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${if (appLanguage == "te") "వడ్డీ" else "Int"}: ${IndianFormatter.formatCurrency(yearlyInterest)}",
                                                fontSize = 11.sp,
                                                color = Color(0xFFD32F2F)
                                            )
                                            Text(
                                                text = "${if (appLanguage == "te") "మిగిలిన రుణం" else "Balance"}: ${IndianFormatter.formatCurrency(remainingBalance)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions: Save & Record
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveCalculation(
                                    "EMI (${IndianFormatter.formatCurrency(loanAmount)})",
                                    "EMI Calculator",
                                    "Monthly EMI: ${IndianFormatter.formatCurrency(monthlyEmi)}",
                                    "Loan: ${IndianFormatter.formatCurrency(loanAmount)}, Interest: ${IndianFormatter.formatCurrency(totalInterest)}, Tenure: $tenureVal $tenureUnit"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_emi_calc"),
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
                                        "EXPENSE",
                                        monthlyEmi,
                                        "Monthly EMI Payment (${IndianFormatter.formatCurrency(loanAmount)} Loan)"
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("record_emi_moneybook")
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (appLanguage == "te") "ఖర్చుగా రాయండి" else "Record Expense",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financial Disclaimer
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
                            "గమనిక: ఈ యాప్ ఎటువంటి రుణాలను మంజూరు చేయదు లేదా ఆర్థిక సలహాలు ఇవ్వదు. ఇవి లెక్కించిన రకం అంచనాలు మాత్రమే."
                        else
                            "This calculator provides mathematical estimates only. We do not provide loan approval or official financial advice. Final loan terms depend on your financial institution.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
