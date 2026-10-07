package com.example.calculator

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.PortalLanguageSelector

data class FarmCalculatorCardItem(
    val id: String,
    val titleEn: String,
    val titleTe: String,
    val descriptionEn: String,
    val descriptionTe: String,
    val category: String, // MACHINERY, CROP, LABOUR, MONEY, FARM INPUTS, LAND, SALES & PROFIT
    val icon: ImageVector,
    val cardGradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmToolsHomeScreen(
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSelectCalculator: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "MY BOOK", "FINANCIAL", "MACHINERY", "CROP", "LABOUR", "SALES & PROFIT", "FARM INPUTS", "LAND", "MONEY")

    val allCalculators = remember {
        listOf(
            FarmCalculatorCardItem(
                id = "my_book",
                titleEn = "My Book (Farmer Notebook)",
                titleTe = "నా ఖాతా పుస్తకం (నా లెక్కలు)",
                descriptionEn = "Record daily farm income, expenses, crop & field accounts",
                descriptionTe = "దినసరి ఆదాయం, ఖర్చులు, పంటలు, పొలాల లెక్కలు నమోదు చేయండి",
                category = "MY BOOK",
                icon = Icons.Default.MenuBook,
                cardGradient = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
            ),
            FarmCalculatorCardItem(
                id = "interest_calc",
                titleEn = "Interest Calculator",
                titleTe = "వడ్డీ లెక్కలు (సాధారణ & చక్రవడ్డీ)",
                descriptionEn = "Simple & Compound Interest with custom frequency",
                descriptionTe = "సాధారణ వడ్డీ మరియు చక్రవడ్డీ లెక్కింపు సాధనం",
                category = "FINANCIAL",
                icon = Icons.Default.Percent,
                cardGradient = listOf(Color(0xFF0288D1), Color(0xFF01579B))
            ),
            FarmCalculatorCardItem(
                id = "emi_calc",
                titleEn = "EMI Calculator",
                titleTe = "EMI లెక్కలు (రుణ వాయిదాలు)",
                descriptionEn = "Loan EMI, total interest & monthly payment breakdown",
                descriptionTe = "బ్యాంకు లేదా ట్రాక్టర్ రుణాల నెలవారీ EMI వాయిదాల లెక్కలు",
                category = "FINANCIAL",
                icon = Icons.Default.AccountBalance,
                cardGradient = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
            ),
            FarmCalculatorCardItem(
                id = "sip_calc",
                titleEn = "SIP Calculator",
                titleTe = "SIP లెక్కలు (క్రమానుగత పెట్టుబడి)",
                descriptionEn = "Regular & Step-Up SIP investment future estimates",
                descriptionTe = "నెలవారీ పొదుపు, స్టెప్-అప్ SIP పెట్టుబడుల అంచనా",
                category = "FINANCIAL",
                icon = Icons.Default.ShowChart,
                cardGradient = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
            ),
            FarmCalculatorCardItem(
                id = "tractor_work",
                titleEn = "Tractor Work Calculator",
                titleTe = "ట్రాక్టర్ పని లెక్కలు",
                descriptionEn = "Calculate tractor hourly cost, diesel & operator charges",
                descriptionTe = "గంటకు ట్రాక్టర్ బాడిగ, డీజిల్, డ్రైవర్ బత్తా లెక్కలు",
                category = "MACHINERY",
                icon = Icons.Default.Agriculture,
                cardGradient = listOf(Color(0xFF1976D2), Color(0xFF0D47A1))
            ),

            FarmCalculatorCardItem(
                id = "tractor_area",
                titleEn = "Tractor Area Calculator",
                titleTe = "దుక్కి ఎకరం లెక్కలు",
                descriptionEn = "Estimate hours and ploughing cost per acre or cent",
                descriptionTe = "ఎకరం, సెంట్ విస్తీర్ణానికి ట్రాక్టర్ దుక్కి ఖర్చు",
                category = "MACHINERY",
                icon = Icons.Default.SquareFoot,
                cardGradient = listOf(Color(0xFF0288D1), Color(0xFF01579B))
            ),
            FarmCalculatorCardItem(
                id = "crop_cost",
                titleEn = "Crop Cultivation Cost",
                titleTe = "పంట సాగు వ్యయ లెక్కలు",
                descriptionEn = "Total farming investment breakdown (Seed, Fert, Labour)",
                descriptionTe = "మొత్తం పంట పెట్టుబడి విశ్లేషణ (విత్తనం, ఎరువులు, కూలీలు)",
                category = "CROP",
                icon = Icons.Default.Grass,
                cardGradient = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
            ),
            FarmCalculatorCardItem(
                id = "crop_yield_bags",
                titleEn = "Crop Yield / Bags Calculator",
                titleTe = "దిగుబడి & బస్తాల లెక్కలు",
                descriptionEn = "Bags to Kg, Quintals, Tonnes & estimated total value",
                descriptionTe = "బస్తాల సంఖ్య నుండి క్వింటాళ్ళు, టన్నుల మార్పిడి & ధర",
                category = "CROP",
                icon = Icons.Default.Inventory2,
                cardGradient = listOf(Color(0xFF388E3C), Color(0xFF2E7D32))
            ),
            FarmCalculatorCardItem(
                id = "bag_to_rupees",
                titleEn = "Bag → Rupees Calculator",
                titleTe = "బస్తా → రూపాయల లెక్కలు",
                descriptionEn = "Quickly multiply bags with rate per bag or per quintal",
                descriptionTe = "బస్తాల సంఖ్యను బస్తా ధరతో త్వరితగతిన గుణించండి",
                category = "MONEY",
                icon = Icons.Default.CurrencyRupee,
                cardGradient = listOf(Color(0xFFF57C00), Color(0xFFE65100))
            ),
            FarmCalculatorCardItem(
                id = "weight_converter",
                titleEn = "Kg / Quintal / Tonne Converter",
                titleTe = "కిలో / క్వింటాల్ / టన్ను మార్పిడి",
                descriptionEn = "Live mathematical agricultural weight converter",
                descriptionTe = "వ్యవసాయ బరువుల లైవ్ లెక్కల మార్పిడి సాధనం",
                category = "MONEY",
                icon = Icons.Default.Scale,
                cardGradient = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
            ),
            FarmCalculatorCardItem(
                id = "crop_sale",
                titleEn = "Crop Sale & Net Return",
                titleTe = "పంట విక్రయం & నికర రాబడి",
                descriptionEn = "Gross sale minus transport, hamali & commission",
                descriptionTe = "మార్కెట్ విక్రయం నుండి రవాణా మినహాయించగా నికర సొమ్ము",
                category = "SALES & PROFIT",
                icon = Icons.Default.Payments,
                cardGradient = listOf(Color(0xFFD81B60), Color(0xFF880E4F))
            ),
            FarmCalculatorCardItem(
                id = "profit_loss",
                titleEn = "Profit / Loss Calculator",
                titleTe = "లాభం / నష్టం లెక్కలు",
                descriptionEn = "Clear Profit/Loss margin calculation (Income − Cost)",
                descriptionTe = "ఆదాయం − పెట్టుబడి = నికర లాభం లేదా నష్టం",
                category = "SALES & PROFIT",
                icon = Icons.Default.TrendingUp,
                cardGradient = listOf(Color(0xFF00897B), Color(0xFF004D40))
            ),
            FarmCalculatorCardItem(
                id = "labour_wage",
                titleEn = "Labour Wage Calculator",
                titleTe = "కూలీల జీతం లెక్కలు",
                descriptionEn = "Calculate daily wages, workers count & overtime",
                descriptionTe = "కూలీల సంఖ్య, రోజువారీ బాడిగ & ఓవర్‌టైమ్ లెక్కలు",
                category = "LABOUR",
                icon = Icons.Default.People,
                cardGradient = listOf(Color(0xFF5D4037), Color(0xFF3E2723))
            ),
            FarmCalculatorCardItem(
                id = "daily_work",
                titleEn = "Farmer Daily Work Calculator",
                titleTe = "దినసరి పని లెక్కలు",
                descriptionEn = "Track worker hours, break times & hourly pay",
                descriptionTe = "కూలీల గంటల సమయం & గంటకు చెల్లించాల్సిన బాడిగ",
                category = "LABOUR",
                icon = Icons.Default.Timer,
                cardGradient = listOf(Color(0xFF4E342E), Color(0xFF271C19))
            ),
            FarmCalculatorCardItem(
                id = "seed_calc",
                titleEn = "Seed Quantity & Cost",
                titleTe = "విత్తనాల పరిమాణం & ఖర్చు",
                descriptionEn = "Seed requirement per acre and total seed cost",
                descriptionTe = "ఎకరానికి కావాల్సిన విత్తన మోతాదు & ఖర్చు",
                category = "FARM INPUTS",
                icon = Icons.Default.Park,
                cardGradient = listOf(Color(0xFF43A047), Color(0xFF1B5E20))
            ),
            FarmCalculatorCardItem(
                id = "fertilizer_calc",
                titleEn = "Fertilizer Cost Calculator",
                titleTe = "ఎరువుల ఖర్చు లెక్కలు",
                descriptionEn = "DAP, Urea, NPK bag quantity and total cost",
                descriptionTe = "యూరియా, డిఎపి బస్తాల సంఖ్య & బస్తాల ఖర్చు",
                category = "FARM INPUTS",
                icon = Icons.Default.Science,
                cardGradient = listOf(Color(0xFF303F9F), Color(0xFF1A237E))
            ),
            FarmCalculatorCardItem(
                id = "pesticide_calc",
                titleEn = "Pesticide / Input Cost",
                titleTe = "పురుగుమందుల ఖర్చు లెక్కలు",
                descriptionEn = "Calculate spray bottles/packets total cost",
                descriptionTe = "పిచికారీ మందుల ప్యాకెట్లు & బాటిళ్ళ మొత్తం ఖర్చు",
                category = "FARM INPUTS",
                icon = Icons.Default.BugReport,
                cardGradient = listOf(Color(0xFFC2185B), Color(0xFF880E4F))
            ),
            FarmCalculatorCardItem(
                id = "irrigation_calc",
                titleEn = "Irrigation / Water Cost",
                titleTe = "నీటిపారుదల ఖర్చు లెక్కలు",
                descriptionEn = "Pump running hours & power/diesel cost",
                descriptionTe = "మోటార్ నడిచే గంటలు & విద్యుత్/డీజిల్ ఖర్చు",
                category = "FARM INPUTS",
                icon = Icons.Default.WaterDrop,
                cardGradient = listOf(Color(0xFF0097A7), Color(0xFF006064))
            ),
            FarmCalculatorCardItem(
                id = "cost_per_acre",
                titleEn = "Crop Cost Per Acre",
                titleTe = "ఎకరానికి ఖర్చు లెక్కలు",
                descriptionEn = "Average investment per acre or hectare",
                descriptionTe = "ఎకరానికి లేదా హెక్టారుకి అయ్యే సగటు ఖర్చు",
                category = "LAND",
                icon = Icons.Default.Landscape,
                cardGradient = listOf(Color(0xFF689F38), Color(0xFF33691E))
            ),
            FarmCalculatorCardItem(
                id = "target_price",
                titleEn = "Sale Price Target Calculator",
                titleTe = "లక్ష్య అమ్మకపు ధర లెక్కలు",
                descriptionEn = "Required minimum sale price for target profit",
                descriptionTe = "మీరు కోరుకునే లాభం కోసం అమ్మవల్సిన కనీస ధర",
                category = "SALES & PROFIT",
                icon = Icons.Default.AdsClick,
                cardGradient = listOf(Color(0xFFF57F17), Color(0xFFE65100))
            ),
            FarmCalculatorCardItem(
                id = "mandi_calc",
                titleEn = "Live Mandi + Calculator",
                titleTe = "మండి ధరల విక్రయ గణన",
                descriptionEn = "Verified Mandi rates integrated directly with calculator",
                descriptionTe = "లైవ్ ధృవీకరించిన మండి ధరలతో త్వరిత విక్రయ గణన",
                category = "MONEY",
                icon = Icons.Default.Storefront,
                cardGradient = listOf(Color(0xFFE65100), Color(0xFFBF360C))
            ),
            FarmCalculatorCardItem(
                id = "compare_markets",
                titleEn = "Compare Markets Calculator",
                titleTe = "మార్కెట్ల పోలిక & నికర రాబడి",
                descriptionEn = "Compare Mandi A vs B vs C considering transport costs",
                descriptionTe = "రవాణా ఖర్చులను మినహాయించి ఉత్తమ రాబడి ఇచ్చే మార్కెట్",
                category = "SALES & PROFIT",
                icon = Icons.Default.Compare,
                cardGradient = listOf(Color(0xFF512DA8), Color(0xFF311B92))
            )
        )
    }

    val filteredCalculators = remember(searchQuery, selectedCategory, allCalculators) {
        allCalculators.filter { item ->
            val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isEmpty() ||
                    item.titleEn.contains(searchQuery, ignoreCase = true) ||
                    item.titleTe.contains(searchQuery, ignoreCase = true) ||
                    item.descriptionEn.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (appLanguage == "te") "వ్యవసాయ లెక్కలు & టూల్స్" else "Farm Tools & Calculators",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Krishi Mithra Smart Farmer Portal",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                    }
                    PortalLanguageSelector(
                        currentLanguage = appLanguage,
                        onLanguageSelected = onLanguageSelected,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar & History Banner
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (appLanguage == "te") "లెక్కల రకం శోధించండి (ఉదా: ట్రాక్టర్, బస్తా, కూలీ)..." else "Search calculators (e.g. Tractor, Bags, Wage)...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("farm_tools_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // History Quick Tile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenHistory() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (appLanguage == "te") "నా పాత లెక్కల వివరాలు (Saved Calculations)" else "My Saved Calculations & History",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = when (cat) {
                                    "ALL" -> if (appLanguage == "te") "అన్నీ" else "All"
                                    "MACHINERY" -> if (appLanguage == "te") "యంత్రాలు" else "Machinery"
                                    "CROP" -> if (appLanguage == "te") "పంట" else "Crop"
                                    "LABOUR" -> if (appLanguage == "te") "కూలీలు" else "Labour"
                                    "SALES & PROFIT" -> if (appLanguage == "te") "అమ్మకాలు & లాభం" else "Sales & Profit"
                                    "FARM INPUTS" -> if (appLanguage == "te") "సాగు ఇన్పుట్స్" else "Farm Inputs"
                                    "LAND" -> if (appLanguage == "te") "భూమి" else "Land"
                                    "MONEY" -> if (appLanguage == "te") "ఆదాయం" else "Money"
                                    else -> cat
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Calculator Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCalculators, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectCalculator(item.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(item.cardGradient))
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.25f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Open",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = item.titleEn,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = item.titleTe,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFFFECB3),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = if (appLanguage == "te") item.descriptionTe else item.descriptionEn,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
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
