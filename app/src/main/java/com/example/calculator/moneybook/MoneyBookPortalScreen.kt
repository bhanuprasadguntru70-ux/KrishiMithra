package com.example.calculator.moneybook

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.components.CalculatorTopHeader
import com.example.calculator.util.IndianFormatter
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.FarmerFieldEntity
import com.example.data.model.IncomeEntryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyBookPortalScreen(
    appLanguage: String,
    initialPreFillType: String? = null, // "EXPENSE" or "INCOME"
    initialAmount: Double? = null,
    initialDesc: String? = null,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: MoneyBookViewModel = viewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val incomeList by viewModel.incomeList.collectAsStateWithLifecycle()
    val fields by viewModel.fields.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigation Tab state: 0=Dashboard, 1=Daily Entries, 2=Crop Accounts, 3=Fields, 4=Reports & Export
    var selectedTab by remember { mutableStateOf(0) }

    // Dialog States
    var showAddExpenseDialog by remember { mutableStateOf(initialPreFillType == "EXPENSE") }
    var showAddIncomeDialog by remember { mutableStateOf(initialPreFillType == "INCOME") }
    var showAddFieldDialog by remember { mutableStateOf(false) }

    // Search & Filter State
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedCropFilter by remember { mutableStateOf("ALL") }

    // Prefilled values passed from Calculators if any
    var prefilledAmount by remember { mutableStateOf(initialAmount?.toString() ?: "") }
    var prefilledDesc by remember { mutableStateOf(initialDesc ?: "") }

    val categories = listOf(
        "Seeds", "Fertilizer", "Pesticides", "Labour",
        "Tractor", "Diesel", "Electricity", "Irrigation",
        "Transport", "Machinery", "Rent", "Other"
    )

    val unitsList = listOf("Bags", "Quintal", "Tonne", "Kg", "Box")

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "My Book (Farmer Notebook)",
                titleTe = "నా ఖాతా పుస్తకం / నా లెక్కలు",
                appLanguage = appLanguage,
                onLanguageSelected = onLanguageSelected,
                onBack = onBack
            )
        },
        floatingActionButton = {
            if (selectedTab in listOf(0, 1)) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddIncomeDialog = true },
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = {
                            Text(
                                text = if (appLanguage == "te") "+ ఆదాయం జతచేయి" else "+ Income",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("add_income_fab")
                    )

                    ExtendedFloatingActionButton(
                        onClick = { showAddExpenseDialog = true },
                        containerColor = Color(0xFFC2185B),
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Remove, contentDescription = null) },
                        text = {
                            Text(
                                text = if (appLanguage == "te") "+ ఖర్చు జతచేయి" else "+ Expense",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("add_expense_fab")
                    )
                }
            } else if (selectedTab == 3) {
                FloatingActionButton(
                    onClick = { showAddFieldDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_field_fab")
                ) {
                    Icon(Icons.Default.AddLocation, contentDescription = "Add Field")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "📊 డాష్‌బోర్డ్" else "📊 Dashboard",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "📒 దినసరి లెక్కలు" else "📒 Daily Entries",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "🌾 పంటల లెక్కలు" else "🌾 Crop Accounts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "🏞️ పొలాలు / ఫీల్డ్స్" else "🏞️ Farm Fields",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = {
                        Text(
                            text = if (appLanguage == "te") "📄 రిపోర్టు & ఎగుమతి" else "📄 Export Report",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            // Screen Content
            when (selectedTab) {
                0 -> MoneyBookDashboardView(
                    expenses = expenses,
                    incomeList = incomeList,
                    fields = fields,
                    appLanguage = appLanguage,
                    onNavigateToEntries = { selectedTab = 1 },
                    onNavigateToCrops = { selectedTab = 2 }
                )
                1 -> DailyEntriesView(
                    expenses = expenses,
                    incomeList = incomeList,
                    fields = fields,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    categoryFilter = selectedCategoryFilter,
                    onCategoryFilterChange = { selectedCategoryFilter = it },
                    categories = categories,
                    appLanguage = appLanguage,
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onDeleteIncome = { viewModel.deleteIncome(it) }
                )
                2 -> CropAccountsView(
                    expenses = expenses,
                    incomeList = incomeList,
                    appLanguage = appLanguage
                )
                3 -> FarmFieldsView(
                    fields = fields,
                    expenses = expenses,
                    incomeList = incomeList,
                    appLanguage = appLanguage,
                    onAddFieldClick = { showAddFieldDialog = true },
                    onDeleteField = { viewModel.deleteField(it) }
                )
                4 -> MoneyBookExportView(
                    expenses = expenses,
                    incomeList = incomeList,
                    appLanguage = appLanguage,
                    farmerName = viewModel.farmerProfileName.value,
                    context = context
                )
            }
        }
    }

    // --- ADD EXPENSE DIALOG ---
    if (showAddExpenseDialog) {
        AddExpenseModalDialog(
            appLanguage = appLanguage,
            categories = categories,
            fields = fields,
            initialAmount = prefilledAmount,
            initialDesc = prefilledDesc,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { cat, desc, amt, crop, fieldName, dateStr, note ->
                viewModel.addExpense(cat, desc, amt, crop, fieldName, dateStr, note)
                showAddExpenseDialog = false
            }
        )
    }

    // --- ADD INCOME DIALOG ---
    if (showAddIncomeDialog) {
        AddIncomeModalDialog(
            appLanguage = appLanguage,
            unitsList = unitsList,
            fields = fields,
            initialDesc = prefilledDesc,
            initialAmount = prefilledAmount,
            onDismiss = { showAddIncomeDialog = false },
            onSave = { cropSold, qty, unit, price, totalAmt, buyer, market, fieldName, dateStr, note ->
                viewModel.addIncome(cropSold, qty, unit, price, totalAmt, buyer, market, fieldName, dateStr, note)
                showAddIncomeDialog = false
            }
        )
    }

    // --- ADD FIELD DIALOG ---
    if (showAddFieldDialog) {
        AddFieldModalDialog(
            appLanguage = appLanguage,
            onDismiss = { showAddFieldDialog = false },
            onSave = { name, area, unit, crop, season, note ->
                viewModel.addField(name, area, unit, crop, season, note)
                showAddFieldDialog = false
            }
        )
    }
}

// =========================================================
// 1. DASHBOARD VIEW
// =========================================================
@Composable
private fun MoneyBookDashboardView(
    expenses: List<ExpenseEntryEntity>,
    incomeList: List<IncomeEntryEntity>,
    fields: List<FarmerFieldEntity>,
    appLanguage: String,
    onNavigateToEntries: () -> Unit,
    onNavigateToCrops: () -> Unit
) {
    val totalIncome = remember(incomeList) { incomeList.sumOf { it.totalAmount } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val netBalance = totalIncome - totalExpenses

    val cropWiseIncome = remember(incomeList) {
        incomeList.groupBy { it.cropSold.ifEmpty { "General" } }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
    }

    val cropWiseExpense = remember(expenses) {
        expenses.groupBy { it.crop.ifEmpty { "General" } }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val allCropNames = (cropWiseIncome.keys + cropWiseExpense.keys).distinct()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Summary Header Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (appLanguage == "te") "ఈ నెల సారాంశం (This Month Balance)" else "This Month Summary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (appLanguage == "te") "మొత్తం ఆదాయం (Income)" else "Total Income",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(totalIncome),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (appLanguage == "te") "మొత్తం ఖర్చులు (Expenses)" else "Total Expenses",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = IndianFormatter.formatCurrency(totalExpenses),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFC2185B)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appLanguage == "te") "మిగిలిన నికర నిల్వ (Net Balance):" else "Net Balance:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = IndianFormatter.formatCurrency(netBalance),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netBalance >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Crop-Wise Summary Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appLanguage == "te") "పంటల వారీ లాభనష్టాలు" else "Crop-Wise Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = onNavigateToCrops) {
                        Text(text = if (appLanguage == "te") "అన్నీ చూడండి" else "View All", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (allCropNames.isEmpty()) {
                    Text(
                        text = if (appLanguage == "te") "ఇంకా పంటల వివరాలు ఏమీ నమోదు చేయలేదు." else "No crop records added yet. Click + Income or + Expense below.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    allCropNames.take(4).forEach { crop ->
                        val cInc = cropWiseIncome[crop] ?: 0.0
                        val cExp = cropWiseExpense[crop] ?: 0.0
                        val cProfit = cInc - cExp

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Grass, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = crop, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = IndianFormatter.formatCurrency(cProfit),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC2185B)
                                )
                                Text(
                                    text = "In: ${IndianFormatter.formatCurrency(cInc)} | Out: ${IndianFormatter.formatCurrency(cExp)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Category Breakdown Cards
        Text(
            text = if (appLanguage == "te") "ముఖ్యమైన ఖర్చుల వర్గాలు (Expense Breakdown)" else "Expense Category Overview",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val catMap = remember(expenses) { expenses.groupBy { it.category } }
        val topCats = listOf("Seeds", "Fertilizer", "Labour", "Tractor", "Pesticides", "Transport")

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(topCats) { cat ->
                val amt = catMap[cat]?.sumOf { it.amount } ?: 0.0
                Card(
                    modifier = Modifier.width(130.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = IndianFormatter.formatCurrency(amt),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// =========================================================
// 2. DAILY ENTRIES VIEW
// =========================================================
@Composable
private fun DailyEntriesView(
    expenses: List<ExpenseEntryEntity>,
    incomeList: List<IncomeEntryEntity>,
    fields: List<FarmerFieldEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categoryFilter: String,
    onCategoryFilterChange: (String) -> Unit,
    categories: List<String>,
    appLanguage: String,
    onDeleteExpense: (Int) -> Unit,
    onDeleteIncome: (Int) -> Unit
) {
    // Combine both into timeline
    data class EntryItem(
        val isIncome: Boolean,
        val id: Int,
        val date: String,
        val title: String,
        val subtitle: String,
        val amount: Double,
        val crop: String,
        val category: String,
        val timestamp: Long
    )

    val combinedList = remember(expenses, incomeList) {
        val list = mutableListOf<EntryItem>()
        incomeList.forEach { inc ->
            list.add(
                EntryItem(
                    isIncome = true,
                    id = inc.id,
                    date = inc.date,
                    title = "Crop Sale: ${inc.cropSold}",
                    subtitle = "${inc.quantity} ${inc.unit} @ ₹${inc.pricePerUnit} | ${inc.buyerName}",
                    amount = inc.totalAmount,
                    crop = inc.cropSold,
                    category = "Sale",
                    timestamp = inc.timestamp
                )
            )
        }
        expenses.forEach { exp ->
            list.add(
                EntryItem(
                    isIncome = false,
                    id = exp.id,
                    date = exp.date,
                    title = "${exp.category}: ${exp.description.ifEmpty { exp.category }}",
                    subtitle = "Crop: ${exp.crop.ifEmpty { "General" }} ${if (exp.fieldName.isNotEmpty()) "| Field: ${exp.fieldName}" else ""}",
                    amount = exp.amount,
                    crop = exp.crop,
                    category = exp.category,
                    timestamp = exp.timestamp
                )
            )
        }
        list.sortedByDescending { it.timestamp }
    }

    val filteredList = combinedList.filter { item ->
        val matchesQuery = searchQuery.isEmpty() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true) ||
                item.crop.contains(searchQuery, ignoreCase = true)
        val matchesCat = categoryFilter == "ALL" || item.category.equals(categoryFilter, ignoreCase = true)
        matchesQuery && matchesCat
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = if (appLanguage == "te") "లెక్కల వెతుకులాట (పంట, కేటగిరి)..." else "Search entries by crop or category...",
                    fontSize = 12.sp
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = categoryFilter == "ALL",
                    onClick = { onCategoryFilterChange("ALL") },
                    label = { Text(if (appLanguage == "te") "అన్నీ" else "All", fontSize = 11.sp) }
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = categoryFilter.equals(cat, ignoreCase = true),
                    onClick = { onCategoryFilterChange(cat) },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Entries List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (appLanguage == "te") "ఎటువంటి దినసరి వివరాలు లేవు. + బటన్ నొక్కి జతచేయండి." else "No entries found. Tap + Income or + Expense to add.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList, key = { "${it.isIncome}_${it.id}" }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                Surface(
                                    shape = CircleShape,
                                    color = if (item.isIncome) Color(0xFFE8F5E9) else Color(0xFFFCE4EC),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (item.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (item.isIncome) Color(0xFF2E7D32) else Color(0xFFC2185B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.date,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = (if (item.isIncome) "+" else "-") + IndianFormatter.formatCurrency(item.amount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.isIncome) Color(0xFF2E7D32) else Color(0xFFC2185B)
                                )

                                IconButton(
                                    onClick = {
                                        if (item.isIncome) onDeleteIncome(item.id) else onDeleteExpense(item.id)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 3. CROP ACCOUNTS VIEW
// =========================================================
@Composable
private fun CropAccountsView(
    expenses: List<ExpenseEntryEntity>,
    incomeList: List<IncomeEntryEntity>,
    appLanguage: String
) {
    val allCrops = (expenses.map { it.crop }.filter { it.isNotEmpty() } +
            incomeList.map { it.cropSold }.filter { it.isNotEmpty() }).distinct()

    var selectedCrop by remember { mutableStateOf(allCrops.firstOrNull() ?: "Maize") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (appLanguage == "te") "పంటను ఎంచుకోండి (Select Crop)" else "Select Crop Account",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Crop Selector Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val displayCrops = if (allCrops.isEmpty()) listOf("Maize", "Paddy", "Tomato", "Cotton") else allCrops
            items(displayCrops) { crop ->
                val isSel = selectedCrop.equals(crop, ignoreCase = true)
                FilterChip(
                    selected = isSel,
                    onClick = { selectedCrop = crop },
                    label = { Text("🌾 $crop", fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val cropIncome = remember(incomeList, selectedCrop) {
            incomeList.filter { it.cropSold.equals(selectedCrop, ignoreCase = true) }.sumOf { it.totalAmount }
        }

        val cropExpenses = remember(expenses, selectedCrop) {
            expenses.filter { it.crop.equals(selectedCrop, ignoreCase = true) }
        }

        val cropTotalExpense = remember(cropExpenses) { cropExpenses.sumOf { it.amount } }
        val netProfit = cropIncome - cropTotalExpense

        // Crop Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌾 $selectedCrop - ${if (appLanguage == "te") "ఖాతా వివరాలు" else "Account Statement"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (appLanguage == "te") "మొత్తం అమ్మకాలు (Income):" else "Total Crop Sales:")
                    Text(text = IndianFormatter.formatCurrency(cropIncome), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (appLanguage == "te") "మొత్తం ఖర్చు (Expenses):" else "Total Expenses:")
                    Text(text = IndianFormatter.formatCurrency(cropTotalExpense), fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appLanguage == "te") "నికర లాభం / నష్టం (Net Profit/Loss):" else "Net Profit / Loss:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = IndianFormatter.formatCurrency(netProfit),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netProfit >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Breakdown by Category
        Text(
            text = if (appLanguage == "te") "ఖర్చుల వర్గాల వారీ విశ్లేషణ" else "Expense Category Breakdown",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val catBreakdown = remember(cropExpenses) {
            cropExpenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
        }

        if (catBreakdown.isEmpty()) {
            Text(
                text = if (appLanguage == "te") "ఈ పంటకి ఎటువంటి ఖర్చులు నమోదు చేయలేదు." else "No expenses recorded for $selectedCrop yet.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            catBreakdown.forEach { (cat, amt) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = cat, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = IndianFormatter.formatCurrency(amt), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================
// 4. FARM FIELDS VIEW
// =========================================================
@Composable
private fun FarmFieldsView(
    fields: List<FarmerFieldEntity>,
    expenses: List<ExpenseEntryEntity>,
    incomeList: List<IncomeEntryEntity>,
    appLanguage: String,
    onAddFieldClick: () -> Unit,
    onDeleteField: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (appLanguage == "te") "నా పొలాలు / ఫీల్డ్‌లు" else "My Farm Fields",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = onAddFieldClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (appLanguage == "te") "+ పొలం జతచేయి" else "+ Add Field", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (fields.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (appLanguage == "te") "ఎటువంటి పొలాలు జతచేయబడలేదు. ఉదా: Field 1, Field 2 జతచేయండి." else "No farm fields added yet. Add fields to track field-level profits.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(fields, key = { it.id }) { field ->
                    val fieldIncome = incomeList.filter { it.fieldName.equals(field.fieldName, ignoreCase = true) }.sumOf { it.totalAmount }
                    val fieldExpense = expenses.filter { it.fieldName.equals(field.fieldName, ignoreCase = true) }.sumOf { it.amount }
                    val fieldProfit = fieldIncome - fieldExpense

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Landscape, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = field.fieldName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(onClick = { onDeleteField(field.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Area: ${field.area} ${field.unit} | Crop: ${field.cropName.ifEmpty { "N/A" }} | Season: ${field.season}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Income", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(text = IndianFormatter.formatCurrency(fieldIncome), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                                Column {
                                    Text(text = "Expenses", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(text = IndianFormatter.formatCurrency(fieldExpense), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Net Profit", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = IndianFormatter.formatCurrency(fieldProfit),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (fieldProfit >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 5. EXPORT & SHARE VIEW
// =========================================================
@Composable
private fun MoneyBookExportView(
    expenses: List<ExpenseEntryEntity>,
    incomeList: List<IncomeEntryEntity>,
    appLanguage: String,
    farmerName: String,
    context: android.content.Context
) {
    val totalIncome = remember(incomeList) { incomeList.sumOf { it.totalAmount } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val netProfit = totalIncome - totalExpenses

    val reportText = remember(expenses, incomeList, farmerName) {
        buildString {
            append("KRISHIMITHRA FARMER MONEY BOOK REPORT\n")
            append("=====================================\n")
            append("Farmer Name: $farmerName\n")
            append("Date: ${MoneyBookViewModel.getCurrentDateFormatted()}\n\n")
            append("SUMMARY:\n")
            append("Total Income: ${IndianFormatter.formatCurrency(totalIncome)}\n")
            append("Total Expenses: ${IndianFormatter.formatCurrency(totalExpenses)}\n")
            append("Net Balance: ${IndianFormatter.formatCurrency(netProfit)}\n\n")
            append("INCOME ENTRIES (${incomeList.size}):\n")
            incomeList.forEachIndexed { idx, inc ->
                append("${idx + 1}. ${inc.date} | ${inc.cropSold} | Qty: ${inc.quantity} ${inc.unit} | Total: ${IndianFormatter.formatCurrency(inc.totalAmount)}\n")
            }
            append("\nEXPENSE ENTRIES (${expenses.size}):\n")
            expenses.forEachIndexed { idx, exp ->
                append("${idx + 1}. ${exp.date} | ${exp.category} | ${exp.description} | Amt: ${IndianFormatter.formatCurrency(exp.amount)}\n")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (appLanguage == "te") "📄 ఖాతా రిపోర్టు ప్రివ్యూ" else "📄 Financial Report Summary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = reportText,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "KrishiMithra Farmer Report - $farmerName")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Report via"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (appLanguage == "te") "వాట్సాప్/షేర్ చేయండి" else "Share Report")
                    }
                }
            }
        }
    }
}

// =========================================================
// MODAL DIALOGS FOR ADDING EXPENSE, INCOME, AND FIELDS
// =========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseModalDialog(
    appLanguage: String,
    categories: List<String>,
    fields: List<FarmerFieldEntity>,
    initialAmount: String = "",
    initialDesc: String = "",
    onDismiss: () -> Unit,
    onSave: (category: String, description: String, amount: Double, crop: String, fieldName: String, dateStr: String, receiptNote: String) -> Unit
) {
    var category by remember { mutableStateOf(categories.first()) }
    var description by remember { mutableStateOf(initialDesc) }
    var amountInput by remember { mutableStateOf(initialAmount) }
    var crop by remember { mutableStateOf("Maize") }
    var selectedFieldName by remember { mutableStateOf(fields.firstOrNull()?.fieldName ?: "") }
    var dateStr by remember { mutableStateOf(MoneyBookViewModel.getCurrentDateFormatted()) }
    var receiptNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (appLanguage == "te") "💸 దినసరి ఖర్చు నమోదు (Add Expense)" else "Add Expense Entry",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Dropdown/Chips
                Text(text = "Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (వివరణ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_desc_input")
                )

                // Crop
                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("Crop Name (పంట పేరు)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_crop_input")
                )

                // Field Selection
                if (fields.isNotEmpty()) {
                    Text(text = "Select Field:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(fields) { f ->
                            FilterChip(
                                selected = selectedFieldName == f.fieldName,
                                onClick = { selectedFieldName = f.fieldName },
                                label = { Text(f.fieldName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Date
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Receipt Note
                OutlinedTextField(
                    value = receiptNote,
                    onValueChange = { receiptNote = it },
                    label = { Text("Photo/Receipt Note (రశీదు వివరాలు)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(category, description, amt, crop, selectedFieldName, dateStr, receiptNote)
                    }
                },
                modifier = Modifier.testTag("save_expense_btn")
            ) {
                Text(text = if (appLanguage == "te") "ఖర్చు సేవ్ చేయి" else "Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (appLanguage == "te") "రద్దు చేయి" else "Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIncomeModalDialog(
    appLanguage: String,
    unitsList: List<String>,
    fields: List<FarmerFieldEntity>,
    initialDesc: String = "",
    initialAmount: String = "",
    onDismiss: () -> Unit,
    onSave: (cropSold: String, quantity: Double, unit: String, pricePerUnit: Double, totalAmount: Double, buyerName: String, marketName: String, fieldName: String, dateStr: String, notes: String) -> Unit
) {
    var cropSold by remember { mutableStateOf(initialDesc.ifEmpty { "Tomato" }) }
    var quantityInput by remember { mutableStateOf("100") }
    var selectedUnit by remember { mutableStateOf(unitsList.first()) }
    var priceInput by remember { mutableStateOf("1000") }
    var buyerName by remember { mutableStateOf("") }
    var marketName by remember { mutableStateOf("") }
    var selectedFieldName by remember { mutableStateOf(fields.firstOrNull()?.fieldName ?: "") }
    var dateStr by remember { mutableStateOf(MoneyBookViewModel.getCurrentDateFormatted()) }
    var notes by remember { mutableStateOf("") }

    val qty = quantityInput.toDoubleOrNull() ?: 0.0
    val price = priceInput.toDoubleOrNull() ?: 0.0
    val totalAmount = qty * price

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (appLanguage == "te") "💰 పంట అమ్మకపు ఆదాయం (Add Income)" else "Record Income / Crop Sale",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Crop Sold
                OutlinedTextField(
                    value = cropSold,
                    onValueChange = { cropSold = it },
                    label = { Text("Crop Sold (పంట రకం)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("income_crop_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("income_qty_input")
                    )

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price/Unit (₹)") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("income_price_input")
                    )
                }

                // Unit selection
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(unitsList) { u ->
                        FilterChip(
                            selected = selectedUnit == u,
                            onClick = { selectedUnit = u },
                            label = { Text(u, fontSize = 11.sp) }
                        )
                    }
                }

                // Auto calculated Total Amount
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Income:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = IndianFormatter.formatCurrency(totalAmount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name (కొనుగోలుదారు)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = marketName,
                    onValueChange = { marketName = it },
                    label = { Text("Market / Mandi (మార్కెట్)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field Selection
                if (fields.isNotEmpty()) {
                    Text(text = "Select Field:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(fields) { f ->
                            FilterChip(
                                selected = selectedFieldName == f.fieldName,
                                onClick = { selectedFieldName = f.fieldName },
                                label = { Text(f.fieldName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (totalAmount > 0) {
                        onSave(cropSold, qty, selectedUnit, price, totalAmount, buyerName, marketName, selectedFieldName, dateStr, notes)
                    }
                },
                modifier = Modifier.testTag("save_income_btn")
            ) {
                Text(text = if (appLanguage == "te") "ఆదాయం సేవ్ చేయి" else "Save Income")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (appLanguage == "te") "రద్దు చేయి" else "Cancel")
            }
        }
    )
}

@Composable
private fun AddFieldModalDialog(
    appLanguage: String,
    onDismiss: () -> Unit,
    onSave: (fieldName: String, area: Double, unit: String, cropName: String, season: String, note: String) -> Unit
) {
    var fieldName by remember { mutableStateOf("Field 1") }
    var areaInput by remember { mutableStateOf("2.5") }
    var selectedUnit by remember { mutableStateOf("Acre") }
    var cropName by remember { mutableStateOf("Maize") }
    var season by remember { mutableStateOf("Kharif") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (appLanguage == "te") "🏞️ కొత్త పొలం / ఫీల్డ్ జతచేయి" else "Add New Farm Field", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fieldName,
                    onValueChange = { fieldName = it },
                    label = { Text("Field Name (ఉదా: Field 1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("field_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = areaInput,
                        onValueChange = { areaInput = it },
                        label = { Text("Area") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("Crop Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val area = areaInput.toDoubleOrNull() ?: 1.0
                    if (fieldName.isNotEmpty()) {
                        onSave(fieldName, area, selectedUnit, cropName, season, "")
                    }
                }
            ) {
                Text(text = "Save Field")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
