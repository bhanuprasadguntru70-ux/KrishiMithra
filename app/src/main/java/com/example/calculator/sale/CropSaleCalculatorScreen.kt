package com.example.calculator.sale

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.*
import com.example.calculator.util.IndianFormatter

@Composable
fun CropSaleCalculatorScreen(
    appLanguage: String,
    initialCropName: String = "",
    initialMandiPrice: String = "",
    onLanguageSelected: (String) -> Unit,
    onSaveCalculation: (title: String, type: String, result: String, details: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var cropName by remember { mutableStateOf(initialCropName.ifEmpty { "Maize / జొన్న/పత్తి" }) }
    var quantityInput by remember { mutableStateOf("25") }
    var quantityUnit by remember { mutableStateOf("quintal") } // "quintal", "bag", "kg"
    var mandiPriceInput by remember { mutableStateOf(initialMandiPrice.ifEmpty { "2400" }) }
    var transportCostInput by remember { mutableStateOf("1500") }
    var loadingCostInput by remember { mutableStateOf("600") }
    var commissionCostInput by remember { mutableStateOf("0") }

    var grossAmount by remember { mutableStateOf<Double?>(null) }
    var totalExpenses by remember { mutableStateOf(0.0) }
    var netAmountReceived by remember { mutableStateOf<Double?>(null) }

    fun calculate() {
        val qty = quantityInput.toDoubleOrNull() ?: 0.0
        val price = mandiPriceInput.toDoubleOrNull() ?: 0.0
        val transport = transportCostInput.toDoubleOrNull() ?: 0.0
        val loading = loadingCostInput.toDoubleOrNull() ?: 0.0
        val comm = commissionCostInput.toDoubleOrNull() ?: 0.0

        if (qty <= 0 || price <= 0) return

        val gross = qty * price
        val expenses = transport + loading + comm
        val net = gross - expenses

        grossAmount = gross
        totalExpenses = expenses
        netAmountReceived = net
    }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Crop Sale & Net Return Calculator",
                titleTe = "పంట విక్రయం & నికర రాబడి లెక్కలు",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Column {
                Text(
                    text = if (appLanguage == "te") "పంట పేరు" else "Crop Name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            CalculatorNumberInput(
                value = quantityInput,
                onValueChange = { quantityInput = it },
                labelEn = "Sale Quantity",
                labelTe = "అమ్మకపు పరిమాణం",
                appLanguage = appLanguage
            )

            Text(
                text = if (appLanguage == "te") "పరిమాణం కొలత రకం" else "Quantity Unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            CalculatorUnitChipSelector(
                selectedUnit = quantityUnit,
                options = listOf("quintal", "bag", "kg"),
                onUnitSelected = { quantityUnit = it }
            )

            CalculatorNumberInput(
                value = mandiPriceInput,
                onValueChange = { mandiPriceInput = it },
                labelEn = "Mandi Price per Unit (₹)",
                labelTe = "మార్కెట్ / మండి ధర (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = transportCostInput,
                onValueChange = { transportCostInput = it },
                labelEn = "Transport Cost (₹)",
                labelTe = "రవాణా / లారీ ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = loadingCostInput,
                onValueChange = { loadingCostInput = it },
                labelEn = "Loading / Unloading / Hamali (₹)",
                labelTe = "హమాలీ / లోడింగ్ ఖర్చు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculatorNumberInput(
                value = commissionCostInput,
                onValueChange = { commissionCostInput = it },
                labelEn = "Commission / Other Deduction (₹)",
                labelTe = "కమీషన్ / ఇతర మినహాయింపులు (₹)",
                appLanguage = appLanguage,
                prefixSymbol = "₹"
            )

            CalculateButtonRow(
                onReset = {
                    quantityInput = ""
                    mandiPriceInput = ""
                    transportCostInput = "0"
                    loadingCostInput = "0"
                    commissionCostInput = "0"
                    grossAmount = null
                    netAmountReceived = null
                },
                onCalculate = { calculate() },
                onSave = {
                    calculate()
                    if (netAmountReceived != null) {
                        onSaveCalculation(
                            "Crop Sale ($cropName - $quantityInput $quantityUnit)",
                            "SALES & PROFIT",
                            IndianFormatter.formatCurrency(netAmountReceived!!),
                            "Gross: ${IndianFormatter.formatCurrency(grossAmount!!)}, Transport & Expenses: ${IndianFormatter.formatCurrency(totalExpenses)}"
                        )
                        Toast.makeText(context, if (appLanguage == "te") "దాచబడింది!" else "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                appLanguage = appLanguage
            )

            netAmountReceived?.let { net ->
                ResultCard(
                    mainResultTitleEn = "ESTIMATED NET AMOUNT RECEIVED",
                    mainResultTitleTe = "రైతు చేతికి వచ్చే నికర సొమ్ము",
                    mainResultValue = IndianFormatter.formatCurrency(net),
                    appLanguage = appLanguage,
                    subtitleEn = "Gross Value: ${IndianFormatter.formatCurrency(grossAmount ?: 0.0)} − Selling Expenses: ${IndianFormatter.formatCurrency(totalExpenses)}",
                    subtitleTe = "మొత్తం విక్రయ విలువ: ${IndianFormatter.formatCurrency(grossAmount ?: 0.0)} − రవాణా ఖర్చులు: ${IndianFormatter.formatCurrency(totalExpenses)}",
                    breakdownItems = listOf(
                        Pair(if (appLanguage == "te") "మొత్తం అమ్మకపు ధర (గ్రాస్)" else "Gross Sale Amount", IndianFormatter.formatCurrency(grossAmount ?: 0.0)),
                        Pair(if (appLanguage == "te") "రవాణా ఖర్చు" else "Transport Cost", IndianFormatter.formatCurrency(transportCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "హమాలీ & లోడింగ్" else "Loading / Hamali", IndianFormatter.formatCurrency(loadingCostInput.toDoubleOrNull() ?: 0.0)),
                        Pair(if (appLanguage == "te") "కమీషన్ / ఇతర ఖర్చులు" else "Commission / Deductions", IndianFormatter.formatCurrency(commissionCostInput.toDoubleOrNull() ?: 0.0))
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
