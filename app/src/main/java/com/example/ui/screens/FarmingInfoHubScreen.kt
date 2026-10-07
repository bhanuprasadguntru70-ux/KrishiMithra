package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmingInfoHubScreen(
    viewModel: AgriViewModel,
    initialTab: String,
    onBack: () -> Unit
) {
    val tabs = listOf("Motivation", "Education", "Jobs", "Health", "Business", "Technology", "Sports")
    var selectedTab by remember { mutableStateOf(initialTab) }

    LaunchedEffect(initialTab) {
        if (tabs.contains(initialTab)) {
            selectedTab = initialTab
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Farmer Information Hub", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Knowledge, Motivation & Rural Growth", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        // Scrollable Tab Row to accommodate all 7 categories comfortably
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { 
                        Text(
                            text = tab, 
                            fontSize = 13.sp, 
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                        ) 
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                "Motivation" -> MotivationTab()
                "Education" -> EducationTab()
                "Jobs" -> JobsTab()
                "Health" -> HealthTab()
                "Business" -> BusinessTab()
                "Technology" -> TechnologyTab()
                "Sports" -> SportsTab()
            }
        }
    }
}

// --- MODULE 6: FARMER MOTIVATION ---
@Composable
fun MotivationTab() {
    val items = remember {
        listOf(
            InfoItem(
                title = "Success Story: How Organic Polyculture Yields 30% Higher Profits",
                subtitle = "సేంద్రీయ బహుళ పంటల విజయం: 30% అదనపు ఆదాయం",
                body = "A progressive farmer in Eluru switched from traditional chemical farming to organic polyculture (growing papaya, turmeric, and marigold together). Utilizing cow dung slurry, neem-coated urea, and modern drip systems, input costs were slashed by half while yields jumped by 30%.",
                highlight = "💡 Tip: Polyculture reduces risk of complete crop failure.",
                color = Color(0xFF4CAF50)
            ),
            InfoItem(
                title = "Water Saving: The Magic of Mulching Sheet Technology",
                subtitle = "నీటి పొదుపు: మల్చింగ్ షీట్ టెక్నాలజీ యొక్క అద్భుతం",
                body = "By covering the soil surface with organic mulch (straw/paddy husk) or plastic sheets, farmers can prevent soil evaporation by up to 70%. It maintains soil temperature, reduces weed growth, and preserves microbial activity, leading to better root development.",
                highlight = "💧 Water Save Alert: Saves 30,000 Liters of water per acre weekly.",
                color = Color(0xFF2196F3)
            ),
            InfoItem(
                title = "Inspirational Quote of the Day",
                subtitle = "నేటి స్ఫూర్తిదాయక వాక్యం",
                body = "\"The farmer is the only man in our economy who buys everything at retail, sells everything at wholesale, and pays the freight both ways. Yet, they feed the nation with a smile.\" - Stand proud, brothers and sisters, your sweat grows the country's backbone.",
                highlight = "🌱 Proud to be a Farmer!",
                color = Color(0xFFFF9800)
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🌟 Daily Farming Inspiration & Success Stories",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(items) { item ->
            MotivationCard(item)
        }
    }
}

@Composable
fun MotivationCard(item: InfoItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(item.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = item.subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 14.dp, top = 2.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.body,
                fontSize = 13.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp,
                modifier = Modifier.padding(start = 14.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.color.copy(alpha = 0.1f))
                    .padding(8.dp)
            ) {
                Text(
                    text = item.highlight,
                    color = item.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- MODULE 7: EDUCATION & TRAINING ---
@Composable
fun EducationTab() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = LocalUriHandler.current
    
    var selectedSubTab by remember { mutableStateOf("Courses") }
    val subTabs = listOf(
        "Courses", 
        "Training", 
        "Govt Training", 
        "YouTube Learning", 
        "Modern Farming"
    )

    // Data structures for Education Tab
    val courses = remember {
        listOf(
            CourseItem(
                "Diploma in Organic Crop Management",
                "PJTS Agricultural University",
                "6 Months (Online/Hybrid)",
                "Complete foundations of bio-fertilizers, organic farming standards, weed/pest management, and national organic crop certification procedures.",
                "Free for Smallholders"
            ),
            CourseItem(
                "Advanced Seed Science & Nursery Tech",
                "Acharya N.G. Ranga Agricultural University",
                "3 Months (Interactive)",
                "Learn commercial seed multiplication, hybrid pollination, seedling protection, and setting up an independent nursery business.",
                "₹1,200 (Subsidized)"
            ),
            CourseItem(
                "Agri-Business & Food Supply Chains",
                "National Extension Management (MANAGE)",
                "1 Year (Certified)",
                "Become an expert in farm logistics, value-added crop processing, contract farming laws, and international agricultural export standards.",
                "Govt Sponsored"
            )
        )
    }

    val practicalTrainings = remember {
        listOf(
            CourseItem(
                "Mushroom Cultivation & Spawn Production",
                "Krishi Vigyan Kendra (KVK)",
                "5 Days (On-field)",
                "A purely practical hands-on workshop covering straw composting, bag inoculation, temperature/humidity monitoring, and harvesting organic oyster mushrooms.",
                "Free Training"
            ),
            CourseItem(
                "Apiculture & Hive Management Program",
                "National Bee Board (NBB)",
                "7 Days (Field Lab)",
                "Master honey bee colony management, queen rearing, hive disease prevention, honey extraction techniques, and processing commercial honeycomb wax.",
                "Free + Daily Stipend"
            ),
            CourseItem(
                "Drip Irrigation Assembly & Maintenance",
                "Micro-Irrigation Technical Center",
                "2 Days (Workshop)",
                "Learn to design drip emitter lines, clear carbonate blockages, service disc/sand filters, integrate Venturi fertilizer injectors, and automate field valves.",
                "₹200 Kit Charge"
            )
        )
    }

    val govtTrainings = remember {
        listOf(
            CourseItem(
                "PM-DAKSH Agriculture Skill Development",
                "Ministry of Social Justice & Empowerment",
                "45 Days (Residential)",
                "Fully funded residential vocational course. Includes a free soil health testing toolkit, daily allowance, certified course completion, and placement assistance.",
                "100% Free with Stipend"
            ),
            CourseItem(
                "Drone Pilot Training & Remote Spraying",
                "DGCA Approved Flight Academy",
                "10 Days (Simulator & Field)",
                "Subsidized high-tech drone license program. Learn farm GIS boundary mapping, multispectral crop health scanning, and automated ultra-low volume pesticide spraying.",
                "₹4,000 (80% Subsidy)"
            ),
            CourseItem(
                "Integrated Soil Nutrition Management (INM)",
                "State Department of Agriculture",
                "15 Days (Extension)",
                "Comprehensive certification in soil microbiology, custom fertilizer prescription blending, and organic remediation. Satisfies eligibility for fertilizer dealer license.",
                "Free Govt Course"
            )
        )
    }

    val youtubeVideos = remember {
        listOf(
            mapOf(
                "title" to "Low-Cost Drip Irrigation & Mulching Installation (డ్రిప్ మరియు మల్చింగ్ అమరిక)",
                "channel" to "Raitu Nestham TV",
                "duration" to "14 min",
                "views" to "680K views",
                "url" to "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            ),
            mapOf(
                "title" to "How to Prepare Jeevamrutham Bio-Fertilizer (జీవామృతం తయారీ విధానం)",
                "channel" to "Subhash Palekar Natural Farming",
                "duration" to "18 min",
                "views" to "1.2M views",
                "url" to "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            ),
            mapOf(
                "title" to "Smart Drone Pesticide Spraying Demo & Drone Subsidies Guide",
                "channel" to "Krishi Jagran",
                "duration" to "11 min",
                "views" to "240K views",
                "url" to "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            ),
            mapOf(
                "title" to "Polyhouse Greenhouse Vegetable Farming: High Profit Formula",
                "channel" to "Integrated Agri Academy",
                "duration" to "22 min",
                "views" to "410K views",
                "url" to "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            )
        )
    }

    val modernFarmingGuides = remember {
        listOf(
            mapOf(
                "title" to "Precision Agriculture & IoT Soil Sensing",
                "category" to "High-Tech",
                "benefits" to "Saves 40% fertilizer & water, boosts yield by 25%",
                "detail" to "Deploying real-time NPK ground sensors, solar weather nodes, and automated field telemetry to ensure crops get exact doses of moisture and nutrition."
            ),
            mapOf(
                "title" to "Hydroponics & Aeroponics (Soil-less Crops)",
                "category" to "Eco-Efficiency",
                "benefits" to "95% less water usage, year-round continuous harvests",
                "detail" to "Growing leafy crops, strawberries, and herbs directly in aerated water solutions (Hydroponics) or hanging suspended roots misted with vital nutrients (Aeroponics)."
            ),
            mapOf(
                "title" to "Vertical Farming & Controlled Polyhouses",
                "category" to "Smart Automation",
                "benefits" to "10x yield per square foot, zero external weather risk",
                "detail" to "Stacked growing layers under optimal spectrum LED lights with fully computerized temperature, ventilation, and carbon dioxide enrichments."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Horizontal filter buttons/chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            subTabs.forEach { tabName ->
                val isSelected = selectedSubTab == tabName
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = containerColor,
                    contentColor = contentColor,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { selectedSubTab = tabName }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (tabName) {
                            "Courses" -> Icons.Default.School
                            "Training" -> Icons.Default.Build
                            "Govt Training" -> Icons.Default.AccountBalance
                            "YouTube Learning" -> Icons.Default.PlayArrow
                            else -> Icons.Default.Science
                        }
                        Icon(icon, contentDescription = tabName, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = tabName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Card at the top of active tab
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        val headerIcon = when (selectedSubTab) {
                            "Courses" -> Icons.Default.School
                            "Training" -> Icons.Default.Build
                            "Govt Training" -> Icons.Default.AccountBalance
                            "YouTube Learning" -> Icons.Default.PlayArrow
                            else -> Icons.Default.Science
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(headerIcon, contentDescription = "Tab Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val (title, sub) = when (selectedSubTab) {
                                "Courses" -> Pair("Academic Agriculture Courses", "Unlock university-certified diplomas and business management courses.")
                                "Training" -> Pair("Vocational Training Programs", "Gain hand-on expertise in apiculture, mushroom farming, or micro-irrigation.")
                                "Govt Training" -> Pair("Government Sponsored Training", "Access subsidized, residential skill training programs with official certifications.")
                                "YouTube Learning" -> Pair("Farmer YouTube Academy", "Visual step-by-step guides, bio-pesticide tutorials, and field walkthroughs.")
                                else -> Pair("Modern Precision Farming", "Learn the scientific foundations of hydroponics, greenhouses, and soil-less crops.")
                            }
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(sub, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Render based on selected sub-tab
            when (selectedSubTab) {
                "Courses" -> {
                    items(courses) { course ->
                        EduCourseCard(
                            title = course.title,
                            provider = course.provider,
                            duration = course.duration,
                            description = course.description,
                            tag = course.cost,
                            actionLabel = "Enroll In Course",
                            onActionClick = {
                                android.widget.Toast.makeText(context, "Successfully enrolled in: ${course.title}! Course details sent to registered phone.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
                "Training" -> {
                    items(practicalTrainings) { training ->
                        EduCourseCard(
                            title = training.title,
                            provider = training.provider,
                            duration = training.duration,
                            description = training.description,
                            tag = training.cost,
                            actionLabel = "Register Seat",
                            onActionClick = {
                                android.widget.Toast.makeText(context, "Seat reserved for: ${training.title}. Our local KVK coordinator will call you shortly.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
                "Govt Training" -> {
                    items(govtTrainings) { govt ->
                        EduCourseCard(
                            title = govt.title,
                            provider = govt.provider,
                            duration = govt.duration,
                            description = govt.description,
                            tag = govt.cost,
                            actionLabel = "Apply Subsidy / Seat",
                            onActionClick = {
                                android.widget.Toast.makeText(context, "Govt registration request received for: ${govt.title}. Please keep your Aadhaar & Farmer Card ready.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
                "YouTube Learning" -> {
                    items(youtubeVideos) { video ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(video["title"] ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp)
                                        Text("Channel: ${video["channel"]}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFFEBEE))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("YouTube", fontSize = 10.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⏱️ ${video["duration"]}", fontSize = 11.sp, color = Color.Gray)
                                    Text("🔥 ${video["views"]}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            uriHandler.openUri(video["url"] ?: "https://www.youtube.com")
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Opening YouTube link...", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Watch Tutorial Video", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                "Modern Farming" -> {
                    items(modernFarmingGuides) { guide ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(guide["title"] ?: "", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(guide["category"] ?: "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                Text(guide["detail"] ?: "", fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp)
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Eco, contentDescription = "Eco", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Benefits: ${guide["benefits"]}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
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
}

@Composable
fun EduCourseCard(
    title: String,
    provider: String,
    duration: String,
    description: String,
    tag: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp)
                    Text("By: $provider", fontSize = 12.sp, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(tag, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }

            Text(description, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 17.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏳ Duration: $duration", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- MODULE 8: JOBS & CAREERS ---
@Composable
fun JobsTab() {
    val jobs = remember {
        listOf(
            JobListing("Assistant Agriculture Officer (AAO)", "AP Public Service Commission", "Eluru / Nellore / Guntur", "₹35,000 - ₹80,000/month", "B.Sc Agriculture Degree required. Offline exam.", "August 10, 2026"),
            JobListing("Mandi Supervisor / Market Analyst", "Central Warehousing Corp", "All Major Mandis, India", "₹28,000 - ₹50,000/month", "Graduate degree. Basic accounting & local language skills.", "July 28, 2026"),
            JobListing("Agronomy Research Intern", "AgriTech Pvt Ltd", "Work from Field / Remote", "₹12,000 Stipend/month", "Pursuing Agri/Botany degree. Basic knowledge of crop diseases.", "Immediate Join")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "💼 Agricultural Jobs & Internships for Rural Youth",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(jobs) { job ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(job.role, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(job.organization, fontSize = 12.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(job.salary, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(job.location, fontSize = 12.sp, color = Color.Gray)
                    }

                    Text("Requirements: ${job.requirements}", fontSize = 12.sp, color = Color.DarkGray)

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📅 Apply Before: ${job.deadline}", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Apply", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- MODULE 9: HEALTH & SAFETY ---
@Composable
fun HealthTab() {
    val tips = remember {
        listOf(
            HealthTip("Pesticide Spray Safety Guidelines", "Always wear rubber gloves, a face shield mask, and a full-sleeved shirt before opening or mixing liquid pesticides. Never spray against the wind direction to avoid inhaling toxic vapors. Wash your body with soap immediately after spraying.", Icons.Default.Masks, "Critical"),
            HealthTip("Beat the Heat: Summer dehydration protection", "Indian summers can reach 44°C. Drink at least 5 liters of water daily. Mix water with lemon juice, buttermilk (Majiga), or coconut water. Take mandatory breaks under tree shade between 12:00 PM and 3:00 PM.", Icons.Default.LocalActivity, "Precaution"),
            HealthTip("Emergency First Aid: Snake or Insect Bites", "In case of field snake bite, remain extremely calm. Do NOT attempt to cut the wound or suck venom. Immobilize the bitten limb and rush immediately to the nearest community hospital supplying anti-snake venom.", Icons.Default.HealthAndSafety, "Critical")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🏥 Farmer Health & Field Safety Guides",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(tips) { tip ->
            val bgCol = if (tip.severity == "Critical") {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            }
            val borderCol = if (tip.severity == "Critical") Color.Red.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgCol),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (tip.severity == "Critical") Color.Red.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(tip.icon, contentDescription = "Icon", tint = if (tip.severity == "Critical") Color.Red else MaterialTheme.colorScheme.primary)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tip.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (tip.severity == "Critical") Color.Red else MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tip.severity.uppercase(), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(tip.details, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// --- MODULE 10: BUSINESS IDEAS ---
@Composable
fun BusinessTab() {
    val ideas = remember {
        listOf(
            BusinessIdea("Integrated Dairy & Vermicompost Unit", "₹3 Lakhs - ₹5 Lakhs", "₹25,000 - ₹40,000/month", "Purchase 3 high-yielding Murrah buffaloes. Construct brick vermicompost beds below cowsheds to collect urine and dung. Sell packaged cow milk and premium organic compost.", "High demand in nearby town markets."),
            BusinessIdea("Oyster Mushroom Cultivation in Sheds", "₹30,000 - ₹50,000", "₹15,000 - ₹25,000/month", "Construct a dark thatched shed. Pack paddy straw in plastic cylinders with mushroom spawn seeds. Spray water daily. Harvest high-protein organic mushrooms every 20 days.", "Requires very low space, fast business cycle."),
            BusinessIdea("Cold Pressed Oil Extraction (Ganuga)", "₹2.5 Lakhs", "₹35,000 - ₹60,000/month", "Install a wooden cold pressed oil machine. Extract pure unrefined groundnut, sesame, and coconut oils directly from farmer harvests. Sell custom bottles at premium rates.", "Growing urban demand for healthy oils.")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "📈 Highly Profitable Agro-Business Models",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(ideas) { idea ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(idea.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Estimated Investment", fontSize = 10.sp, color = Color.Gray)
                            Text(idea.investment, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Monthly Income Profit", fontSize = 10.sp, color = Color.Gray)
                            Text(idea.expectedProfit, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                    Column {
                        Text("Business Blueprint Strategy:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(idea.strategy, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            .padding(8.dp)
                    ) {
                        Text("🎯 Market Insights: ${idea.market}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// --- MODULE 11: AGRICULTURAL TECHNOLOGY (AGTECH) ---
@Composable
fun TechnologyTab() {
    val technologies = remember {
        listOf(
            AgTechItem("IoT Soil Moisture Sensors", "Smart Irrigation Automation", "Small battery-operated sensor sticks are placed in fields to monitor soil dry-level. This transmits real-time telemetry to the farmer's smartphone, auto-starting pump motors when soil humidity drops below 25%, saving power.", Icons.Default.Wifi),
            AgTechItem("Drone Precision Foliar Sprayers", "Fast Fertilizer Delivery", "Advanced octacopter drone carrying 15 liters of liquid urea or crop pesticide sprays one whole acre in 6 minutes. It utilizes laser sensors to hover exactly 2 meters above crop level, preventing chemical wastage.", Icons.Default.Airplay),
            AgTechItem("Gemini AI Image Pathologists", "Instant Diagnostics", "By using multimodal generative AI (like the Disease Detection module), farmers get immediate prescription results from images. It eliminates the delay of waiting for a manual soil testing report or plant doctor visit.", Icons.Default.AutoAwesome)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🤖 Advanced Farming Technologies (AgTech)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(technologies) { tech ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(tech.icon, contentDescription = "Tech Icon", tint = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(tech.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(tech.details, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp)
                }
            }
        }
    }
}

// --- MODULE 12: RURAL SPORTS NEWS ---
@Composable
fun SportsTab() {
    val sportsNews = remember {
        listOf(
            SportNewsItem("Andhra Pradesh State Kabaddi Tournament in Vijayawada", "Vijayawada will host the massive rural AP State Kabaddi Championship next week. Over 45 village block teams have registered to participate in the open-weight division. Local village players are competing for the main ₹1 Lakh cash prize.", "📅 Starts: July 15, 2026", Icons.Default.SportsFootball),
            SportNewsItem("Rural Block Cricket Tournament Concludes in Guntur", "The final of Guntur Rural Cricket cup ended with a thrilling super-over finish! Eluru Lions clinched victory against Guntur Stars by 4 runs. B. Ramu from Eluru was awarded Man of the Series, receiving a modern power-tiller machine sponsored by regional agricultural co-operative boards.", "🏆 Winners: Eluru Lions", Icons.Default.SportsCricket),
            SportNewsItem("Village Volleyball Cup Registrations Open", "Registration for Guntur Block 3 Village Volleyball Cup is now officially active. Youth crop grower groups can enroll their teams free of cost at the local panchayat office.", "✉️ Register at Local Panchayat", Icons.Default.SportsVolleyball)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🏆 Rural Tournaments & Village Sports News",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(sportsNews) { news ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(news.icon, contentDescription = "Sport Icon", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        }
                        Text(news.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Text(news.content, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(news.meta, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// --- Helper Data Classes ---
data class InfoItem(val title: String, val subtitle: String, val body: String, val highlight: String, val color: Color)
data class CourseItem(val title: String, val provider: String, val duration: String, val description: String, val cost: String)
data class JobListing(val role: String, val organization: String, val location: String, val salary: String, val requirements: String, val deadline: String)
data class HealthTip(val title: String, val details: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val severity: String)
data class BusinessIdea(val name: String, val investment: String, val expectedProfit: String, val strategy: String, val market: String)
data class AgTechItem(val name: String, val category: String, val details: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
data class SportNewsItem(val title: String, val content: String, val meta: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
