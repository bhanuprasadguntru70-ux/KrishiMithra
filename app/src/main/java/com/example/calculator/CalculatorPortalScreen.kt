package com.example.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.crop.*
import com.example.calculator.financial.*
import com.example.calculator.inputs.*
import com.example.calculator.labour.*
import com.example.calculator.land.*
import com.example.calculator.models.CalculationHistoryItem
import com.example.calculator.models.SavedCalculation
import com.example.calculator.profit.ProfitLossCalculatorScreen
import com.example.calculator.sale.CompareMarketsCalculatorScreen
import com.example.calculator.sale.CropSaleCalculatorScreen
import com.example.calculator.tractor.TractorAreaCalculatorScreen
import com.example.calculator.tractor.TractorWorkCalculatorScreen
import com.example.calculator.weight.WeightConverterScreen
import com.example.data.model.MandiCropRate
import com.example.ui.theme.KrishiAccentOrange
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen
import com.example.ui.theme.KrishiTextDark

data class ToolPortalCard(
    val id: String,
    val titleEng: String,
    val titleTel: String,
    val emoji: String,
    val category: String,
    val descriptionEng: String,
    val descriptionTel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorPortalScreen(
    mandiRates: List<MandiCropRate>,
    selectedLang: String,
    savedCalculations: List<SavedCalculation>,
    historyItems: List<CalculationHistoryItem>,
    onSaveCalculation: (SavedCalculation) -> Unit,
    onDeleteSaved: (String) -> Unit,
    activeToolId: String? = null,
    onCloseActiveTool: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var currentLang by remember { mutableStateOf(selectedLang) }
    var openedToolId by remember { mutableStateOf<String?>(activeToolId) }

    LaunchedEffect(activeToolId) {
        if (activeToolId != null) {
            openedToolId = activeToolId
        }
    }

    val toolsList = remember {
        listOf(
            ToolPortalCard("1", "Tractor Work Calculator", "ట్రాక్టర్ పని లెక్కలు", "🚜", "MACHINERY", "Calculate hourly, diesel & driver costs", "గంటల అద్దె, డీజిల్, డ్రైవర్ కూలీ లెక్కలు"),
            ToolPortalCard("2", "Bag → Rupees Calculator", "బస్తాల సొమ్ము లెక్కలు", "📦", "CROP", "Convert bags count & weight to rupees", "బస్తాల సంఖ్య, బరువు నుండి రూపాయల విలువ"),
            ToolPortalCard("3", "Profit / Loss Calculator", "లాభ నష్టాల లెక్కలు", "📈", "MONEY", "Calculate total net profit or loss", "మొత్తం నికర లాభం లేదా నష్టం లెక్కలు"),
            ToolPortalCard("4", "Labour Wage Calculator", "కూలీల రేటు లెక్కలు", "👨‍🌾", "LABOUR", "Daily wages, days & overtime calculation", "రోజువారీ కూలీలు, రోజులు, ఓవర్‌టైమ్"),
            ToolPortalCard("5", "Crop Sale & Deductions", "పంట అమ్మకం మినహాయింపులు", "💰", "MONEY", "Calculate gross sale minus transport/loading", "స్థూల విక్రయం మినహాయింపుల నికర సొమ్ము"),
            ToolPortalCard("6", "Tractor Area Calculator", "పొలం ట్రాక్టర్ లెక్కలు", "🏞️", "MACHINERY", "Calculate tractor cost per acre or hectare", "ఎకరానికి ట్రాక్టర్ ఖర్చు లెక్కలు"),
            ToolPortalCard("7", "Crop Cultivation Cost", "సాగు వ్యయం లెక్కలు", "🌾", "CROP", "Seeds, fertilizers & labour total cost", "విత్తనాలు, ఎరువులు, కూలీల మొత్తం ఖర్చు"),
            ToolPortalCard("8", "Target Sale Price", "లక్ష్య అమ్మకం ధర", "🎯", "CROP", "Required minimum selling price per quintal", "లాభం కోసం కావలసిన క్వింటాల్ కనీస ధర"),
            ToolPortalCard("9", "Compare Markets", "మార్కెట్ల పోలిక", "⚖️", "MONEY", "Compare net returns between APMCs", "వివిధ మార్కెట్ల నికర ఆదాయాల పోలిక"),
            ToolPortalCard("10", "Agricultural Weight Converter", "బరువుల మార్పిడి", "⚖️", "WEIGHT", "Kg, Quintals & Tonnes conversion", "కిలోలు, క్వింటాళ్లు, టన్నుల మార్పిడి"),
            ToolPortalCard("11", "Daily Work & Hourly", "దినసరి పని గంటలు", "👷", "LABOUR", "Calculate worker hours & daily total", "పనిమనిషి గంటలు, రోజువారీ మొత్తం"),
            ToolPortalCard("12", "Seed Requirement & Cost", "విత్తనాల పరిమాణం & ఖర్చు", "🌱", "INPUTS", "Seed requirement per acre & total cost", "ఎకరానికి విత్తనాల పరిమాణం, మొత్తం ఖర్చు"),
            ToolPortalCard("13", "Fertilizer Cost", "ఎరువుల ఖర్చు", "🧪", "INPUTS", "Bags count & fertilizer expense", "ఎరువుల బస్తాల ఖర్చు లెక్కలు"),
            ToolPortalCard("14", "Irrigation / Pump Cost", "నీటి పారుదల ఖర్చు", "💧", "INPUTS", "Pump running hours & fuel cost", "పంప్ రన్నింగ్ గంటలు, ఇంధన ఖర్చు"),
            ToolPortalCard("15", "Cost Per Acre Average", "ఎకరానికి సగటు ఖర్చు", "🏞️", "LAND", "Average cultivation expense per acre", "ఎకరానికి సగటు సాగు వ్యయం"),
            ToolPortalCard("16", "Interest Calculator", "వడ్డీ లెక్కలు", "💰", "FINANCIAL", "Simple & Compound interest with frequencies", "సాధారణ & చక్రవడ్డీ లెక్కలు"),
            ToolPortalCard("17", "Loan EMI Calculator", "బ్యాంక్ EMI లెక్కలు", "🏦", "FINANCIAL", "Monthly EMI, total interest & loan summary", "నెలవారీ EMI మరియు మొత్తం వడ్డీ లెక్కలు"),
            ToolPortalCard("18", "SIP Investment Calculator", "SIP పొదుపు లెక్కలు", "📈", "FINANCIAL", "Monthly SIP & Step-Up investment returns", "నెలవారీ SIP పొదుపు & లాభం లెక్కలు")
        )
    }

    val categories = listOf("ALL", "FINANCIAL", "MACHINERY", "CROP", "LABOUR", "MONEY", "INPUTS", "WEIGHT", "SAVED")

    val filteredTools = toolsList.filter { tool ->
        (selectedCategory == "ALL" || tool.category == selectedCategory) &&
                (searchQuery.isBlank() || tool.titleEng.contains(searchQuery, ignoreCase = true) || tool.titleTel.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KrishiHeaderGreen)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentLang == "తెలుగు") "🧮 ఫార్మ్ కాలిక్యులేటర్లు" else "🧮 Farm Tools & Calculators",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Accurate Farmer Math • No Fake Data",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Language Selector
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (currentLang == "English") Color.White else Color.Transparent)
                                .clickable { currentLang = "English" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ENG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentLang == "English") KrishiHeaderGreen else Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (currentLang == "తెలుగు") Color.White else Color.Transparent)
                                .clickable { currentLang = "తెలుగు" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("తెలుగు", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentLang == "తెలుగు") KrishiHeaderGreen else Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar inside Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = KrishiHeaderGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(if (currentLang == "తెలుగు") "కాలిక్యులేటర్ శోధించండి..." else "Search calculator...", fontSize = 13.sp, color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7FBF8))
        ) {
            // Category Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = when (cat) {
                                    "ALL" -> if (currentLang == "తెలుగు") "అన్నీ" else "ALL TOOLS"
                                    "FINANCIAL" -> if (currentLang == "తెలుగు") "వడ్డీ/లోన్/SIP" else "FINANCIAL & EMI"
                                    "MACHINERY" -> if (currentLang == "తెలుగు") "యంత్రాలు" else "MACHINERY"
                                    "CROP" -> if (currentLang == "తెలుగు") "పంటలు" else "CROPS"
                                    "LABOUR" -> if (currentLang == "తెలుగు") "కూలీలు" else "LABOUR"
                                    "MONEY" -> if (currentLang == "తెలుగు") "డబ్బు/అమ్మకాలు" else "SALES & PROFIT"
                                    "INPUTS" -> if (currentLang == "తెలుగు") "విత్తనాలు/ఎరువులు" else "INPUTS"
                                    "SAVED" -> if (currentLang == "తెలుగు") "సేవ్ చేసినవి (${savedCalculations.size})" else "SAVED (${savedCalculations.size})"
                                    else -> cat
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KrishiHeaderGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (selectedCategory == "SAVED") {
                // SAVED CALCULATIONS TAB
                if (savedCalculations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔖", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (currentLang == "తెలుగు") "సేవ్ చేసిన లెక్కలు ఏవీ లేవు" else "No saved calculations yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = KrishiHeaderGreen
                            )
                            Text(
                                if (currentLang == "తెలుగు") "కాలిక్యులేటర్ ఉపయోగించి SAVE నొక్కండి" else "Calculate and press Save to bookmark your records",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(savedCalculations) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.calculatorName, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = KrishiHeaderGreen)
                                            Text("Saved on: ${item.date}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                        IconButton(onClick = { onDeleteSaved(item.id) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.summary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = KrishiAccentOrange
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // GRID OF CALCULATORS
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTools) { tool ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openedToolId = tool.id }
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEAF5ED)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(tool.emoji, fontSize = 22.sp)
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open",
                                        tint = KrishiHeaderGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (currentLang == "తెలుగు") tool.titleTel else tool.titleEng,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = KrishiHeaderGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = if (currentLang == "తెలుగు") tool.titleEng else tool.titleTel,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (currentLang == "తెలుగు") tool.descriptionTel else tool.descriptionEng,
                                    fontSize = 10.sp,
                                    color = KrishiTextDark,
                                    lineHeight = 12.sp,
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

    // Interactive Modal Dialog for Active Calculator
    if (openedToolId != null) {
        ModalBottomSheet(
            onDismissRequest = { openedToolId = null; onCloseActiveTool() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            val handleSave: (String, String, Map<String, String>) -> Unit = { calcName, summary, details ->
                onSaveCalculation(
                    SavedCalculation(
                        title = calcName,
                        calculatorName = calcName,
                        date = "05 Aug 2026",
                        summary = summary,
                        details = details
                    )
                )
            }

            val closeAction = { openedToolId = null; onCloseActiveTool() }

            when (openedToolId) {
                "1" -> TractorWorkCalculatorScreen(currentLang, handleSave, closeAction)
                "2" -> BagsToRupeesCalculatorScreen(currentLang, handleSave, closeAction)
                "3" -> ProfitLossCalculatorScreen(currentLang, handleSave, closeAction)
                "4" -> LabourWageCalculatorScreen(currentLang, handleSave, closeAction)
                "5" -> CropSaleCalculatorScreen(mandiRates, currentLang, handleSave, closeAction)
                "6" -> TractorAreaCalculatorScreen(currentLang, handleSave, closeAction)
                "7" -> CropCostCalculatorScreen(currentLang, handleSave, closeAction)
                "8" -> SalePriceTargetCalculatorScreen(currentLang, handleSave, closeAction)
                "9" -> CompareMarketsCalculatorScreen(mandiRates, currentLang, handleSave, closeAction)
                "10" -> WeightConverterScreen(currentLang, closeAction)
                "11" -> FarmerDailyWorkCalculatorScreen(currentLang, handleSave, closeAction)
                "12" -> SeedCalculatorScreen(currentLang, handleSave, closeAction)
                "13" -> FertilizerCalculatorScreen(currentLang, handleSave, closeAction)
                "14" -> IrrigationCalculatorScreen(currentLang, handleSave, closeAction)
                "15" -> CostPerAcreCalculatorScreen(currentLang, handleSave, closeAction)
                "16" -> InterestCalculatorScreen(currentLang, handleSave, closeAction)
                "17" -> EmiCalculatorScreen(currentLang, handleSave, closeAction)
                "18" -> SipCalculatorScreen(currentLang, handleSave, closeAction)
                else -> TractorWorkCalculatorScreen(currentLang, handleSave, closeAction)
            }
        }
    }
}
