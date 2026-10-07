package com.example.calculator.financial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*
import com.example.ui.theme.KrishiAccentOrange
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen
import kotlin.math.pow

// DISCLAIMER CARD
@Composable
fun FinancialDisclaimerCard(selectedLang: String, isSip: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("⚠️", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSip) {
                    if (selectedLang == "తెలుగు") {
                        "గమనిక: రిటర్న్స్ హామీ ఇవ్వబడవు. ఇవి మీరు నమోదు చేసిన అంచనా విలువల ఆధారంగా చేసిన లెక్కలు మాత్రమే. మార్కెట్ పరిస్థితులపై రిటర్న్లు ఆధారపడి ఉంటాయి."
                    } else {
                        "Disclaimer: Returns are not guaranteed. These are mathematical estimates based on the values entered by the user."
                    }
                } else {
                    if (selectedLang == "తెలుగు") {
                        "గమనిక: ఇవి వినియోగదారు నమోదు చేసిన విలువల ఆధారంగా వేసిన గణిత శాస్త్ర అంచనాలు మాత్రమే. అసలు బ్యాంక్ నిబంధనలు, వడ్డీ రేట్లు, పన్నులు మరియు ప్రాసెసింగ్ ఫీజులు వేరుగా ఉండవచ్చు."
                    } else {
                        "Disclaimer: These are mathematical estimates based on the values entered by the user. Actual loan terms, bank charges, taxes, fees and investment returns may differ."
                    }
                },
                fontSize = 11.sp,
                color = Color(0xFF5D4037),
                lineHeight = 14.sp
            )
        }
    }
}

// 1. INTEREST CALCULATOR
@Composable
fun InterestCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var isCompound by remember { mutableStateOf(false) } // false = Simple, true = Compound
    var principalInput by remember { mutableStateOf("50000") }
    var rateInput by remember { mutableStateOf("10") }
    var timeInput by remember { mutableStateOf("2") }
    var isTimeInYears by remember { mutableStateOf(true) } // true = Years, false = Months

    // Compounding Frequency for Compound Interest
    // 12 = Monthly, 4 = Quarterly, 2 = Half-yearly, 1 = Yearly
    var compoundingFreq by remember { mutableStateOf(1) } // Default Yearly

    var resultInterest by remember { mutableStateOf<Double?>(null) }
    var resultTotalAmount by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val p = principalInput.toDoubleOrNull()
        val r = rateInput.toDoubleOrNull()
        val tVal = timeInput.toDoubleOrNull()

        if (p == null || p <= 0 || r == null || r < 0 || tVal == null || tVal <= 0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన అసలు, వడ్డీ రేటు మరియు సమయం నమోదు చేయండి." else "Please enter valid principal, interest rate and time."
            resultInterest = null
            resultTotalAmount = null
            return
        }

        val timeInYears = if (isTimeInYears) tVal else (tVal / 12.0)

        if (!isCompound) {
            // Simple Interest: SI = P * R * T / 100
            val si = (p * r * timeInYears) / 100.0
            val total = p + si
            resultInterest = si
            resultTotalAmount = total
        } else {
            // Compound Interest: A = P * (1 + r/(100*n))^(n*t)
            val n = compoundingFreq.toDouble()
            val total = p * (1.0 + (r / (100.0 * n))).pow(n * timeInYears)
            val ci = total - p
            resultInterest = ci
            resultTotalAmount = total
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Interest Calculator 💰",
                titleTel = "వడ్డీ లెక్కలు (Simple & Compound)",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        // Type Switch: Simple vs Compound Interest
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEAF5ED))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { isCompound = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isCompound) KrishiHeaderGreen else Color.Transparent,
                        contentColor = if (!isCompound) Color.White else KrishiHeaderGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (langState == "తెలుగు") "సాధారణ వడ్డీ (Simple)" else "Simple Interest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isCompound = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompound) KrishiHeaderGreen else Color.Transparent,
                        contentColor = if (isCompound) Color.White else KrishiHeaderGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (langState == "తెలుగు") "చక్రవడ్డీ (Compound)" else "Compound Interest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            CurrencyInput(
                value = principalInput,
                onValueChange = { principalInput = it },
                labelEng = "Principal Amount (₹)",
                labelTel = "అసలు సొమ్ము (₹)",
                selectedLang = langState
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    labelEng = "Interest Rate (% per year)",
                    labelTel = "వడ్డీ రేటు (% ఏటా)",
                    selectedLang = langState,
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )

                QuantityInput(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    labelEng = if (isTimeInYears) "Time (Years)" else "Time (Months)",
                    labelTel = if (isTimeInYears) "సమయం (సంవత్సరాలు)" else "సమయం (నెలలు)",
                    selectedLang = langState,
                    suffix = if (isTimeInYears) "yrs" else "mos",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Time Unit Selector (Years vs Months)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (langState == "తెలుగు") "కాలపరిమితి యూనిట్:" else "Time Unit:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KrishiHeaderGreen
                )

                FilterChip(
                    selected = isTimeInYears,
                    onClick = { isTimeInYears = true },
                    label = { Text(if (langState == "తెలుగు") "సంవత్సరాలు (Years)" else "Years") }
                )

                FilterChip(
                    selected = !isTimeInYears,
                    onClick = { isTimeInYears = false },
                    label = { Text(if (langState == "తెలుగు") "నెలలు (Months)" else "Months") }
                )
            }
        }

        // Compounding frequency option for Compound Interest
        if (isCompound) {
            item {
                Column {
                    Text(
                        text = if (langState == "తెలుగు") "చక్రవడ్డీ లెక్కించే కాలం (Compounding Frequency):" else "Compounding Frequency:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrishiHeaderGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple(1, "Yearly", "ఏటా"),
                            Triple(2, "Half-Yr", "అరసంవత్సరం"),
                            Triple(4, "Quarterly", "త్రైమాసికం"),
                            Triple(12, "Monthly", "నెలానెలా")
                        ).forEach { (freqVal, engLabel, telLabel) ->
                            FilterChip(
                                selected = compoundingFreq == freqVal,
                                onClick = { compoundingFreq = freqVal },
                                label = { Text(if (langState == "తెలుగు") telLabel else engLabel, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            item {
                Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    principalInput = ""
                    rateInput = ""
                    timeInput = ""
                    resultInterest = null
                    resultTotalAmount = null
                    errorMessage = null
                }
            )
        }

        if (resultTotalAmount != null && resultInterest != null) {
            item {
                val formattedPrincipal = CalculatorUtils.formatCurrency(principalInput.toDoubleOrNull() ?: 0.0)
                val formattedInterest = CalculatorUtils.formatCurrency(resultInterest!!)
                val formattedTotal = CalculatorUtils.formatCurrency(resultTotalAmount!!)

                val modeName = if (isCompound) "Compound Interest" else "Simple Interest"

                CalculationResultCard(
                    mainAmountStr = formattedTotal,
                    mainLabelEng = "Total Amount Payable / Receivable",
                    mainLabelTel = "మొత్తం తిరిగి చెల్లించాల్సిన / వచ్చే సొమ్ము",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("Interest Type / వడ్డీ రకం", if (langState == "తెలుగు") (if (isCompound) "చక్రవడ్డీ" else "సాధారణ వడ్డీ") else modeName),
                        Pair("Principal Amount / అసలు", formattedPrincipal),
                        Pair("Interest Earned / వచ్చిన వడ్డీ", formattedInterest),
                        Pair("Total Amount / మొత్తం", formattedTotal)
                    ),
                    onSave = {
                        onSaveResult(
                            "Interest Calculator ($modeName)",
                            formattedTotal,
                            mapOf("Principal" to formattedPrincipal, "Interest" to formattedInterest, "Total" to formattedTotal)
                        )
                    }
                )
            }
        }

        item {
            FinancialDisclaimerCard(selectedLang = langState, isSip = false)
        }
    }
}

// 2. EMI CALCULATOR
@Composable
fun EmiCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var loanAmountInput by remember { mutableStateOf("100000") }
    var interestRateInput by remember { mutableStateOf("12") }
    var tenureInput by remember { mutableStateOf("24") }
    var isTenureInMonths by remember { mutableStateOf(true) } // true = Months, false = Years

    var resultEmi by remember { mutableStateOf<Double?>(null) }
    var resultTotalInterest by remember { mutableStateOf<Double?>(null) }
    var resultTotalPayment by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val p = loanAmountInput.toDoubleOrNull()
        val rYear = interestRateInput.toDoubleOrNull()
        val tVal = tenureInput.toDoubleOrNull()

        if (p == null || p <= 0 || rYear == null || rYear <= 0 || tVal == null || tVal <= 0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన లోన్ మొత్తం, వడ్డీ రేటు మరియు కాలపరిమితి ఎంటర్ చేయండి." else "Please enter valid loan amount, interest rate and tenure."
            resultEmi = null
            resultTotalInterest = null
            resultTotalPayment = null
            return
        }

        val totalMonths = if (isTenureInMonths) tVal else (tVal * 12.0)
        val monthlyRate = rYear / (12.0 * 100.0)

        // EMI = [P * r * (1 + r)^n] / [(1 + r)^n - 1]
        val emi = (p * monthlyRate * (1.0 + monthlyRate).pow(totalMonths)) / ((1.0 + monthlyRate).pow(totalMonths) - 1.0)
        val totalPayment = emi * totalMonths
        val totalInterest = totalPayment - p

        resultEmi = emi
        resultTotalInterest = totalInterest
        resultTotalPayment = totalPayment
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "Loan EMI Calculator 🏦",
                titleTel = "బ్యాంకు / ట్రాక్టర్ లోన్ EMI లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        item {
            CurrencyInput(
                value = loanAmountInput,
                onValueChange = { loanAmountInput = it },
                labelEng = "Loan Amount (₹)",
                labelTel = "తీసుకున్న అప్పు/లోన్ మొత్తం (₹)",
                selectedLang = langState
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = interestRateInput,
                    onValueChange = { interestRateInput = it },
                    labelEng = "Interest Rate (% p.a.)",
                    labelTel = "సంవత్సరపు వడ్డీ రేటు (%)",
                    selectedLang = langState,
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )

                QuantityInput(
                    value = tenureInput,
                    onValueChange = { tenureInput = it },
                    labelEng = if (isTenureInMonths) "Tenure (Months)" else "Tenure (Years)",
                    labelTel = if (isTenureInMonths) "కాలపరిమితి (నెలలు)" else "కాలపరిమితి (సంవత్సరాలు)",
                    selectedLang = langState,
                    suffix = if (isTenureInMonths) "mos" else "yrs",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tenure switch
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (langState == "తెలుగు") "కాలపరిమితి టైప్:" else "Tenure Unit:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KrishiHeaderGreen
                )

                FilterChip(
                    selected = isTenureInMonths,
                    onClick = { isTenureInMonths = true },
                    label = { Text(if (langState == "తెలుగు") "నెలలు (Months)" else "Months") }
                )

                FilterChip(
                    selected = !isTenureInMonths,
                    onClick = { isTenureInMonths = false },
                    label = { Text(if (langState == "తెలుగు") "సంవత్సరాలు (Years)" else "Years") }
                )
            }
        }

        if (errorMessage != null) {
            item {
                Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    loanAmountInput = ""
                    interestRateInput = ""
                    tenureInput = ""
                    resultEmi = null
                    resultTotalInterest = null
                    resultTotalPayment = null
                    errorMessage = null
                }
            )
        }

        if (resultEmi != null && resultTotalPayment != null) {
            item {
                val formattedEmi = CalculatorUtils.formatCurrency(resultEmi!!)
                val formattedPrincipal = CalculatorUtils.formatCurrency(loanAmountInput.toDoubleOrNull() ?: 0.0)
                val formattedInterest = CalculatorUtils.formatCurrency(resultTotalInterest ?: 0.0)
                val formattedTotal = CalculatorUtils.formatCurrency(resultTotalPayment!!)

                val pVal = loanAmountInput.toDoubleOrNull() ?: 0.0
                val iVal = resultTotalInterest ?: 0.0
                val totalVal = resultTotalPayment ?: 1.0
                val principalPct = ((pVal / totalVal) * 100).coerceIn(0.0, 100.0)
                val interestPct = (100.0 - principalPct).coerceIn(0.0, 100.0)

                Column {
                    CalculationResultCard(
                        mainAmountStr = "$formattedEmi / month",
                        mainLabelEng = "Monthly Installment (EMI)",
                        mainLabelTel = "నెలకు చెల్లించాల్సిన EMI వాయిదా",
                        selectedLang = langState,
                        breakdown = listOf(
                            Pair("Principal Loan Amount / లోన్ అసలు", formattedPrincipal),
                            Pair("Total Interest Payable / మొత్తం వడ్డీ", formattedInterest),
                            Pair("Total Amount Payable / మొత్తం చెల్లింపు", formattedTotal)
                        ),
                        onSave = {
                            onSaveResult(
                                "EMI Calculator",
                                formattedEmi,
                                mapOf("Loan" to formattedPrincipal, "Monthly EMI" to formattedEmi, "Total Payable" to formattedTotal)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Visual Progress Bar (Principal vs Interest)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (langState == "తెలుగు") "చెల్లింపుల వర్గీకరణ (Principal vs Interest)" else "Payment Breakdown",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KrishiHeaderGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom horizontal bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(principalPct.toFloat().coerceAtLeast(0.01f))
                                        .background(KrishiHeaderGreen)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(interestPct.toFloat().coerceAtLeast(0.01f))
                                        .background(KrishiAccentOrange)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(KrishiHeaderGreen))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Principal (${principalPct.toInt()}%)", fontSize = 11.sp, color = Color.DarkGray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(KrishiAccentOrange))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Interest (${interestPct.toInt()}%)", fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            FinancialDisclaimerCard(selectedLang = langState, isSip = false)
        }
    }
}

// 3. SIP CALCULATOR
@Composable
fun SipCalculatorScreen(
    selectedLang: String,
    onSaveResult: (String, String, Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var isStepUp by remember { mutableStateOf(false) } // false = Regular SIP, true = Step-up SIP
    var monthlyInvestmentInput by remember { mutableStateOf("2000") }
    var expectedReturnInput by remember { mutableStateOf("12") }
    var yearsInput by remember { mutableStateOf("5") }
    var stepUpPctInput by remember { mutableStateOf("10") } // Default 10% step-up per year

    var resultInvested by remember { mutableStateOf<Double?>(null) }
    var resultReturns by remember { mutableStateOf<Double?>(null) }
    var resultFinalValue by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var langState by remember { mutableStateOf(selectedLang) }

    fun calculate() {
        errorMessage = null
        val p = monthlyInvestmentInput.toDoubleOrNull()
        val rateAnnual = expectedReturnInput.toDoubleOrNull()
        val years = yearsInput.toDoubleOrNull()

        if (p == null || p <= 0 || rateAnnual == null || rateAnnual < 0 || years == null || years <= 0) {
            errorMessage = if (langState == "తెలుగు") "దయచేసి సరైన నెలసరి పెట్టుబడి, వడ్డీ/రిటర్న్ రేటు మరియు సంవత్సరాలు ఎంటర్ చేయండి." else "Please enter valid monthly investment, return rate and years."
            resultInvested = null
            resultReturns = null
            resultFinalValue = null
            return
        }

        val i = rateAnnual / (12.0 * 100.0) // Monthly rate
        val nYears = years.toInt()

        if (!isStepUp) {
            // Regular SIP Formula: M = P * [((1 + i)^n - 1) / i] * (1 + i)
            val totalMonths = nYears * 12.0
            val finalVal = p * (((1.0 + i).pow(totalMonths) - 1.0) / i) * (1.0 + i)
            val totalInvested = p * totalMonths
            val estReturns = finalVal - totalInvested

            resultInvested = totalInvested
            resultReturns = estReturns
            resultFinalValue = finalVal
        } else {
            // Step-up SIP: Month-by-month compounding loop
            val stepUpPct = (stepUpPctInput.toDoubleOrNull() ?: 0.0) / 100.0
            var currentMonthlyP = p
            var accumulatedBalance = 0.0
            var totalInvestedSum = 0.0

            for (yr in 1..nYears) {
                for (m in 1..12) {
                    accumulatedBalance = (accumulatedBalance + currentMonthlyP) * (1.0 + i)
                    totalInvestedSum += currentMonthlyP
                }
                currentMonthlyP *= (1.0 + stepUpPct)
            }

            resultInvested = totalInvestedSum
            resultReturns = accumulatedBalance - totalInvestedSum
            resultFinalValue = accumulatedBalance
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CalculatorHeader(
                titleEng = "SIP Investment Calculator 📈",
                titleTel = "నెలవారీ పొదుపు / SIP లెక్కలు",
                selectedLang = langState,
                onLangToggle = { langState = it },
                onClose = onClose
            )
        }

        // Mode switch: Regular vs Step-Up SIP
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEAF5ED))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { isStepUp = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isStepUp) KrishiHeaderGreen else Color.Transparent,
                        contentColor = if (!isStepUp) Color.White else KrishiHeaderGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (langState == "తెలుగు") "సాధారణ SIP" else "Regular SIP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isStepUp = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStepUp) KrishiHeaderGreen else Color.Transparent,
                        contentColor = if (isStepUp) Color.White else KrishiHeaderGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (langState == "తెలుగు") "స్టెప్-అప్ SIP (Step-Up)" else "Step-Up SIP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            CurrencyInput(
                value = monthlyInvestmentInput,
                onValueChange = { monthlyInvestmentInput = it },
                labelEng = "Monthly Investment (₹)",
                labelTel = "నెలవారీ పొదుపు మొత్తం (₹)",
                selectedLang = langState
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityInput(
                    value = expectedReturnInput,
                    onValueChange = { expectedReturnInput = it },
                    labelEng = "Expected Return (% p.a.)",
                    labelTel = "అంచనా వార్షిక రిటర్న్ (%)",
                    selectedLang = langState,
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )

                QuantityInput(
                    value = yearsInput,
                    onValueChange = { yearsInput = it },
                    labelEng = "Period (Years)",
                    labelTel = "వ్యవధి (సంవత్సరాలు)",
                    selectedLang = langState,
                    suffix = "yrs",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (isStepUp) {
            item {
                QuantityInput(
                    value = stepUpPctInput,
                    onValueChange = { stepUpPctInput = it },
                    labelEng = "Annual Step-Up (% increment each year)",
                    labelTel = "ఏటా పొదుపు పెంపు శాతం (% ఏటా)",
                    selectedLang = langState,
                    suffix = "%"
                )
            }
        }

        if (errorMessage != null) {
            item {
                Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            CalculateActionButtons(
                selectedLang = langState,
                onCalculate = { calculate() },
                onReset = {
                    monthlyInvestmentInput = ""
                    expectedReturnInput = ""
                    yearsInput = ""
                    stepUpPctInput = "10"
                    resultInvested = null
                    resultReturns = null
                    resultFinalValue = null
                    errorMessage = null
                }
            )
        }

        if (resultFinalValue != null && resultInvested != null) {
            item {
                val formattedInvested = CalculatorUtils.formatCurrency(resultInvested!!)
                val formattedReturns = CalculatorUtils.formatCurrency(resultReturns ?: 0.0)
                val formattedFinal = CalculatorUtils.formatCurrency(resultFinalValue!!)

                val modeStr = if (isStepUp) "Step-Up SIP" else "Regular SIP"

                CalculationResultCard(
                    mainAmountStr = formattedFinal,
                    mainLabelEng = "Estimated Total Maturity Value",
                    mainLabelTel = "అంచనా మొత్తం మెచ్యూరిటీ విలువ",
                    selectedLang = langState,
                    breakdown = listOf(
                        Pair("SIP Mode / పొదుపు రకం", modeStr),
                        Pair("Total Amount Invested / మీరు దాచిన మొత్తం", formattedInvested),
                        Pair("Estimated Returns / అంచనా లాభం/రిటర్న్లు", formattedReturns),
                        Pair("Final Corpus / మెచ్యూరిటీ మొత్తం", formattedFinal)
                    ),
                    onSave = {
                        onSaveResult(
                            "SIP Calculator ($modeStr)",
                            formattedFinal,
                            mapOf("Invested" to formattedInvested, "Returns" to formattedReturns, "Maturity Value" to formattedFinal)
                        )
                    }
                )
            }
        }

        item {
            FinancialDisclaimerCard(selectedLang = langState, isSip = true)
        }
    }
}
