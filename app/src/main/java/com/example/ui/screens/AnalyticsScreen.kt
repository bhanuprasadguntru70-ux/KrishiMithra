package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AgriViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// --- DATA STRUCTURES FOR CHARTS ---
data class DataPoint(
    val label: String,
    val value: Float,
    val secondaryValue: Float? = null,
    val tooltipText: String = ""
)

data class PieSlice(
    val name: String,
    val value: Float,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allMarketCrops by viewModel.allMarketCrops.collectAsState()
    val allFarms by viewModel.allFarms.collectAsState()
    val allReports by viewModel.allReports.collectAsState()

    var selectedTimeframe by remember { mutableStateOf("30 Days") } // "7 Days", "30 Days", "3 Months", "1 Year"
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Financials, 1: Crops & Agronomy, 2: Environment

    val isDark = isSystemInDarkTheme() || viewModel.isDarkMode.collectAsState().value

    // Animation trigger for charts rendering
    var transitionTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(selectedTimeframe, selectedTab) {
        transitionTrigger++
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Analytics Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time agronomic & financial insights",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("analytics_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF111D13) else Color(0xFFF9FBE7)
                )
            )
        },
        containerColor = if (isDark) Color(0xFF111D13) else Color(0xFFF9FBE7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Timeframe Segmented Picker Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("timeframe_selector"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1B2C1F) else Color(0xFFFFFFFF)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Analytics Period",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isDark) Color(0xFF111D13) else Color(0xFFF1F5F0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val timeframes = listOf("7 Days", "30 Days", "3 Months", "1 Year")
                        timeframes.forEach { timeframe ->
                            val isSelected = selectedTimeframe == timeframe
                            val buttonColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                label = "bg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.6f) else Color.DarkGray),
                                label = "text"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(buttonColor)
                                    .clickable { selectedTimeframe = timeframe }
                                    .padding(vertical = 10.dp)
                                    .testTag("timeframe_$timeframe"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = timeframe,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            // Tab Selector Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty() && selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("analytics_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finances", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Gray
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crops & Agronomy", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Gray
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Weather & Market", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Tab Content Layout with Smooth Fading Stagger
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "tabContent"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> FinancialTabContent(
                        timeframe = selectedTimeframe,
                        isDark = isDark,
                        triggerValue = transitionTrigger
                    )
                    1 -> CropsTabContent(
                        timeframe = selectedTimeframe,
                        allFarms = allFarms,
                        allReports = allReports,
                        isDark = isDark,
                        triggerValue = transitionTrigger
                    )
                    2 -> WeatherMarketTabContent(
                        timeframe = selectedTimeframe,
                        allMarketCrops = allMarketCrops,
                        district = currentUser?.district ?: "Eluru",
                        isDark = isDark,
                        triggerValue = transitionTrigger
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// --- FINANCIAL HUB CONTENT ---
// ==========================================
@Composable
fun FinancialTabContent(
    timeframe: String,
    isDark: Boolean,
    triggerValue: Int
) {
    // Generate data based on timeframe selected
    val sampleIncome = remember(timeframe) {
        when (timeframe) {
            "7 Days" -> listOf(
                DataPoint("Mon", 1200f), DataPoint("Tue", 0f), DataPoint("Wed", 4500f),
                DataPoint("Thu", 0f), DataPoint("Fri", 1800f), DataPoint("Sat", 8000f), DataPoint("Sun", 0f)
            )
            "30 Days" -> listOf(
                DataPoint("Week 1", 15000f), DataPoint("Week 2", 22000f),
                DataPoint("Week 3", 8000f), DataPoint("Week 4", 32000f)
            )
            "3 Months" -> listOf(
                DataPoint("May", 85000f), DataPoint("Jun", 98000f), DataPoint("Jul", 145000f)
            )
            else -> listOf(
                DataPoint("Jul", 145000f), DataPoint("Aug", 112000f), DataPoint("Sep", 95000f),
                DataPoint("Oct", 180000f), DataPoint("Nov", 250000f), DataPoint("Dec", 60000f),
                DataPoint("Jan", 45000f), DataPoint("Feb", 80000f), DataPoint("Mar", 195000f),
                DataPoint("Apr", 320000f), DataPoint("May", 90000f), DataPoint("Jun", 110000f)
            )
        }
    }

    val sampleExpenses = remember(timeframe) {
        when (timeframe) {
            "7 Days" -> listOf(
                DataPoint("Mon", 600f), DataPoint("Tue", 1500f), DataPoint("Wed", 800f),
                DataPoint("Thu", 2100f), DataPoint("Fri", 500f), DataPoint("Sat", 1200f), DataPoint("Sun", 300f)
            )
            "30 Days" -> listOf(
                DataPoint("Week 1", 8500f), DataPoint("Week 2", 12000f),
                DataPoint("Week 3", 6400f), DataPoint("Week 4", 15000f)
            )
            "3 Months" -> listOf(
                DataPoint("May", 45000f), DataPoint("Jun", 51000f), DataPoint("Jul", 62000f)
            )
            else -> listOf(
                DataPoint("Jul", 62000f), DataPoint("Aug", 55000f), DataPoint("Sep", 48000f),
                DataPoint("Oct", 85000f), DataPoint("Nov", 120000f), DataPoint("Dec", 35000f),
                DataPoint("Jan", 30000f), DataPoint("Feb", 42000f), DataPoint("Mar", 78000f),
                DataPoint("Apr", 140000f), DataPoint("May", 41000f), DataPoint("Jun", 49000f)
            )
        }
    }

    val profitData = remember(timeframe) {
        sampleIncome.mapIndexed { idx, inc ->
            val exp = sampleExpenses.getOrNull(idx)?.value ?: 0f
            DataPoint(inc.label, inc.value - exp)
        }
    }

    val sampleHarvestPie = remember(timeframe) {
        listOf(
            PieSlice("Paddy (Rice)", 45f, Color(0xFF4CAF50)),
            PieSlice("Cotton", 25f, Color(0xFF64B5F6)),
            PieSlice("Sugarcane", 18f, Color(0xFFFFB74D)),
            PieSlice("Turmeric & Spices", 12f, Color(0xFFBA68C8))
        )
    }

    val totalInc = sampleIncome.sumOf { it.value.toDouble() }.toFloat()
    val totalExp = sampleExpenses.sumOf { it.value.toDouble() }.toFloat()
    val totalProfit = totalInc - totalExp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Core Statistics Cards Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatSummaryCard(
                title = "Total Income",
                value = "₹${formatAmount(totalInc)}",
                changeText = "+12% vs last period",
                isPositive = true,
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
            StatSummaryCard(
                title = "Total Expenses",
                value = "₹${formatAmount(totalExp)}",
                changeText = "+4% inflation rise",
                isPositive = false,
                icon = Icons.Default.TrendingDown,
                color = Color(0xFFE57373),
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
        }

        StatSummaryCard(
            title = "Net Profit Margin",
            value = "₹${formatAmount(totalProfit)}",
            changeText = "Average Net Margin: ${(if (totalInc > 0f) ((totalProfit / totalInc) * 100f).toInt() else 0)}%",
            isPositive = totalProfit >= 0f,
            icon = Icons.Default.Payments,
            color = Color(0xFF4FC3F7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 6: Monthly Income
        ChartCard(
            title = "Monthly Income Trend",
            description = "Aggregate revenue cashflow across sales channels",
            isDark = isDark
        ) {
            BarChart(
                dataPoints = sampleIncome,
                barColor = Color(0xFF81C784),
                isCurrency = true,
                triggerValue = triggerValue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 7: Monthly Expenses
        ChartCard(
            title = "Expense Allocation Breakdown",
            description = "Tracked seed inputs, machinery hiring, fertilizer, & water fees",
            isDark = isDark
        ) {
            BarChart(
                dataPoints = sampleExpenses,
                barColor = Color(0xFFE57373),
                isCurrency = true,
                triggerValue = triggerValue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 8: Profit Analysis
        ChartCard(
            title = "Net Profit Analysis",
            description = "Deductions of cumulative expenses from gross farm revenues",
            isDark = isDark
        ) {
            LineChart(
                dataPoints = profitData,
                lineColor = Color(0xFF4FC3F7),
                isCurrency = true,
                triggerValue = triggerValue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 12: Harvest Production (Pie Chart)
        ChartCard(
            title = "Seasonal Harvest Volume Projection",
            description = "Percentage allocation of projected yields for current crops",
            isDark = isDark
        ) {
            DonutPieChart(
                slices = sampleHarvestPie,
                totalLabel = "Total Volume",
                totalValue = "120 Tons",
                triggerValue = triggerValue,
                isDark = isDark
            )
        }
    }
}

// ==========================================
// --- CROPS & AGRONOMY CONTENT ---
// ==========================================
@Composable
fun CropsTabContent(
    timeframe: String,
    allFarms: List<com.example.data.model.FarmEntity>,
    allReports: List<com.example.data.model.DiseaseReportEntity>,
    isDark: Boolean,
    triggerValue: Int
) {
    // Crop Yield data
    val yieldData = remember(allFarms, timeframe) {
        if (allFarms.isNotEmpty()) {
            allFarms.map { farm ->
                val acreage = farm.area.removeSuffix("Acres").trim().toFloatOrNull() ?: 1.0f
                val baseValue = when (farm.crop.lowercase()) {
                    "rice", "paddy" -> 22.0f
                    "cotton" -> 14.0f
                    "sugarcane" -> 85.0f
                    "maize", "corn" -> 28.0f
                    else -> 18.0f
                }
                val predictedYield = baseValue * acreage
                DataPoint(farm.crop, predictedYield, secondaryValue = baseValue)
            }
        } else {
            listOf(
                DataPoint("Paddy", 44f, 22f),
                DataPoint("Cotton", 28f, 14f),
                DataPoint("Sugarcane", 85f, 85f),
                DataPoint("Maize", 56f, 28f)
            )
        }
    }

    // Disease Reports aggregation
    val diseaseSlices = remember(allReports) {
        if (allReports.isNotEmpty()) {
            val groups = allReports.groupBy { it.diseaseName }
            val colors = listOf(Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4FC3F7), Color(0xFFAED581))
            groups.entries.mapIndexed { idx, entry ->
                PieSlice(entry.key, entry.value.size.toFloat(), colors[idx % colors.size])
            }
        } else {
            listOf(
                PieSlice("Blast Disease (Paddy)", 4f, Color(0xFFE57373)),
                PieSlice("Leaf Spot (Cotton)", 2f, Color(0xFFFFB74D)),
                PieSlice("Stem Rot (Sugarcane)", 1f, Color(0xFFBA68C8)),
                PieSlice("Healthy Crops", 15f, Color(0xFF81C784))
            )
        }
    }

    // Fertilizer Usage (N-P-K distribution)
    val fertilizerNPK = remember(timeframe) {
        listOf(
            DataPoint("N (Nitrogen)", 60f, tooltipText = "Supports healthy foliage & vegetative growth"),
            DataPoint("P (Phosphorus)", 45f, tooltipText = "Develops robust roots & flowers"),
            DataPoint("K (Potassium)", 55f, tooltipText = "Provides plant stamina & disease resistance")
        )
    }

    // Water Usage trends in Kilo-Liters
    val waterUsage = remember(timeframe) {
        when (timeframe) {
            "7 Days" -> listOf(
                DataPoint("Mon", 12f), DataPoint("Tue", 15f), DataPoint("Wed", 8f),
                DataPoint("Thu", 14f), DataPoint("Fri", 16f), DataPoint("Sat", 11f), DataPoint("Sun", 9f)
            )
            "30 Days" -> listOf(
                DataPoint("Week 1", 85f), DataPoint("Week 2", 95f),
                DataPoint("Week 3", 70f), DataPoint("Week 4", 102f)
            )
            else -> listOf(
                DataPoint("May", 320f), DataPoint("Jun", 450f), DataPoint("Jul", 280f)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Tip Context Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tips",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Agronomic trends sync daily with your registered farm logs to compute precise irrigation requirements and NPK margins.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )
            }
        }

        // Chart 5: Crop Yield
        ChartCard(
            title = "Estimated Crop Yield Analysis",
            description = "Calculated output projection in Quintals (Bars) vs Base Yield (Green Dot)",
            isDark = isDark
        ) {
            BarChart(
                dataPoints = yieldData,
                barColor = Color(0xFF81C784),
                isCurrency = false,
                triggerValue = triggerValue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 9: Disease Detection Statistics (Pie Chart)
        ChartCard(
            title = "Regional Disease Diagnostics",
            description = "Overview of recorded pest invasions & crop infections",
            isDark = isDark
        ) {
            DonutPieChart(
                slices = diseaseSlices,
                totalLabel = "Total Scans",
                totalValue = "${diseaseSlices.sumOf { it.value.toDouble() }.toInt()}",
                triggerValue = triggerValue,
                isDark = isDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 10: Fertilizer Usage
        ChartCard(
            title = "Optimal Fertilizer N-P-K Ratio",
            description = "Standard nutrient balances distributed (kg/Hectare) for seasonal health",
            isDark = isDark
        ) {
            BarChart(
                dataPoints = fertilizerNPK,
                barColor = Color(0xFFFFB74D),
                isCurrency = false,
                triggerValue = triggerValue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 11: Water Usage
        ChartCard(
            title = "Cumulative Irrigation Consumption",
            description = "Water pumped onto fields measured in Kilo-Liters (KL)",
            isDark = isDark
        ) {
            LineChart(
                dataPoints = waterUsage,
                lineColor = Color(0xFF64B5F6),
                isCurrency = false,
                triggerValue = triggerValue
            )
        }
    }
}

// ==========================================
// --- ENVIRONMENT & MARKET CONTENT ---
// ==========================================
@Composable
fun WeatherMarketTabContent(
    timeframe: String,
    allMarketCrops: List<com.example.data.model.MarketCropEntity>,
    district: String,
    isDark: Boolean,
    triggerValue: Int
) {
    // 1. Market Price Trend (from local DB or standard simulated)
    val marketTrendData = remember(allMarketCrops, timeframe) {
        val defaultCrops = listOf("Paddy", "Cotton", "Turmeric", "Maize", "Chilli")
        val basePrice = if (allMarketCrops.isNotEmpty()) allMarketCrops.first().todayPrice.toFloat() else 4200f
        
        when (timeframe) {
            "7 Days" -> listOf(
                DataPoint("Mon", basePrice - 100f), DataPoint("Tue", basePrice - 50f), DataPoint("Wed", basePrice),
                DataPoint("Thu", basePrice + 80f), DataPoint("Fri", basePrice + 120f), DataPoint("Sat", basePrice + 90f),
                DataPoint("Sun", basePrice + 150f)
            )
            "30 Days" -> listOf(
                DataPoint("Week 1", basePrice - 300f), DataPoint("Week 2", basePrice - 150f),
                DataPoint("Week 3", basePrice + 100f), DataPoint("Week 4", basePrice + 250f)
            )
            else -> listOf(
                DataPoint("Jan", basePrice - 1000f), DataPoint("Feb", basePrice - 800f), DataPoint("Mar", basePrice - 400f),
                DataPoint("Apr", basePrice + 200f), DataPoint("May", basePrice + 600f), DataPoint("Jun", basePrice + 900f)
            )
        }
    }

    // 2. Weather Trend (Simulated seasonal forecasting)
    val weatherTrend = remember(timeframe) {
        when (timeframe) {
            "7 Days" -> listOf(
                DataPoint("Mon", 34f, 26f), DataPoint("Tue", 35f, 25f), DataPoint("Wed", 32f, 24f),
                DataPoint("Thu", 33f, 25f), DataPoint("Fri", 36f, 27f), DataPoint("Sat", 34f, 26f),
                DataPoint("Sun", 33f, 24f)
            )
            else -> listOf(
                DataPoint("Week 1", 34f, 25f), DataPoint("Week 2", 36f, 26f),
                DataPoint("Week 3", 33f, 24f), DataPoint("Week 4", 35f, 25f)
            )
        }
    }

    // 3. Rainfall %
    val rainfallData = remember(timeframe) {
        when (timeframe) {
            "7 Days" -> listOf(
                DataPoint("Mon", 15f), DataPoint("Tue", 40f), DataPoint("Wed", 85f),
                DataPoint("Thu", 60f), DataPoint("Fri", 20f), DataPoint("Sat", 10f), DataPoint("Sun", 50f)
            )
            "30 Days" -> listOf(
                DataPoint("Week 1", 20f), DataPoint("Week 2", 45f),
                DataPoint("Week 3", 90f), DataPoint("Week 4", 30f)
            )
            else -> listOf(
                DataPoint("May", 15f), DataPoint("Jun", 65f), DataPoint("Jul", 92f)
            )
        }
    }

    // 4. Temperature Trend - Dual-line
    val temperatureTrend = remember(timeframe) {
        when (timeframe) {
            "7 Days" -> listOf(
                DataPoint("Mon", 34f, 25f), DataPoint("Tue", 35f, 26f), DataPoint("Wed", 32f, 24f),
                DataPoint("Thu", 33f, 25f), DataPoint("Fri", 36f, 27f), DataPoint("Sat", 34f, 26f),
                DataPoint("Sun", 31f, 23f)
            )
            else -> listOf(
                DataPoint("Week 1", 35f, 26f), DataPoint("Week 2", 37f, 28f),
                DataPoint("Week 3", 33f, 24f), DataPoint("Week 4", 34f, 25f)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Location Badge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1B2C1F) else Color(0xFFFFFFFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Current Station: ${district.replaceFirstChar { it.uppercase() }}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sensors syncing live with Indian Meteorological Station",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Chart 1: Market Price Trend
        ChartCard(
            title = "Market Price Index Trend",
            description = "Regional pricing index (₹ per Quintal) for high demand crop varieties",
            isDark = isDark
        ) {
            LineChart(
                dataPoints = marketTrendData,
                lineColor = Color(0xFFFFB74D),
                isCurrency = true,
                triggerValue = triggerValue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 2: Weather Trend (Line Chart)
        ChartCard(
            title = "Humidity & Solar Comfort Level",
            description = "Calculated diurnal indexes for moisture absorption vs soil crusting",
            isDark = isDark
        ) {
            LineChart(
                dataPoints = weatherTrend,
                lineColor = Color(0xFFAED581),
                isCurrency = false,
                triggerValue = triggerValue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 3: Rainfall % (Bar/Column Chart)
        ChartCard(
            title = "Precipitation Probability (%)",
            description = "Atmospheric rain indexes computed over the selected period",
            isDark = isDark
        ) {
            BarChart(
                dataPoints = rainfallData,
                barColor = Color(0xFF4FC3F7),
                isCurrency = false,
                triggerValue = triggerValue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 4: Temperature Trend (Dual Line Chart)
        ChartCard(
            title = "Temperature Range Forecast (°C)",
            description = "High (Yellow) and Low (Blue) temperature projections",
            isDark = isDark
        ) {
            DualLineChart(
                dataPoints = temperatureTrend,
                colorMax = Color(0xFFFFD54F),
                colorMin = Color(0xFF64B5F6),
                triggerValue = triggerValue
            )
        }
    }
}

// ==========================================
// --- REUSABLE CARD WRAPPER ---
// ==========================================
@Composable
fun ChartCard(
    title: String,
    description: String,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chart_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1B2C1F) else Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun StatSummaryCard(
    title: String,
    value: String,
    changeText: String,
    isPositive: Boolean,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1B2C1F) else Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isPositive) Color(0xFF81C784) else Color(0xFFE57373),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = changeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isPositive) Color(0xFF81C784) else Color(0xFFE57373)
                )
            }
        }
    }
}

// ==========================================
// --- CUSTOM HIGH-PERFORMANCE CHARTS ---
// ==========================================

// 1. ANIMATED LINE CHART
@Composable
fun LineChart(
    dataPoints: List<DataPoint>,
    lineColor: Color,
    isCurrency: Boolean = false,
    triggerValue: Int
) {
    if (dataPoints.isEmpty()) return

    val maxVal = remember(dataPoints) { dataPoints.maxOf { it.value }.let { if (it == 0f) 1f else it } }
    val minVal = remember(dataPoints) { dataPoints.minOf { it.value } }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    // Smooth float scale transitions
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(triggerValue, dataPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                            val clickedIndex = (offset.x / stepX).plus(0.5f).toInt()
                            selectedIndex = if (clickedIndex in dataPoints.indices) clickedIndex else -1
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val paddingBottom = 20.dp.toPx()
                val paddingTop = 10.dp.toPx()
                val graphHeight = height - paddingBottom - paddingTop

                val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

                // Grid background lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingTop + (graphHeight * i / gridLines)
                    drawLine(
                        color = lineColor.copy(alpha = 0.1f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Path line
                val path = Path()
                val fillPath = Path()

                dataPoints.forEachIndexed { index, point ->
                    // Normalize Y coordinate with animations progress
                    val normY = (point.value / maxVal) * animatedProgress.value
                    val x = index * stepX
                    val y = height - paddingBottom - (normY * graphHeight)

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, y)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevNormY = (dataPoints[index - 1].value / maxVal) * animatedProgress.value
                        val prevY = height - paddingBottom - (prevNormY * graphHeight)

                        // Smooth bezier approximation
                        path.cubicTo(
                            prevX + stepX / 2f, prevY,
                            x - stepX / 2f, y,
                            x, y
                        )
                        fillPath.cubicTo(
                            prevX + stepX / 2f, prevY,
                            x - stepX / 2f, y,
                            x, y
                        )
                    }

                    if (index == dataPoints.lastIndex) {
                        fillPath.lineTo(x, height - paddingBottom)
                        fillPath.lineTo(0f, height - paddingBottom)
                        fillPath.close()
                    }
                }

                // Draw filled gradient area
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = paddingTop,
                        endY = height - paddingBottom
                    )
                )

                // Draw solid line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw dots / interactive handles
                dataPoints.forEachIndexed { index, point ->
                    val normY = (point.value / maxVal) * animatedProgress.value
                    val x = index * stepX
                    val y = height - paddingBottom - (normY * graphHeight)

                    if (index == selectedIndex) {
                        // Vertical guideline
                        drawLine(
                            color = lineColor.copy(alpha = 0.4f),
                            start = Offset(x, paddingTop),
                            end = Offset(x, height - paddingBottom),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Glowing selector ring
                        drawCircle(
                            color = lineColor.copy(alpha = 0.3f),
                            radius = 10.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Dot center
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal bottom label layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dataPoints.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                Text(
                    text = point.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) lineColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active selection tooltip banner
        AnimatedVisibility(
            visible = selectedIndex in dataPoints.indices,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val point = dataPoints.getOrNull(selectedIndex)
            if (point != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(lineColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected: ${point.label}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCurrency) "₹${point.value.toInt()}" else "${point.value.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = lineColor
                    )
                }
            }
        }
    }
}

// 2. ANIMATED DUAL LINE CHART (For Temperature Trends Min & Max)
@Composable
fun DualLineChart(
    dataPoints: List<DataPoint>,
    colorMax: Color,
    colorMin: Color,
    triggerValue: Int
) {
    if (dataPoints.isEmpty()) return

    val maxVal = remember(dataPoints) { dataPoints.maxOf { it.value } }
    val minVal = remember(dataPoints) { dataPoints.minOf { it.secondaryValue ?: 0f } }
    val absMax = (maxVal + 5f).coerceAtLeast(1f)

    var selectedIndex by remember { mutableIntStateOf(-1) }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(triggerValue, dataPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(750, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                            val clickedIndex = (offset.x / stepX).plus(0.5f).toInt()
                            selectedIndex = if (clickedIndex in dataPoints.indices) clickedIndex else -1
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val paddingBottom = 20.dp.toPx()
                val paddingTop = 10.dp.toPx()
                val graphHeight = height - paddingBottom - paddingTop

                val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

                // Grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = paddingTop + (graphHeight * i / gridLines)
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val pathMax = Path()
                val pathMin = Path()

                dataPoints.forEachIndexed { index, point ->
                    val normYMax = (point.value / absMax) * animatedProgress.value
                    val normYMin = ((point.secondaryValue ?: point.value) / absMax) * animatedProgress.value

                    val x = index * stepX
                    val yMax = height - paddingBottom - (normYMax * graphHeight)
                    val yMin = height - paddingBottom - (normYMin * graphHeight)

                    if (index == 0) {
                        pathMax.moveTo(x, yMax)
                        pathMin.moveTo(x, yMin)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevNormYMax = (dataPoints[index - 1].value / absMax) * animatedProgress.value
                        val prevNormYMin = ((dataPoints[index - 1].secondaryValue ?: dataPoints[index - 1].value) / absMax) * animatedProgress.value

                        val prevYMax = height - paddingBottom - (prevNormYMax * graphHeight)
                        val prevYMin = height - paddingBottom - (prevNormYMin * graphHeight)

                        pathMax.cubicTo(prevX + stepX/2f, prevYMax, x - stepX/2f, yMax, x, yMax)
                        pathMin.cubicTo(prevX + stepX/2f, prevYMin, x - stepX/2f, yMin, x, yMin)
                    }
                }

                // Draw Paths
                drawPath(path = pathMax, color = colorMax, style = Stroke(width = 2.5.dp.toPx()))
                drawPath(path = pathMin, color = colorMin, style = Stroke(width = 2.5.dp.toPx()))

                // Circles
                dataPoints.forEachIndexed { index, point ->
                    val x = index * stepX
                    val normYMax = (point.value / absMax) * animatedProgress.value
                    val normYMin = ((point.secondaryValue ?: point.value) / absMax) * animatedProgress.value

                    val yMax = height - paddingBottom - (normYMax * graphHeight)
                    val yMin = height - paddingBottom - (normYMin * graphHeight)

                    // Draw selector line if clicked
                    if (index == selectedIndex) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.2f),
                            start = Offset(x, paddingTop),
                            end = Offset(x, height - paddingBottom),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    drawCircle(color = colorMax, radius = 3.dp.toPx(), center = Offset(x, yMax))
                    drawCircle(color = colorMin, radius = 3.dp.toPx(), center = Offset(x, yMin))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal bottom label layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dataPoints.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                Text(
                    text = point.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Tooltip Banner
        AnimatedVisibility(
            visible = selectedIndex in dataPoints.indices,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            val point = dataPoints.getOrNull(selectedIndex)
            if (point != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(colorMax.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Day: ${point.label}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Max: ${point.value.toInt()}°C", fontSize = 11.sp, color = colorMax, fontWeight = FontWeight.Bold)
                        Text("Min: ${(point.secondaryValue ?: point.value).toInt()}°C", fontSize = 11.sp, color = colorMin, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. ANIMATED BAR CHART (With pill rounded corners & clean alignment)
@Composable
fun BarChart(
    dataPoints: List<DataPoint>,
    barColor: Color,
    isCurrency: Boolean = false,
    triggerValue: Int
) {
    if (dataPoints.isEmpty()) return

    val maxVal = remember(dataPoints) { dataPoints.maxOf { it.value }.let { if (it == 0f) 1f else it } }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(triggerValue, dataPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(650, easing = LinearOutSlowInEasing)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val barSpacingRatio = 0.35f
                            val numBars = dataPoints.size
                            val barAndSpaceWidth = width / numBars
                            val clickedIndex = (offset.x / barAndSpaceWidth).toInt()
                            selectedIndex = if (clickedIndex in dataPoints.indices) clickedIndex else -1
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val paddingBottom = 20.dp.toPx()
                val paddingTop = 10.dp.toPx()
                val graphHeight = height - paddingBottom - paddingTop

                val numBars = dataPoints.size
                val barAndSpaceWidth = width / numBars
                val barSpacingWidth = barAndSpaceWidth * 0.35f
                val barWidth = barAndSpaceWidth - barSpacingWidth

                // Draw background grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = paddingTop + (graphHeight * i / gridLines)
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                dataPoints.forEachIndexed { index, point ->
                    val normHeight = (point.value / maxVal) * animatedProgress.value
                    val x = index * barAndSpaceWidth + (barSpacingWidth / 2f)
                    val y = height - paddingBottom - (normHeight * graphHeight)

                    val isSelected = index == selectedIndex
                    val finalBarColor = if (isSelected) barColor.copy(alpha = 0.8f) else barColor

                    // Draw pill bar using rounded rectangles
                    drawRoundRect(
                        color = finalBarColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, normHeight * graphHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Secondary indicator dot if available (for Target yields etc.)
                    if (point.secondaryValue != null) {
                        val secondaryNormY = (point.secondaryValue / maxVal) * animatedProgress.value
                        val dotY = height - paddingBottom - (secondaryNormY * graphHeight)
                        drawCircle(
                            color = Color(0xFF4FC3F7),
                            radius = 3.5.dp.toPx(),
                            center = Offset(x + barWidth / 2f, dotY)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal bottom labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dataPoints.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                Text(
                    text = point.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) barColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active selected bar text overlay
        AnimatedVisibility(
            visible = selectedIndex in dataPoints.indices,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val point = dataPoints.getOrNull(selectedIndex)
            if (point != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(barColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Item: ${point.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isCurrency) "₹${point.value.toInt()}" else "${point.value.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    }
                    if (point.tooltipText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = point.tooltipText,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// 4. ANIMATED PIE/DONUT CHART (With segmented labels and interactive center)
@Composable
fun DonutPieChart(
    slices: List<PieSlice>,
    totalLabel: String,
    totalValue: String,
    triggerValue: Int,
    isDark: Boolean
) {
    if (slices.isEmpty()) return

    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat() }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    val animatedSweep = remember { Animatable(0f) }
    LaunchedEffect(triggerValue, slices) {
        animatedSweep.snapTo(0f)
        animatedSweep.animateTo(
            targetValue = 360f,
            animationSpec = tween(900, easing = FastOutSlowInEasing)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Doughnut Canvas
        Box(
            modifier = Modifier
                .size(140.dp)
                .weight(1.1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(slices) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val height = size.height
                            val center = Offset(width / 2f, height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            var currentSum = 0f
                            var clickedIdx = -1
                            for (i in slices.indices) {
                                val sliceSweep = (slices[i].value / total) * 360f
                                if (angle >= currentSum && angle < (currentSum + sliceSweep)) {
                                    clickedIdx = i
                                    break
                                }
                                currentSum += sliceSweep
                            }
                            selectedIndex = if (clickedIdx == selectedIndex) -1 else clickedIdx
                        }
                    }
            ) {
                val strokeWidth = 24.dp.toPx()
                val radius = (size.width - strokeWidth) / 2f
                val arcSize = Size(radius * 2f, radius * 2f)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                var currentAngle = 0f
                slices.forEachIndexed { index, slice ->
                    val sweepAngle = (slice.value / total) * 360f
                    val isSelected = index == selectedIndex
                    val finalStroke = if (isSelected) strokeWidth * 1.3f else strokeWidth

                    drawArc(
                        color = slice.color,
                        startAngle = currentAngle,
                        sweepAngle = sweepAngle * (animatedSweep.value / 360f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = finalStroke, cap = StrokeCap.Round)
                    )
                    currentAngle += sweepAngle
                }
            }

            // Doughnut Center Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = if (selectedIndex in slices.indices) slices[selectedIndex].name.take(12) + ".." else totalLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (selectedIndex in slices.indices) "${slices[selectedIndex].value.toInt()}" else totalValue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedIndex in slices.indices) slices[selectedIndex].color else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Side Legend List
        Column(
            modifier = Modifier.weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.forEachIndexed { index, slice ->
                val isSelected = index == selectedIndex
                val alphaValue by animateFloatAsState(targetValue = if (selectedIndex == -1 || isSelected) 1f else 0.35f, label = "alpha")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) slice.color.copy(alpha = 0.08f) else Color.Transparent)
                        .clickable { selectedIndex = if (isSelected) -1 else index }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slice.color, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = slice.name,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alphaValue),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// --- CURRENCY UTILITY FORMATTER ---
private fun formatAmount(amount: Float): String {
    return when {
        amount >= 100000 -> String.format(Locale.ENGLISH, "%.1fL", amount / 100000)
        amount >= 1000 -> String.format(Locale.ENGLISH, "%.1fk", amount / 1000)
        else -> String.format(Locale.ENGLISH, "%.0f", amount)
    }
}