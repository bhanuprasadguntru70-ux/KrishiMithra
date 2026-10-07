package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.CalculatorUtils
import com.example.ui.theme.KrishiAccentOrange
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen
import com.example.ui.theme.KrishiTextDark
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

import com.example.ui.components.CropFinancialBarChart
import com.example.ui.components.CropFinancialData

@Composable
fun MoneyBookTabScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Transactions, 1: Crop Accounts, 2: Field Records, 3: Export/Reports
    var filterType by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE
    var searchQuery by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }

    val lang = uiState.selectedLanguage
    val entries = uiState.moneyBookEntries

    val totalIncome = entries.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = entries.filter { !it.isIncome }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    val filteredEntries = entries.filter { entry ->
        (filterType == "ALL" || (filterType == "INCOME" && entry.isIncome) || (filterType == "EXPENSE" && !entry.isIncome)) &&
                (searchQuery.isBlank() || entry.title.contains(searchQuery, ignoreCase = true) || entry.category.contains(searchQuery, ignoreCase = true) || entry.cropName.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FBF8))
            .padding(14.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (lang == "తెలుగు") "📓 మై బుక్ (రైతు ఖాతా పుస్తకం)" else "📓 My Book (Farmer Money Book)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = KrishiHeaderGreen
                )
                Text(
                    text = if (lang == "తెలుగు") "మీ వ్యవసాయ ఆదాయ వ్యయాలు మరియు ఖాతాల నిర్వహణ" else "Track crop expenses, sales & field profits",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { viewModel.toggleAddBookEntry(true) },
                colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (lang == "తెలుగు") "ఎంట్రీ" else "+ Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // OVERVIEW BALANCE CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = KrishiHeaderGreen),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "తెలుగు") "నికర నిల్వ / వ్యవసాయ నికర లాభం" else "Net Farm Profit / Balance",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (netBalance >= 0) "📈 Profit" else "📉 Deficit",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = CalculatorUtils.formatCurrency(netBalance),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == "తెలుగు") "మొత్తం రాబడి (Income)" else "Total Income", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                        Text(CalculatorUtils.formatCurrency(totalIncome), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF81C784))
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == "తెలుగు") "మొత్తం ఖర్చులు (Expense)" else "Total Expense", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                        Text(CalculatorUtils.formatCurrency(totalExpense), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFFF8A80))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SUB-TAB SELECTION CHIPS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                Pair(0, if (lang == "తెలుగు") "ఖాతా వివరాలు" else "Transactions"),
                Pair(1, if (lang == "తెలుగు") "పంటల వారీగా" else "Crop Wise"),
                Pair(2, if (lang == "తెలుగు") "పొలాల వారీగా" else "Field Wise"),
                Pair(3, if (lang == "తెలుగు") "రిపోర్ట్ / CSV" else "Export Report")
            ).forEach { (tabIdx, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSubTab == tabIdx) KrishiHeaderGreen else Color.Transparent)
                        .clickable { selectedSubTab = tabIdx }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSubTab == tabIdx) Color.White else KrishiHeaderGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedSubTab) {
            0 -> {
                // TRANSACTIONS TAB
                Column {
                    // Filter Chips (ALL, INCOME, EXPENSE)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = filterType == "ALL",
                                onClick = { filterType = "ALL" },
                                label = { Text("All (${entries.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = filterType == "INCOME",
                                onClick = { filterType = "INCOME" },
                                label = { Text("Income (+)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KrishiPrimaryGreen, selectedLabelColor = Color.White)
                            )
                            FilterChip(
                                selected = filterType == "EXPENSE",
                                onClick = { filterType = "EXPENSE" },
                                label = { Text("Expense (-)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC62828), selectedLabelColor = Color.White)
                            )
                        }

                        IconButton(onClick = { showExportDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Export", tint = KrishiHeaderGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (lang == "తెలుగు") "ఎంట్రీలు ఏవీ లేవు. + నొక్కి కొత్త ఎంట్రీ చేర్చండి." else "No records found. Click '+ Add' to record transactions.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredEntries) { entry ->
                                TransactionItemCard(
                                    entry = entry,
                                    lang = lang,
                                    onDelete = { viewModel.deleteMoneyBookEntry(entry.id) }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // CROP-WISE ACCOUNTS
                val cropGrouped = entries.groupBy { it.cropName }
                val cropChartData = cropGrouped.map { (cropName, cEntries) ->
                    val inc = cEntries.filter { it.isIncome }.sumOf { it.amount }
                    val exp = cEntries.filter { !it.isIncome }.sumOf { it.amount }
                    CropFinancialData(cropName, inc, exp, KrishiHeaderGreen)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = if (lang == "తెలుగు") "పంటల వారి వ్యయం మరియు రాబడి నివేదిక" else "Crop-wise Expense & Income Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KrishiHeaderGreen
                        )
                    }

                    if (cropChartData.isNotEmpty()) {
                        item {
                            CropFinancialBarChart(cropsData = cropChartData)
                        }
                    }

                    items(cropGrouped.keys.toList()) { crop ->
                        val cropEntries = cropGrouped[crop] ?: emptyList()
                        val cIncome = cropEntries.filter { it.isIncome }.sumOf { it.amount }
                        val cExpense = cropEntries.filter { !it.isIncome }.sumOf { it.amount }
                        val cNet = cIncome - cExpense

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🌾", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(crop, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = KrishiHeaderGreen)
                                            Text("${cropEntries.size} transactions recorded", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }

                                    Text(
                                        text = CalculatorUtils.formatCurrency(cNet),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (cNet >= 0) KrishiHeaderGreen else Color(0xFFC62828)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Yield Income: ${CalculatorUtils.formatCurrency(cIncome)}", fontSize = 12.sp, color = KrishiPrimaryGreen, fontWeight = FontWeight.Bold)
                                    Text("Input Cost: ${CalculatorUtils.formatCurrency(cExpense)}", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // FIELD-WISE ACCOUNTS
                val fieldGrouped = entries.groupBy { it.fieldName }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = if (lang == "తెలుగు") "పొలాల (Plot) వారి లెక్కల నివేదిక" else "Field / Plot Accounts Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KrishiHeaderGreen
                        )
                    }

                    items(fieldGrouped.keys.toList()) { field ->
                        val fEntries = fieldGrouped[field] ?: emptyList()
                        val fIncome = fEntries.filter { it.isIncome }.sumOf { it.amount }
                        val fExpense = fEntries.filter { !it.isIncome }.sumOf { it.amount }
                        val fNet = fIncome - fExpense

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🏞️", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(field, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = KrishiHeaderGreen)
                                            Text("${fEntries.size} field activities", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }

                                    Text(
                                        text = CalculatorUtils.formatCurrency(fNet),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (fNet >= 0) KrishiHeaderGreen else Color(0xFFC62828)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Income: ${CalculatorUtils.formatCurrency(fIncome)}", fontSize = 12.sp, color = KrishiPrimaryGreen)
                                    Text("Expense: ${CalculatorUtils.formatCurrency(fExpense)}", fontSize = 12.sp, color = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // EXPORT REPORT / CSV STATEMENT
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📄 Digital Khata Report Preview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KrishiHeaderGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("KRISHIMITHRA FARMER LEDGER STATEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            val currentFarmerName = if (uiState.isLoggedIn) uiState.userName else "Farmer"
                            Text("Farmer: $currentFarmerName • Location: ${uiState.selectedLocation.ifBlank { "Local" }}", fontSize = 11.sp, color = Color.Gray)
                            Text("Total Income: ${CalculatorUtils.formatCurrency(totalIncome)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KrishiPrimaryGreen)
                            Text("Total Expenses: ${CalculatorUtils.formatCurrency(totalExpense)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            Text("Net Profit/Balance: ${CalculatorUtils.formatCurrency(netBalance)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = KrishiHeaderGreen)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { showExportDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export CSV", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showExportDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = KrishiPrimaryGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Report", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Privacy Assurance Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (lang == "తెలుగు") "గోప్యతా హామీ (Privacy Protection)" else "Strict Privacy Guarantee",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF5D4037)
                                )
                                Text(
                                    text = if (lang == "తెలుగు") "మీ ఆర్థిక మరియు లావాదేవీల రికార్డులు మీ ఫోన్‌లో మాత్రమే సురక్షితంగా ఉంటాయి. ఎవరితోనూ భాగస్వామ్యం చేయబడవు." else "Each farmer's financial records are kept strictly private on this device.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF5D4037).copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Statement CSV", fontWeight = FontWeight.Bold, color = KrishiHeaderGreen) },
            text = {
                Column {
                    val currentFarmerName = if (uiState.isLoggedIn) uiState.userName else "Farmer"
                    Text("Generated CSV Statement for $currentFarmerName:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F1)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Date,Title,Type,Category,Crop,Amount\n" +
                                    entries.joinToString("\n") { "${it.date},${it.title},${if (it.isIncome) "Income" else "Expense"},${it.category},${it.cropName},₹${it.amount}" },
                            modifier = Modifier.padding(10.dp),
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = KrishiHeaderGreen)
                ) {
                    Text("Downloaded Statement")
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    entry: com.example.data.model.MoneyBookEntry,
    lang: String,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (entry.isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (entry.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (entry.isIncome) KrishiHeaderGreen else Color(0xFFC62828),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = entry.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = KrishiHeaderGreen
                    )
                    Text(
                        text = "${entry.category} • ${entry.cropName} • ${entry.date}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (entry.isIncome) "+" else "-"}₹${entry.amount.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (entry.isIncome) KrishiPrimaryGreen else Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.LightGray)
                }
            }
        }
    }
}
