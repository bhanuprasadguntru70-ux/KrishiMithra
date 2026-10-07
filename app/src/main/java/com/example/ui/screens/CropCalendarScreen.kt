package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CropCalendarEntity
import com.example.data.model.CropStageEntity
import com.example.data.model.CropTaskReminderEntity
import com.example.ui.AgriViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class AppLanguage {
    ENGLISH, TELUGU, DUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropCalendarScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val calendars by viewModel.allCropCalendars.collectAsStateWithLifecycle()
    val selectedCalendarId by viewModel.selectedCalendarId.collectAsStateWithLifecycle()
    val stages by viewModel.selectedCalendarStages.collectAsStateWithLifecycle()
    val tasks by viewModel.selectedCalendarTasks.collectAsStateWithLifecycle()

    var languageMode by remember { mutableStateOf(AppLanguage.DUAL) }
    var showAddCalendarDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskCategoryFilter by remember { mutableStateOf("All") }

    // Automatically select first calendar if none selected
    LaunchedEffect(calendars) {
        if (selectedCalendarId == null && calendars.isNotEmpty()) {
            viewModel.selectCropCalendar(calendars.first().id)
        }
    }

    val currentCalendar = remember(calendars, selectedCalendarId) {
        calendars.find { it.id == selectedCalendarId } ?: calendars.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (languageMode) {
                                AppLanguage.ENGLISH -> "Crop Lifecycle Calendar"
                                AppLanguage.TELUGU -> "పంట క్యాలెండర్"
                                AppLanguage.DUAL -> "Crop Calendar (పంట క్యాలెండర్)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Smart Timelines & Task Reminders",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Language Switcher Toggle Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (languageMode == AppLanguage.ENGLISH) Color(0xFF2E7D32) else Color.Transparent)
                                .clickable { languageMode = AppLanguage.ENGLISH }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("EN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (languageMode == AppLanguage.TELUGU) Color(0xFF2E7D32) else Color.Transparent)
                                .clickable { languageMode = AppLanguage.TELUGU }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("తెలుగు", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (languageMode == AppLanguage.DUAL) Color(0xFF2E7D32) else Color.Transparent)
                                .clickable { languageMode = AppLanguage.DUAL }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Both", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F3D1E),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (currentCalendar != null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    icon = { Icon(Icons.Default.AddAlarm, contentDescription = "Add Task") },
                    text = { Text(if (languageMode == AppLanguage.TELUGU) "టాస్క్ రిమైండర్" else "Add Task") },
                    containerColor = Color(0xFFFFD54F),
                    contentColor = Color(0xFF1B5E20),
                    modifier = Modifier.testTag("add_task_fab")
                )
            }
        },
        containerColor = Color(0xFF0A2212)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("crop_calendar_lazy_column"),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Farm & Crop Selector Bar
            item {
                FarmCropSelectorHeader(
                    calendars = calendars,
                    selectedId = currentCalendar?.id,
                    languageMode = languageMode,
                    onSelect = { viewModel.selectCropCalendar(it) },
                    onAddNew = { showAddCalendarDialog = true }
                )
            }

            if (currentCalendar != null) {
                // Section 1: Active Crop Progress Card
                item {
                    CropSummaryProgressCard(
                        calendar = currentCalendar,
                        languageMode = languageMode,
                        onDelete = { viewModel.deleteCropCalendar(currentCalendar) }
                    )
                }

                // Section 2: Visual Lifecycle Stages Stepper / Timeline
                item {
                    LifecycleStagesTimelineSection(
                        stages = stages,
                        plantingDate = currentCalendar.plantingDate,
                        totalDuration = currentCalendar.totalDurationDays,
                        languageMode = languageMode
                    )
                }

                // Section 3: Task Reminders & Agricultural Care Schedule
                item {
                    TaskRemindersSection(
                        tasks = tasks,
                        selectedFilter = selectedTaskCategoryFilter,
                        languageMode = languageMode,
                        onFilterChange = { selectedTaskCategoryFilter = it },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onToggleReminder = { viewModel.toggleTaskReminder(it) },
                        onAddTask = { showAddTaskDialog = true }
                    )
                }
            } else {
                // Empty State when no farms exist
                item {
                    EmptyCropCalendarView(
                        languageMode = languageMode,
                        onAddClick = { showAddCalendarDialog = true }
                    )
                }
            }
        }
    }

    // Dialog: Add New Crop Calendar
    if (showAddCalendarDialog) {
        AddCropCalendarDialog(
            languageMode = languageMode,
            onDismiss = { showAddCalendarDialog = false },
            onConfirm = { farmName, cropName, cropNameTe, variety, farmArea, soilType, plantingDate, notes ->
                viewModel.createCropCalendar(
                    farmName = farmName,
                    cropName = cropName,
                    cropNameTe = cropNameTe,
                    variety = variety,
                    farmArea = farmArea,
                    soilType = soilType,
                    plantingDate = plantingDate,
                    notes = notes
                )
                showAddCalendarDialog = false
            }
        )
    }

    // Dialog: Add Custom Task Reminder
    if (showAddTaskDialog && currentCalendar != null) {
        AddTaskReminderDialog(
            calendarId = currentCalendar.id,
            languageMode = languageMode,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, titleTe, category, categoryTe, dueDate, instructions, priority ->
                viewModel.addCustomTask(
                    calendarId = currentCalendar.id,
                    taskTitle = title,
                    taskTitleTe = titleTe,
                    category = category,
                    categoryTe = categoryTe,
                    dueDate = dueDate,
                    instructions = instructions,
                    instructionsTe = "",
                    priority = priority
                )
                showAddTaskDialog = false
            }
        )
    }
}

// --- SELECTOR HEADER ---
@Composable
fun FarmCropSelectorHeader(
    calendars: List<CropCalendarEntity>,
    selectedId: Int?,
    languageMode: AppLanguage,
    onSelect: (Int) -> Unit,
    onAddNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F3D1E))
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (languageMode) {
                    AppLanguage.ENGLISH -> "MY CROP CALENDARS"
                    AppLanguage.TELUGU -> "నా పంట క్యాలెండర్లు"
                    AppLanguage.DUAL -> "MY CROPS (నా పంటలు)"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F),
                letterSpacing = 1.sp
            )

            TextButton(onClick = onAddNew) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = Color(0xFF81C784), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (languageMode) {
                        AppLanguage.ENGLISH -> "+ Add Crop"
                        AppLanguage.TELUGU -> "+ కొత్త పంట"
                        AppLanguage.DUAL -> "+ Add Crop (కొత్త పంట)"
                    },
                    color = Color(0xFF81C784),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(calendars) { cal ->
                val isSelected = cal.id == selectedId
                val bgGradient = if (isSelected) {
                    Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)))
                } else {
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgGradient)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) Color(0xFFFFD54F) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelect(cal.id) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCropIcon(cal.cropName),
                                contentDescription = cal.cropName,
                                tint = if (isSelected) Color(0xFF1B5E20) else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            val cropDisplayName = when (languageMode) {
                                AppLanguage.ENGLISH -> cal.cropName
                                AppLanguage.TELUGU -> cal.cropNameTe.ifBlank { cal.cropName }
                                AppLanguage.DUAL -> "${cal.cropName} (${cal.cropNameTe.ifBlank { cal.cropName }})"
                            }
                            Text(
                                text = cropDisplayName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = cal.farmName,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- CROP SUMMARY PROGRESS CARD ---
@Composable
fun CropSummaryProgressCard(
    calendar: CropCalendarEntity,
    languageMode: AppLanguage,
    onDelete: () -> Unit
) {
    val daysElapsed = remember(calendar.plantingDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val plantDate = sdf.parse(calendar.plantingDate) ?: Date()
            val diffMs = Date().time - plantDate.time
            (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    val progressFraction = (daysElapsed.toFloat() / calendar.totalDurationDays.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF144D2A)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD54F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCropIcon(calendar.cropName),
                            contentDescription = calendar.cropName,
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = when (languageMode) {
                                AppLanguage.ENGLISH -> calendar.cropName
                                AppLanguage.TELUGU -> calendar.cropNameTe.ifBlank { calendar.cropName }
                                AppLanguage.DUAL -> "${calendar.cropName} • ${calendar.cropNameTe}"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${calendar.farmName} • ${calendar.variety.ifBlank { calendar.farmArea }}",
                            fontSize = 12.sp,
                            color = Color(0xFF81C784)
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            // Progress Days Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (languageMode) {
                            AppLanguage.ENGLISH -> "DAY $daysElapsed OF ${calendar.totalDurationDays}"
                            AppLanguage.TELUGU -> "మొత్తం ${calendar.totalDurationDays} రోజుల్లో $daysElapsed-వ రోజు"
                            AppLanguage.DUAL -> "Day $daysElapsed / ${calendar.totalDurationDays} (రోజులు)"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                    Text(
                        text = "Planting: ${calendar.plantingDate} | Harvest: ${calendar.expectedHarvestDate}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "${(progressFraction * 100).toInt()}%",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // Custom Linear Progress Bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFFFFD54F),
                trackColor = Color.White.copy(alpha = 0.15f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(icon = Icons.Default.Agriculture, label = "Soil: ${calendar.soilType}")
                InfoChip(icon = Icons.Default.SquareFoot, label = "Area: ${calendar.farmArea}")
            }
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(14.dp))
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}

// --- LIFECYCLE STAGES TIMELINE SECTION ---
@Composable
fun LifecycleStagesTimelineSection(
    stages: List<CropStageEntity>,
    plantingDate: String,
    totalDuration: Int,
    languageMode: AppLanguage
) {
    var expandedStageId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Timeline, contentDescription = null, tint = Color(0xFFFFD54F))
            Text(
                text = when (languageMode) {
                    AppLanguage.ENGLISH -> "Crop Growth Lifecycle Stages"
                    AppLanguage.TELUGU -> "పంట పెరుగుదల దశల టైమ్‌లైన్"
                    AppLanguage.DUAL -> "Lifecycle Stages (ఎదుగుదల దశలు)"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        stages.forEachIndexed { index, stage ->
            val isExpanded = expandedStageId == stage.id || index == 0
            val isLast = index == stages.size - 1

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vertical Timeline Node Bar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (stage.status == "Active") Color(0xFFFFD54F) else Color(0xFF1B5E20))
                            .border(2.dp, Color(0xFF81C784), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getStageIcon(stage.iconType),
                            contentDescription = stage.stageName,
                            tint = if (stage.status == "Active") Color(0xFF1B5E20) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(70.dp)
                                .background(Color(0xFF81C784).copy(alpha = 0.4f))
                        )
                    }
                }

                // Stage Card Content
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { expandedStageId = if (isExpanded) null else stage.id },
                    colors = CardDefaults.cardColors(
                        containerColor = if (stage.status == "Active") Color(0xFF1E522F) else Color(0xFF123B1E)
                    ),
                    border = BorderStroke(
                        width = if (stage.status == "Active") 1.5.dp else 0.5.dp,
                        color = if (stage.status == "Active") Color(0xFFFFD54F) else Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (languageMode) {
                                    AppLanguage.ENGLISH -> stage.stageName
                                    AppLanguage.TELUGU -> stage.stageNameTe.ifBlank { stage.stageName }
                                    AppLanguage.DUAL -> "${stage.stageName} (${stage.stageNameTe})"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )

                            if (stage.status == "Active") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFD54F))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (languageMode == AppLanguage.TELUGU) "ప్రస్తుతం నడుస్తోంది" else "ACTIVE",
                                        color = Color(0xFF1B5E20),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Days ${stage.startDay} - ${stage.endDay} (${stage.startDate} to ${stage.endDate})",
                            fontSize = 11.sp,
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Medium
                        )

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Text(
                                    text = when (languageMode) {
                                        AppLanguage.ENGLISH -> stage.description
                                        AppLanguage.TELUGU -> stage.descriptionTe.ifBlank { stage.description }
                                        AppLanguage.DUAL -> "${stage.description}\n\nతెలుగు: ${stage.descriptionTe}"
                                    },
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TASK REMINDERS SECTION ---
@Composable
fun TaskRemindersSection(
    tasks: List<CropTaskReminderEntity>,
    selectedFilter: String,
    languageMode: AppLanguage,
    onFilterChange: (String) -> Unit,
    onToggleTask: (CropTaskReminderEntity) -> Unit,
    onToggleReminder: (CropTaskReminderEntity) -> Unit,
    onAddTask: () -> Unit
) {
    val filters = listOf("All", "Irrigation", "Fertilization", "Pest Control", "Planting", "Harvesting", "Pending")

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "Irrigation" -> tasks.filter { it.category.contains("Irrigation", true) }
            "Fertilization" -> tasks.filter { it.category.contains("Fertilization", true) }
            "Pest Control" -> tasks.filter { it.category.contains("Pest", true) }
            "Planting" -> tasks.filter { it.category.contains("Planting", true) }
            "Harvesting" -> tasks.filter { it.category.contains("Harvest", true) }
            "Pending" -> tasks.filter { !it.isCompleted }
            else -> tasks
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFFD54F))
                Text(
                    text = when (languageMode) {
                        AppLanguage.ENGLISH -> "Agricultural Task Reminders"
                        AppLanguage.TELUGU -> "వ్యవసాయ పనుల ముందస్తు సమాచారం"
                        AppLanguage.DUAL -> "Task Reminders (పనుల రిమైండర్లు)"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(onClick = onAddTask) {
                Text(
                    text = if (languageMode == AppLanguage.TELUGU) "+ కొత్తది" else "+ Add",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { f ->
                val isSel = f == selectedFilter
                FilterChip(
                    selected = isSel,
                    onClick = { onFilterChange(f) },
                    label = {
                        Text(
                            text = getFilterLabel(f, languageMode),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFD54F),
                        selectedLabelColor = Color(0xFF1B5E20),
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White
                    )
                )
            }
        }

        // Task Items List
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (languageMode == AppLanguage.TELUGU) "ఈ కేటగిరీలో పనులు ఏమీ లేవు." else "No task reminders in this category.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        } else {
            filteredTasks.forEach { task ->
                TaskReminderCard(
                    task = task,
                    languageMode = languageMode,
                    onToggleTask = { onToggleTask(task) },
                    onToggleReminder = { onToggleReminder(task) }
                )
            }
        }
    }
}

@Composable
fun TaskReminderCard(
    task: CropTaskReminderEntity,
    languageMode: AppLanguage,
    onToggleTask: () -> Unit,
    onToggleReminder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF0F2E17) else Color(0xFF144522)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (task.isCompleted) Color.Gray.copy(alpha = 0.3f) else Color(0xFF81C784).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleTask() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFFFD54F),
                            checkmarkColor = Color(0xFF1B5E20),
                            uncheckedColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("task_checkbox_${task.id}")
                    )

                    Column {
                        val taskTitleText = when (languageMode) {
                            AppLanguage.ENGLISH -> task.taskTitle
                            AppLanguage.TELUGU -> task.taskTitleTe.ifBlank { task.taskTitle }
                            AppLanguage.DUAL -> "${task.taskTitle}\n${task.taskTitleTe}"
                        }

                        Text(
                            text = taskTitleText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (task.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                            Text(
                                text = "Due: ${task.dueDate} (Day ${task.dayOffset})",
                                fontSize = 11.sp,
                                color = Color(0xFF81C784)
                            )
                        }
                    }
                }

                // Reminder Switch Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (task.isReminderEnabled) "ON" else "OFF",
                        fontSize = 10.sp,
                        color = if (task.isReminderEnabled) Color(0xFFFFD54F) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = task.isReminderEnabled,
                        onCheckedChange = { onToggleReminder() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1B5E20),
                            checkedTrackColor = Color(0xFFFFD54F)
                        ),
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("reminder_switch_${task.id}")
                    )
                }
            }

            if (task.instructions.isNotBlank() || task.instructionsTe.isNotBlank()) {
                val instText = when (languageMode) {
                    AppLanguage.ENGLISH -> task.instructions
                    AppLanguage.TELUGU -> task.instructionsTe.ifBlank { task.instructions }
                    AppLanguage.DUAL -> "${task.instructions}\n(తెలుగు: ${task.instructionsTe})"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "💡 Advice: $instText",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)

// Helper: Filter Label Map
fun getFilterLabel(filterKey: String, languageMode: AppLanguage): String {
    return when (filterKey) {
        "All" -> if (languageMode == AppLanguage.TELUGU) "అన్నీ" else "All Tasks"
        "Irrigation" -> if (languageMode == AppLanguage.TELUGU) "నీటి పారుదల" else "Irrigation"
        "Fertilization" -> if (languageMode == AppLanguage.TELUGU) "ఎరువులు" else "Fertilization"
        "Pest Control" -> if (languageMode == AppLanguage.TELUGU) "పురుగుల నివారణ" else "Pest Control"
        "Planting" -> if (languageMode == AppLanguage.TELUGU) "విత్తనాలు నాటడం" else "Planting"
        "Harvesting" -> if (languageMode == AppLanguage.TELUGU) "పంట కోత" else "Harvesting"
        "Pending" -> if (languageMode == AppLanguage.TELUGU) "పూర్తి కానివి" else "Pending"
        else -> filterKey
    }
}

// --- DIALOG: ADD CROP CALENDAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropCalendarDialog(
    languageMode: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (farmName: String, cropName: String, cropNameTe: String, variety: String, farmArea: String, soilType: String, plantingDate: String, notes: String) -> Unit
) {
    var farmName by remember { mutableStateOf("Sri Krishna Farm (నా పొలం)") }
    var selectedCrop by remember { mutableStateOf("Paddy (Rice)") }
    var selectedCropTe by remember { mutableStateOf("వరి") }
    var variety by remember { mutableStateOf("BPT 5204") }
    var farmArea by remember { mutableStateOf("2.5 Acres") }
    var soilType by remember { mutableStateOf("Black Cotton Soil") }
    var plantingDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var notes by remember { mutableStateOf("") }

    val popularCrops = listOf(
        Pair("Paddy (Rice)", "వరి"),
        Pair("Chilli", "మిరప"),
        Pair("Cotton", "పత్తి"),
        Pair("Groundnut", "వేరుశెనగ"),
        Pair("Maize", "మొక్కజొన్న"),
        Pair("Tomato", "టమోటా"),
        Pair("Sugarcane", "చెరకు")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (languageMode) {
                    AppLanguage.ENGLISH -> "Add Farm & Crop Calendar"
                    AppLanguage.TELUGU -> "కొత్త పొలం & పంట క్యాలెండర్ జత చేయండి"
                    AppLanguage.DUAL -> "Add Crop Calendar (పంట క్యాలెండర్)"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = farmName,
                    onValueChange = { farmName = it },
                    label = { Text("Farm Name / పొలం పేరు") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_farm_name_input")
                )

                Text("Select Crop / పంటను ఎంచుకోండి:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(popularCrops) { cropPair ->
                        val isSelected = selectedCrop == cropPair.first
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCrop = cropPair.first
                                selectedCropTe = cropPair.second
                            },
                            label = { Text("${cropPair.first} (${cropPair.second})", fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Variety / వంగడం (e.g., BPT 5204, Guntur Sannam)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = farmArea,
                        onValueChange = { farmArea = it },
                        label = { Text("Area / వైశాల్యం") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = soilType,
                        onValueChange = { soilType = it },
                        label = { Text("Soil / నేల రకం") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = plantingDate,
                    onValueChange = { plantingDate = it },
                    label = { Text("Planting Date / నాటిన తేదీ (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / వివరాలు") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(farmName, selectedCrop, selectedCropTe, variety, farmArea, soilType, plantingDate, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.testTag("dialog_confirm_crop_calendar")
            ) {
                Text("Generate Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// --- DIALOG: ADD TASK REMINDER ---
@Composable
fun AddTaskReminderDialog(
    calendarId: Int,
    languageMode: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (title: String, titleTe: String, category: String, categoryTe: String, dueDate: String, instructions: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("Irrigation & Fertilizer Spray") }
    var titleTe by remember { mutableStateOf("నీటి పారుదల & ఎరువుల పిచికారీ") }
    var category by remember { mutableStateOf("Irrigation") }
    var dueDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var instructions by remember { mutableStateOf("Apply 10kg Urea per acre during morning hours.") }

    val categories = listOf("Irrigation", "Fertilization", "Pest Control", "Weeding", "Harvesting")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Task Reminder / పని ముందస్తు సమాచారం") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (English)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = titleTe,
                    onValueChange = { titleTe = it },
                    label = { Text("Task Title (తెలుగు)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions / సూచనలు") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, titleTe, category, category, dueDate, instructions, "High") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Save Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// --- EMPTY STATE ---
@Composable
fun EmptyCropCalendarView(
    languageMode: AppLanguage,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF144D2A)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(64.dp))
            Text(
                text = when (languageMode) {
                    AppLanguage.ENGLISH -> "No Crop Calendars Found"
                    AppLanguage.TELUGU -> "ఏ పంట క్యాలెండర్లు లభించలేదు"
                    AppLanguage.DUAL -> "No Crop Calendars (క్యాలెండర్లు లేవు)"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Add your farm crop to get an interactive automated lifecycle timeline with localized English & Telugu task reminders.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color(0xFF1B5E20)),
                modifier = Modifier.testTag("empty_state_add_crop_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Crop Calendar Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Helper: Icons
fun getCropIcon(cropName: String): ImageVector {
    val name = cropName.lowercase()
    return when {
        name.contains("rice") || name.contains("paddy") -> Icons.Default.Grain
        name.contains("chilli") || name.contains("chili") -> Icons.Default.Whatshot
        name.contains("cotton") -> Icons.Default.Cloud
        name.contains("groundnut") -> Icons.Default.Spa
        name.contains("maize") || name.contains("corn") -> Icons.Default.Grass
        name.contains("tomato") -> Icons.Default.LocalFlorist
        else -> Icons.Default.Agriculture
    }
}

fun getStageIcon(iconType: String): ImageVector {
    return when (iconType) {
        "Sowing" -> Icons.Default.Spa
        "Vegetative" -> Icons.Default.Grass
        "Tillering" -> Icons.Default.Park
        "Flowering" -> Icons.Default.LocalFlorist
        "Harvest" -> Icons.Default.Agriculture
        else -> Icons.Default.CheckCircle
    }
}
