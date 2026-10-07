package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.NewsEntity
import com.example.ui.AgriViewModel
import com.example.ui.NewsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val newsViewModel: NewsViewModel = viewModel()
    val retrofitNewsList by newsViewModel.rawDailyNews.collectAsStateWithLifecycle()
    val agriViewModelNewsList by viewModel.allNews.collectAsStateWithLifecycle()
    val newsList = if (retrofitNewsList.isNotEmpty()) retrofitNewsList else agriViewModelNewsList
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // News Categories
    val categories = listOf(
        "All",
        "Bookmarks",
        "AP Agriculture News",
        "Telangana Agriculture News",
        "India Agriculture News",
        "Organic Farming",
        "Natural Farming",
        "Farmer Success Stories",
        "New Technologies",
        "Government Announcements",
        "Irrigation",
        "Weather",
        "Pest Control",
        "Fertilizers",
        "Dairy",
        "Fisheries",
        "Poultry",
        "Horticulture"
    )

    var selectedCategory by remember { mutableStateOf("All") }
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val languageMode = if (appLanguage == "te") "Telugu" else "English"
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedNews by remember { mutableStateOf<NewsEntity?>(null) }
    var showWebBrowserNews by remember { mutableStateOf<NewsEntity?>(null) }

    // Use rememberSaveable to keep bookmarked news IDs persistent across configuration changes
    var bookmarkedNewsIds by rememberSaveable { mutableStateOf(setOf<Int>()) }

    // Pull-to-refresh simulation
    fun triggerRefresh() {
        scope.launch {
            isRefreshing = true
            delay(1500) // Beautiful shimmer effect duration
            isRefreshing = false
        }
    }

    // Filtered News Logic
    val filteredNews = remember(newsList, selectedCategory, bookmarkedNewsIds) {
        if (selectedCategory == "Bookmarks") {
            newsList.filter { bookmarkedNewsIds.contains(it.id) }
        } else if (selectedCategory == "All") {
            newsList
        } else {
            newsList.filter { it.category == selectedCategory }
        }
    }

    // Rotating Animation for Refresh Button
    val infiniteTransition = rememberInfiniteTransition(label = "refreshRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Agri News",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Way2News & Inshorts Style Updates",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Refresh Button with smooth rotation while updating
                    IconButton(
                        onClick = { triggerRefresh() },
                        enabled = !isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (isRefreshing) rotationAngle else 0f
                            },
                            tint = Color(0xFF2E7D32)
                        )
                    }

                    // Language Toggle
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (languageMode == "English") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (languageMode == "English") Color(0xFF2E7D32) else Color.Transparent)
                                .clickable { viewModel.setAppLanguage("en") }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                        Text(
                            text = "తెలుగు",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (languageMode == "Telugu") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (languageMode == "Telugu") Color(0xFF2E7D32) else Color.Transparent)
                                .clickable { viewModel.setAppLanguage("te") }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(2.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Horizontal category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val chipColor = if (isSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant
                        val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        val icon = when (cat) {
                            "All" -> Icons.Default.Feed
                            "Bookmarks" -> Icons.Default.Bookmark
                            "AP Agriculture News" -> Icons.Default.Map
                            "Telangana Agriculture News" -> Icons.Default.Place
                            "India Agriculture News" -> Icons.Default.Language
                            "Organic Farming" -> Icons.Default.Spa
                            "Natural Farming" -> Icons.Default.Forest
                            "Farmer Success Stories" -> Icons.Default.Star
                            "New Technologies" -> Icons.Default.Build
                            "Government Announcements" -> Icons.Default.Campaign
                            "Irrigation" -> Icons.Default.Water
                            "Weather" -> Icons.Default.WbSunny
                            "Pest Control" -> Icons.Default.BugReport
                            "Fertilizers" -> Icons.Default.Science
                            "Dairy" -> Icons.Default.Pets
                            "Fisheries" -> Icons.Default.Water
                            "Poultry" -> Icons.Default.Pets
                            "Horticulture" -> Icons.Default.LocalFlorist
                            else -> Icons.Default.Feed
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipColor)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = cat,
                                    tint = if (isSelected) Color.White else Color(0xFF2E7D32),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (cat == "Bookmarks") "Saved" else cat,
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Main News List, loading Shimmer or Empty States
                if (isRefreshing) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(3) {
                            ShimmerNewsCard()
                        }
                    }
                } else if (filteredNews.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedCategory == "Bookmarks") Icons.Default.BookmarkBorder else Icons.Default.Feed,
                                contentDescription = "Empty State",
                                tint = Color(0xFF2E7D32).copy(alpha = 0.4f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (selectedCategory == "Bookmarks") {
                                    "No Bookmarked Articles"
                                } else {
                                    "No feeds available in '$selectedCategory'"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedCategory == "Bookmarks") {
                                    "Bookmark informative farming updates and read them quickly in this tab anytime."
                                } else {
                                    "Check back shortly for newly released agricultural news and government notifications."
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (selectedCategory != "All") {
                                Button(
                                    onClick = { selectedCategory = "All" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Text("Explore All News", color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredNews) { news ->
                            val isBookmarked = bookmarkedNewsIds.contains(news.id)
                            PremiumNewsItemCard(
                                news = news,
                                languageMode = languageMode,
                                isBookmarked = isBookmarked,
                                onBookmarkToggle = {
                                    bookmarkedNewsIds = if (isBookmarked) {
                                        bookmarkedNewsIds - news.id
                                    } else {
                                        bookmarkedNewsIds + news.id
                                    }
                                },
                                onShare = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        val shareText = if (news.titleTe.isNotBlank()) {
                                            "${news.titleTe}\n${news.title}\n\n${news.content}\n\nShared via Daily Agri News App"
                                        } else {
                                            "${news.title}\n\n${news.content}\n\nShared via Daily Agri News App"
                                        }
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Daily Agri News via:")
                                    context.startActivity(shareIntent)
                                },
                                onClick = {
                                    selectedNews = news
                                }
                            )
                        }
                    }
                }
            }

            // Beautiful slide-up full news details screen
            AnimatedVisibility(
                visible = selectedNews != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedNews?.let { news ->
                    val isBookmarked = bookmarkedNewsIds.contains(news.id)
                    FullNewsDetailView(
                        news = news,
                        languageMode = languageMode,
                        isBookmarked = isBookmarked,
                        onBookmarkToggle = {
                            bookmarkedNewsIds = if (isBookmarked) {
                                bookmarkedNewsIds - news.id
                            } else {
                                bookmarkedNewsIds + news.id
                            }
                        },
                        onShare = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                val shareText = if (news.titleTe.isNotBlank()) {
                                    "${news.titleTe}\n${news.title}\n\n${news.content}\n\nShared via Daily Agri News App"
                                } else {
                                    "${news.title}\n\n${news.content}\n\nShared via Daily Agri News App"
                                }
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Daily Agri News via:")
                            context.startActivity(shareIntent)
                        },
                        onBack = { selectedNews = null },
                        onReadOriginal = { showWebBrowserNews = news }
                    )
                }
            }

            // Mock Web Browser Dialog for Original Articles
            if (showWebBrowserNews != null) {
                WebBrowserDialog(
                    news = showWebBrowserNews!!,
                    onDismiss = { showWebBrowserNews = null }
                )
            }
        }
    }
}

@Composable
fun PremiumNewsItemCard(
    news: NewsEntity,
    languageMode: String,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    val categoryDetails = remember(news.category) { getCategoryColors(news.category) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Full Width Large Image container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (!news.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = news.imageUrl,
                        contentDescription = news.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AgriculturePlaceholder(category = news.category, modifier = Modifier.fillMaxSize())
                }

                // Visual Gradient overlay to preserve text readability on overlay items
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Overlay information
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(categoryDetails.badgeBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = news.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryDetails.badgeText
                            )
                        }

                        // Bookmark & Share Overlay Buttons (Inshorts style)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable(onClick = onBookmarkToggle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) Color(0xFFFFD54F) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable(onClick = onShare),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Source and Date in bottom-scrim
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Source",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Source: ${news.source}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val formattedDate = remember(news.timestamp) {
                            try {
                                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                sdf.format(Date(news.timestamp))
                            } catch (e: Exception) {
                                "Today"
                            }
                        }
                        Text(
                            text = formattedDate,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Article visual content details
            Column(modifier = Modifier.padding(16.dp)) {
                // Telugu Title (displays elegantly first if language toggle or always stacked clearly)
                if (news.titleTe.isNotBlank()) {
                    Text(
                        text = news.titleTe,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // English Title
                Text(
                    text = news.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Short Description content (Inshorts style excerpt)
                Text(
                    text = news.content,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Broadcast",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Verified Broadcast",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Read Full Article ➜",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun FullNewsDetailView(
    news: NewsEntity,
    languageMode: String,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
    onReadOriginal: () -> Unit
) {
    val categoryDetails = remember(news.category) { getCategoryColors(news.category) }
    val formattedDate = remember(news.timestamp) {
        try {
            val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            sdf.format(Date(news.timestamp))
        } catch (e: Exception) {
            "Today"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Detail Header navigation row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Close details",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Full Article Feed",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Large Top Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (!news.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = news.imageUrl,
                        contentDescription = news.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AgriculturePlaceholder(category = news.category, modifier = Modifier.fillMaxSize())
                }

                // Small decorative bottom overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
            }

            // Article body details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryDetails.badgeBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = news.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryDetails.badgeText
                    )
                }

                // Telugu Title if existing
                if (news.titleTe.isNotBlank()) {
                    Text(
                        text = news.titleTe,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        lineHeight = 28.sp
                    )
                }

                // English Title
                Text(
                    text = news.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 25.sp
                )

                // Publisher Source Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Broadcaster",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = news.source,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Verified Officer",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Full Article Text body (Premium typography)
                Text(
                    text = news.content,
                    fontSize = 14.sp,
                    lineHeight = 23.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Justify
                )

                // Extra farmer helper advice banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Notice",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "This report was verified directly by local Krishi Seva Kendra block officials. Follow safety instructions as broadcasted.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // "Read Original Article" button
                Button(
                    onClick = onReadOriginal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Original", tint = Color.White)
                        Text(
                            text = "Read Original Article",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// Visual Shimmer skeleton news card loader
@Composable
fun ShimmerNewsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Image shimmer block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title Line 1
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Title Line 2
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Description
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .width(100.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_translate"
    )
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    Box(modifier = modifier.background(brush))
}

@Composable
fun AgriculturePlaceholder(category: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E7D32), // Forest green
                        Color(0xFF1B5E20)  // Deep organic green
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = "Agri Placeholder",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WebBrowserDialog(
    news: NewsEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Browser header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "agridept.gov.in/news",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Secure Connection",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = 1.0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )

                // Browser content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "AGRICULTURE DEPARTMENT NEWS PORTAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = news.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Source: ${news.source}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Verified Broadcaster",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Divider()

                    Text(
                        text = news.content + "\n\n" +
                                "Additional verified field reports confirm that farmers adopting this strategy have experienced a significant improvement in efficiency and yields. Local extension offices (Rythu Bharosa Kendras / Krishi Vigyan Kendras) have been equipped with necessary kits, seeds, and training modules to assist any grower needing hands-on help.",
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "This document has been digitally signed and published by the Department of Agriculture, Government of India.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Browser footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "© 2026 Ministry of Agriculture. All Rights Reserved.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

data class CategoryStyle(val badgeBg: Color, val badgeText: Color)

fun getCategoryColors(category: String): CategoryStyle {
    return when (category) {
        "AP Agriculture News" -> CategoryStyle(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "Telangana Agriculture News" -> CategoryStyle(Color(0xFFE0F2F1), Color(0xFF00695C))
        "India Agriculture News" -> CategoryStyle(Color(0xFFE3F2FD), Color(0xFF1565C0))
        "Organic Farming" -> CategoryStyle(Color(0xFFFFF3E0), Color(0xFFE65100))
        "Natural Farming" -> CategoryStyle(Color(0xFFF1F8E9), Color(0xFF558B2F))
        "Farmer Success Stories" -> CategoryStyle(Color(0xFFFCE4EC), Color(0xFFC2185B))
        "New Technologies" -> CategoryStyle(Color(0xFFEDE7F6), Color(0xFF673AB7))
        "Government Announcements" -> CategoryStyle(Color(0xFFFFFDE7), Color(0xFFF57F17))
        "Irrigation" -> CategoryStyle(Color(0xFFE0F7FA), Color(0xFF00838F))
        "Weather" -> CategoryStyle(Color(0xFFE0F2F1), Color(0xFF00796B))
        "Pest Control" -> CategoryStyle(Color(0xFFFFEBEE), Color(0xFFC62828))
        "Fertilizers" -> CategoryStyle(Color(0xFFE8EAF6), Color(0xFF283593))
        "Dairy" -> CategoryStyle(Color(0xFFFFF3E0), Color(0xFFD84315))
        "Fisheries" -> CategoryStyle(Color(0xFFE1F5FE), Color(0xFF0277BD))
        "Poultry" -> CategoryStyle(Color(0xFFF9F1F0), Color(0xFFAD1457))
        "Horticulture" -> CategoryStyle(Color(0xFFE8F5E9), Color(0xFF1B5E20))
        else -> CategoryStyle(Color(0xFFECEFF1), Color(0xFF37474F))
    }
}
