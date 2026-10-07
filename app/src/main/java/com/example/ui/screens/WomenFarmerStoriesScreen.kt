package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.WomanFarmerStoryEntity
import com.example.ui.AgriViewModel
import com.example.ui.util.PortalLanguageSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WomenFarmerStoriesScreen(
    agriViewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage by agriViewModel.appLanguage.collectAsStateWithLifecycle()
    val isTelugu by agriViewModel.isTelugu.collectAsStateWithLifecycle()

    val approvedStories by agriViewModel.approvedWomenFarmerStories.collectAsStateWithLifecycle()
    val pendingStories by agriViewModel.pendingWomenFarmerStories.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStoryForDetail by remember { mutableStateOf<WomanFarmerStoryEntity?>(null) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            "All" to if (isTelugu) "అన్నీ" else "All Stories",
            "మహిళా రైతు" to if (isTelugu) "మహిళా రైతు" else "Women Farmer",
            "వ్యవసాయ కార్మికురాలు" to if (isTelugu) "వ్యవసాయ కార్మికురాలు" else "Agri Worker",
            "పశుపోషణ" to if (isTelugu) "పశుపోషణ" else "Livestock Rearing",
            "కూరగాయల సాగు" to if (isTelugu) "కూరగాయల సాగు" else "Vegetable Cultivation",
            "పాడి పరిశ్రమ" to if (isTelugu) "పాడి పరిశ్రమ" else "Dairy Farming",
            "విత్తన/పంట పనులు" to if (isTelugu) "విత్తన/పంట పనులు" else "Seed/Crop Works",
            "స్వయం ఉపాధి" to if (isTelugu) "స్వయం ఉపాధి" else "Self Employment"
        )
    }

    val filteredStories = remember(approvedStories, selectedCategory) {
        if (selectedCategory == "All") {
            approvedStories
        } else {
            approvedStories.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTelugu) "మా మహిళా రైతుల స్ఫూర్తి కథలు" else "Women Farmers' Inspiration Stories",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isTelugu) "ప్రతిరోజూ కష్టపడే మహిళా రైతులకు మా గౌరవం" else "Honoring hardworking women farmers every day",
                            fontSize = 11.sp,
                            color = Color(0xFFFFD54F)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("women_stories_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    PortalLanguageSelector(
                        currentLanguage = appLanguage,
                        onLanguageSelected = { agriViewModel.setAppLanguage(it) },
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Admin Approval Panel Toggle Badge
                    if (pendingStories.isNotEmpty() || showAdminPanel) {
                        IconButton(
                            onClick = { showAdminPanel = !showAdminPanel },
                            modifier = Modifier.testTag("admin_approval_toggle_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (pendingStories.isNotEmpty()) {
                                        Badge(containerColor = Color(0xFFEF5350)) {
                                            Text("${pendingStories.size}", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (showAdminPanel) Icons.Default.AdminPanelSettings else Icons.Default.VerifiedUser,
                                    contentDescription = "Admin Approval",
                                    tint = if (showAdminPanel) Color(0xFFFFD54F) else Color.White
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A20))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSubmitDialog = true },
                icon = { Icon(Icons.Default.AddComment, contentDescription = null, tint = Color.Black) },
                text = {
                    Text(
                        text = if (isTelugu) "మీ కథను పంచుకోండి" else "Share Your Story",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 13.sp
                    )
                },
                containerColor = Color(0xFFFFD54F),
                contentColor = Color.Black,
                modifier = Modifier.testTag("share_story_fab")
            )
        },
        containerColor = Color(0xFF0C1F10)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("women_farmer_stories_screen_container")
        ) {
            // --- HEADER BANNER CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .testTag("women_stories_header_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A331E)),
                border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTelugu) "మా మహిళా రైతుల స్ఫూర్తి కథలు" else "Inspiring Tales of Women Farmers",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isTelugu)
                                "పొలాల్లో చెమటోడ్చి గ్రామీణ వ్యవస్థకు వెన్నుముకగా నిలిచే ధీర మహిళల జీవిత పాఠాలు & అనుభవాలు."
                            else
                                "Stories of resilience, hard work and courage from women who form the backbone of rural farming.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // --- CATEGORY FILTERS ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (catKey, catLabel) ->
                    val isSelected = selectedCategory == catKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = catKey },
                        label = {
                            Text(
                                text = catLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            val icon = when (catKey) {
                                "మహిళా రైతు" -> Icons.Default.Grass
                                "వ్యవసాయ కార్మికురాలు" -> Icons.Default.Engineering
                                "పశుపోషణ" -> Icons.Default.Pets
                                "కూరగాయల సాగు" -> Icons.Default.Eco
                                "పాడి పరిశ్రమ" -> Icons.Default.WaterDrop
                                "విత్తన/పంట పనులు" -> Icons.Default.Grain
                                "స్వయం ఉపాధి" -> Icons.Default.Lightbulb
                                else -> Icons.Default.AutoAwesome
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isSelected) Color.Black else Color(0xFFFFD54F)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD54F),
                            selectedLabelColor = Color.Black,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = 0.2f),
                            selectedBorderColor = Color(0xFFFFD54F)
                        ),
                        modifier = Modifier.testTag("story_category_chip_$catKey")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // --- ADMIN APPROVAL PANEL (IF OPENED) ---
            if (showAdminPanel) {
                AdminApprovalSection(
                    pendingStories = pendingStories,
                    isTelugu = isTelugu,
                    onApprove = { story ->
                        agriViewModel.approveStory(story.id)
                        Toast.makeText(context, if (isTelugu) "కథ ఆమోదించబడింది మరియు ప్రచురించబడింది!" else "Story approved & published!", Toast.LENGTH_SHORT).show()
                    },
                    onReject = { story ->
                        agriViewModel.deleteStory(story)
                        Toast.makeText(context, if (isTelugu) "కథ తిరస్కరించబడింది" else "Story rejected", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // --- PUBLIC STORIES LIST ---
                if (filteredStories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isTelugu) "ఈ విభాగంలో కథలు ఇంకా అందుబాటులో లేవు." else "No stories available in this category yet.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showSubmitDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                            ) {
                                Text(
                                    text = if (isTelugu) "మొదటి కథను పంచుకోండి" else "Share First Story",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("stories_list_lazy_column")
                    ) {
                        items(filteredStories) { story ->
                            WomanFarmerStoryCard(
                                story = story,
                                isTelugu = isTelugu,
                                onLike = { agriViewModel.incrementStoryLike(story.id) },
                                onShare = { shareStoryIntent(context, story, isTelugu) },
                                onReadFull = { selectedStoryForDetail = story }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- FULL STORY DIALOG ---
    selectedStoryForDetail?.let { story ->
        FullStoryDetailModal(
            story = story,
            isTelugu = isTelugu,
            onDismiss = { selectedStoryForDetail = null },
            onLike = { agriViewModel.incrementStoryLike(story.id) },
            onShare = { shareStoryIntent(context, story, isTelugu) }
        )
    }

    // --- SUBMIT STORY DIALOG ---
    if (showSubmitDialog) {
        SubmitWomanStoryModal(
            isTelugu = isTelugu,
            onDismiss = { showSubmitDialog = false },
            onSubmit = { newStory ->
                agriViewModel.submitWomanFarmerStory(newStory) { success ->
                    if (success) {
                        Toast.makeText(
                            context,
                            if (isTelugu)
                                "మీ కథ విజయవంతంగా పంపబడింది! అడ్మిన్ ఆమోదం పొందిన తర్వాత ప్రచురించబడుతుంది."
                            else
                                "Story submitted successfully! Will be published after Admin Approval.",
                            Toast.LENGTH_LONG
                        ).show()
                        showSubmitDialog = false
                    } else {
                        Toast.makeText(context, "Error submitting story. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
fun WomanFarmerStoryCard(
    story: WomanFarmerStoryEntity,
    isTelugu: Boolean,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onReadFull: () -> Unit
) {
    val shortStoryText = if (isTelugu || story.shortStoryEn.isBlank()) story.shortStoryTe else story.shortStoryEn

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("story_card_${story.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF162B1A)),
        border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Avatar Photo + Name + Village + Category Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo
                if (story.photoUrl.startsWith("http")) {
                    AsyncImage(
                        model = story.photoUrl,
                        contentDescription = story.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = story.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = story.village,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFD54F).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = story.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Main Crop / Work Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.06f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding( horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isTelugu) "ముఖ్యమైన పని/పంట: ${story.cropOrWork}" else "Main Work: ${story.cropOrWork}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF81C784)
                    )
                }
            }

            // Short Inspirational Quote / Story
            Text(
                text = "“$shortStoryText”",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Action Buttons Row (Like, Share, Read Full)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like / Inspired Button
                OutlinedButton(
                    onClick = onLike,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("like_story_button_${story.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTelugu) "స్ఫూర్తి (${story.likeCount})" else "Inspired (${story.likeCount})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Share Button
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("share_story_button_${story.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Read Full Story Button
                Button(
                    onClick = onReadFull,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("read_full_story_button_${story.id}")
                ) {
                    Text(
                        text = if (isTelugu) "పూర్తి కథ చదవండి ➔" else "Read Full Story ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun FullStoryDetailModal(
    story: WomanFarmerStoryEntity,
    isTelugu: Boolean,
    onDismiss: () -> Unit,
    onLike: () -> Unit,
    onShare: () -> Unit
) {
    val fullText = if (isTelugu || story.fullStoryEn.isBlank()) story.fullStoryTe else story.fullStoryEn

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
            ) {
                Text(if (isTelugu) "ముగించు" else "Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onShare,
                border = BorderStroke(1.dp, Color(0xFFFFD54F))
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isTelugu) "షేర్" else "Share", color = Color(0xFFFFD54F))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color(0xFFFFD54F))
                Text(story.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${story.village} • ${story.category}",
                        fontSize = 11.sp,
                        color = Color(0xFFFFD54F),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = fullText,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onLike,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTelugu) "❤️ స్ఫూర్తి పొందితే ఇక్కడ క్లిక్ చేయండి (${story.likeCount})" else "❤️ I am Inspired (${story.likeCount})",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = Color(0xFF132B18),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SubmitWomanStoryModal(
    isTelugu: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (WomanFarmerStoryEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var cropOrWork by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("మహిళా రైతు") }
    var shortStory by remember { mutableStateOf("") }
    var fullStory by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var hasConsent by remember { mutableStateOf(false) }

    val categoriesList = listOf(
        "మహిళా రైతు",
        "వ్యవసాయ కార్మికురాలు",
        "పశుపోషణ",
        "కూరగాయల సాగు",
        "పాడి పరిశ్రమ",
        "విత్తన/పంట పనులు",
        "స్వయం ఉపాధి"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || village.isBlank() || shortStory.isBlank()) return@Button
                    val newStory = WomanFarmerStoryEntity(
                        name = name.trim(),
                        village = village.trim(),
                        cropOrWork = cropOrWork.ifBlank { "సాధారణ వ్యవసాయం" }.trim(),
                        category = category,
                        shortStoryTe = shortStory.trim(),
                        fullStoryTe = fullStory.ifBlank { shortStory }.trim(),
                        photoUrl = photoUrl.ifBlank { "https://images.unsplash.com/photo-1595273670150-bd0c3c392e46?w=600&auto=format&fit=crop&q=60" },
                        likeCount = 1,
                        isFeaturedToday = false,
                        isApproved = false, // Must be approved by admin
                        hasConsent = hasConsent,
                        submittedBy = "Farmer"
                    )
                    onSubmit(newStory)
                },
                enabled = hasConsent && name.isNotBlank() && village.isNotBlank() && shortStory.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                modifier = Modifier.testTag("submit_story_confirm_button")
            ) {
                Text(
                    text = if (isTelugu) "కథ సమర్పించండి" else "Submit Story",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isTelugu) "రద్దు చేయి" else "Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddComment, contentDescription = null, tint = Color(0xFFFFD54F))
                Text(
                    text = if (isTelugu) "మీ కథను మాతో పంచుకోండి" else "Share Your / A Woman's Story",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isTelugu) "మహిళా రైతు పేరు *" else "Woman Farmer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_woman_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD54F), unfocusedBorderColor = Color.White.copy(0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text(if (isTelugu) "గ్రామం & జిల్లా *" else "Village & District *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_village_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD54F), unfocusedBorderColor = Color.White.copy(0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = cropOrWork,
                    onValueChange = { cropOrWork = it },
                    label = { Text(if (isTelugu) "ముఖ్యమైన పంట / పని" else "Main Crop / Work") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_crop_work"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD54F), unfocusedBorderColor = Color.White.copy(0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                // Category selection dropdown chips
                Text(
                    text = if (isTelugu) "విభాగం ఎంచుకోండి:" else "Select Category:",
                    fontSize = 11.sp,
                    color = Color.White.copy(0.7f)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categoriesList) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD54F),
                                selectedLabelColor = Color.Black,
                                containerColor = Color.White.copy(0.1f),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = shortStory,
                    onValueChange = { shortStory = it },
                    label = { Text(if (isTelugu) "స్ఫూర్తిదాయక సారాంశం (2-3 వాక్యాలు) *" else "Inspirational Short Summary *") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_short_story"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD54F), unfocusedBorderColor = Color.White.copy(0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = fullStory,
                    onValueChange = { fullStory = it },
                    label = { Text(if (isTelugu) "పూర్తి వివరమైన కథ" else "Full Detailed Story") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("input_full_story"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD54F), unfocusedBorderColor = Color.White.copy(0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text(if (isTelugu) "ఫోటో లింక్ / URL (ఐచ్ఛికం)" else "Photo URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_photo_url"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD54F), unfocusedBorderColor = Color.White.copy(0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                // PRIVACY MANDATORY CONSENT CHECKBOX
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasConsent,
                        onCheckedChange = { hasConsent = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFD54F), checkmarkColor = Color.Black),
                        modifier = Modifier.testTag("consent_checkbox")
                    )
                    Text(
                        text = if (isTelugu)
                            "సదరు మహిళా రైతు నుంచి ఫోటో మరియు వివరాలను యాప్‌లో ప్రచురించడానికి పూర్తి అనుమతి ఉంది (గోప్యతా నిబంధనలు)."
                        else
                            "Consent has been explicitly obtained from the woman farmer to publish photo and details (Privacy Rule).",
                        fontSize = 10.sp,
                        color = Color.White,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        containerColor = Color(0xFF132B18),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AdminApprovalSection(
    pendingStories: List<WomanFarmerStoryEntity>,
    isTelugu: Boolean,
    onApprove: (WomanFarmerStoryEntity) -> Unit,
    onReject: (WomanFarmerStoryEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("admin_approval_section_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isTelugu) "పరిశీలనలో ఉన్న కథలు (${pendingStories.size})" else "Pending Stories for Approval (${pendingStories.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEF5350).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "Admin Mode",
                    fontSize = 10.sp,
                    color = Color(0xFFEF5350),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (pendingStories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isTelugu) "ప్రస్తుతం అనుమతి కోసం వేచి ఉన్న కథలు ఏవీ లేవు." else "No pending stories waiting for approval.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pendingStories) { story ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pending_story_card_${story.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF233B26)),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(story.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF81C784).copy(0.2f)) {
                                    Text(
                                        text = if (story.hasConsent) "Consent Granted ✓" else "No Consent ✕",
                                        fontSize = 10.sp,
                                        color = if (story.hasConsent) Color(0xFF81C784) else Color(0xFFEF5350),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${story.village} • ${story.category} (${story.cropOrWork})",
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F)
                            )

                            Text(
                                text = story.shortStoryTe,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onApprove(story) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.weight(1f).testTag("approve_button_${story.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isTelugu) "ఆమోదించు" else "Approve", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onReject(story) },
                                    border = BorderStroke(1.dp, Color(0xFFEF5350)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                                    modifier = Modifier.weight(1f).testTag("reject_button_${story.id}")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isTelugu) "తిరస్కరించు" else "Reject", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareStoryIntent(context: android.content.Context, story: WomanFarmerStoryEntity, isTelugu: Boolean) {
    val shareText = if (isTelugu) {
        "🌸 KrishiMithra - మా మహిళా రైతుల స్ఫూర్తి కథ:\n\n" +
                "👤 పేరు: ${story.name}\n" +
                "📍 గ్రామం: ${story.village}\n" +
                "🌾 పంట/పని: ${story.cropOrWork}\n\n" +
                "“${story.shortStoryTe}”\n\n" +
                "మహిళా రైతుల నిబద్ధతకు మా ప్రణామాలు! 🙏\n" +
                "మరిన్ని కథల కోసం KrishiMithra యాప్ డౌన్‌లోడ్ చేసుకోండి."
    } else {
        "🌸 KrishiMithra - Women Farmer Inspiration Story:\n\n" +
                "👤 Name: ${story.name}\n" +
                "📍 Village: ${story.village}\n" +
                "🌾 Work: ${story.cropOrWork}\n\n" +
                "“${story.shortStoryEn.ifBlank { story.shortStoryTe }}”\n\n" +
                "Saluting the spirit of hardworking women farmers! 🙏\n" +
                "Download KrishiMithra for more inspiring stories."
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Women Farmer Story: ${story.name}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Story via"))
}
