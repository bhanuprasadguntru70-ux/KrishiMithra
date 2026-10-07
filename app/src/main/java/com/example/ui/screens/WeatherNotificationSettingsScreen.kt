package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherNotificationSettings
import com.example.ui.AgriViewModel
import com.example.util.WeatherNotificationHelper
import com.example.util.WeatherPrefsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherNotificationSettingsScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { WeatherPrefsManager(context) }
    var settings by remember { mutableStateOf(prefsManager.getSettings()) }
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isTelugu = appLanguage == "te"

    fun updateSettings(newSettings: WeatherNotificationSettings) {
        settings = newSettings
        prefsManager.saveSettings(newSettings)
        viewModel.updateWeatherNotificationSettings(newSettings)
        viewModel.setAppLanguage(newSettings.preferredLanguage)
        WeatherNotificationHelper.syncFcmTopics(
            context,
            newSettings.manualLat,
            newSettings.manualLon,
            newSettings.manualDistrict
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTelugu) "వాతావరణ నోటిఫికేషన్ల సెట్టింగ్‌లు" else "Weather Notification Settings",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isTelugu) "మీ గ్రామం లేదా ప్రాంతానికి తగిన అలర్ట్‌లు" else "Location-Specific Alerts for Farmers",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20)
                )
            )
        },
        containerColor = Color(0xFFF4F6F8)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // MASTER TOGGLE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("master_notification_switch"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.enabled) Color(0xFFE8F5E9) else Color.White
                ),
                border = BorderStroke(1.dp, if (settings.enabled) Color(0xFF2E7D32) else Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (settings.enabled) Color(0xFF2E7D32) else Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isTelugu) "వాతావరణ అలర్ట్‌లు ప్రారంభించండి" else "Enable Weather Alerts",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = if (isTelugu) "యాప్ మూసివేసినప్పుడు కూడా హెచ్చరికలు అందుతాయి" else "Receive alerts even when app is closed",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = settings.enabled,
                        onCheckedChange = { isChecked ->
                            updateSettings(settings.copy(enabled = isChecked))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32))
                    )
                }
            }

            AnimatedVisibility(visible = settings.enabled) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // LOCATION CLUSTER & PRIVACY CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTelugu) "ప్రాంతీయ స్థానాల వర్గీకరణ (Location Grouping)" else "Location Cluster & Privacy",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D47A1)
                                )
                            }

                            Text(
                                text = if (isTelugu)
                                    "లక్షలాది రైతుల గోప్యతను గౌరవిస్తూ మీ ప్రాంతాన్ని (~5km-10km క్లస్టర్) Geohash ఆధారంగా గ్రూప్ చేస్తాము. ప్రత్యేక వ్యక్తగత GPS వివరాలు సర్వర్‌లో సేకరించబడవు."
                                else
                                    "To protect privacy, users are grouped into ~5km-10km Geohash geographic clusters. Precise personal GPS trace is never stored.",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                lineHeight = 16.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE3F2FD),
                                border = BorderStroke(1.dp, Color(0xFF90CAF9))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isTelugu) "ప్రస్తుత యాక్టివ్ టాపిక్:" else "Active Topic Subscription:",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "${settings.activeDistrictTopic} | ${settings.activeGeohash}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1565C0)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            // Mode Selector (GPS vs Manual)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = settings.locationMode == "GPS",
                                    onClick = { updateSettings(settings.copy(locationMode = "GPS")) },
                                    label = { Text(if (isTelugu) "📍 GPS స్థానం" else "📍 Dynamic GPS") },
                                    modifier = Modifier.weight(1f).testTag("gps_location_chip")
                                )
                                FilterChip(
                                    selected = settings.locationMode == "MANUAL",
                                    onClick = { updateSettings(settings.copy(locationMode = "MANUAL")) },
                                    label = { Text(if (isTelugu) "🏙️ స్థిర ప్రాంతం" else "🏙️ Fixed Region") },
                                    modifier = Modifier.weight(1f).testTag("manual_location_chip")
                                )
                            }
                        }
                    }

                    // ALERT CATEGORIES TOGGLES CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (isTelugu) "అలర్ట్ రకాలు (Alert Categories)" else "Alert Types & Preferences",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )

                            // Rain Alerts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🌧️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isTelugu) "వర్షాల హెచ్చరికలు (Rain Alerts)" else "Rain & Shower Alerts",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (isTelugu) "వర్షం పడే అవకాశం ఉన్నప్పుడు నోటిఫికేషన్" else "Notify when rain probability > 60%",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Switch(
                                    checked = settings.rainAlertsEnabled,
                                    onCheckedChange = { isChecked ->
                                        updateSettings(settings.copy(rainAlertsEnabled = isChecked))
                                    }
                                )
                            }

                            HorizontalDivider(color = Color(0xFFEEEEEE))

                            // Severe Weather Alerts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("⚡", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isTelugu) "తుఫాను & ఈదురు గాలుల అలర్ట్‌లు" else "Thunderstorm & Severe Wind Alerts",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (isTelugu) "భారీ వర్షాలు, ఈదురు గాలులు, తీవ్రమైన ఎండలు" else "High priority severe weather warnings",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Switch(
                                    checked = settings.severeAlertsEnabled,
                                    onCheckedChange = { isChecked ->
                                        updateSettings(settings.copy(severeAlertsEnabled = isChecked))
                                    }
                                )
                            }

                            HorizontalDivider(color = Color(0xFFEEEEEE))

                            // Daily Morning Summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🌅", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isTelugu) "ఉదయం రోజువారీ సమాచారం" else "Daily Morning Weather Update",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (isTelugu) "ప్రతిరోజూ ఉదయం వాతావరణ బులెటిన్" else "Daily forecast summary every morning",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Switch(
                                    checked = settings.dailyUpdateEnabled,
                                    onCheckedChange = { isChecked ->
                                        updateSettings(settings.copy(dailyUpdateEnabled = isChecked))
                                    }
                                )
                            }

                            if (settings.dailyUpdateEnabled) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isTelugu) "ఉదయం నోటిఫికేషన్ సమయం:" else "Morning Alert Time:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("06:00", "07:00", "08:00").forEach { time ->
                                        FilterChip(
                                            selected = settings.morningNotificationTime == time,
                                            onClick = { updateSettings(settings.copy(morningNotificationTime = time)) },
                                            label = { Text("$time AM") }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // LANGUAGE PREFERENCE CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isTelugu) "నోటిఫికేషన్ భాష (Notification Language)" else "Notification Language",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { updateSettings(settings.copy(preferredLanguage = "te")) },
                                    modifier = Modifier.weight(1f).testTag("lang_te_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        2.dp,
                                        if (settings.preferredLanguage == "te") Color(0xFF2E7D32) else Color(0xFFE0E0E0)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (settings.preferredLanguage == "te") Color(0xFFE8F5E9) else Color.White
                                    )
                                ) {
                                    Text("తెలుగు (Telugu)", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                }

                                OutlinedButton(
                                    onClick = { updateSettings(settings.copy(preferredLanguage = "en")) },
                                    modifier = Modifier.weight(1f).testTag("lang_en_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        2.dp,
                                        if (settings.preferredLanguage == "en") Color(0xFF2E7D32) else Color(0xFFE0E0E0)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (settings.preferredLanguage == "en") Color(0xFFE8F5E9) else Color.White
                                    )
                                ) {
                                    Text("English", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                }
                            }
                        }
                    }

                    // TEST NOTIFICATION BUTTON
                    Button(
                        onClick = {
                            val locName = settings.manualLocality
                            val testTitle = if (isTelugu) "🌧️ వర్ష సూచన - $locName" else "🌧️ Rain Alert - $locName"
                            val testBody = if (isTelugu) {
                                "మీ ప్రాంతంలో ($locName) రాబోయే కొన్ని గంటల్లో వర్షం పడే అవకాశం ఉంది. రైతులు అవసరమైన పంట పనులను ముందుగానే ప్లాన్ చేసుకోండి."
                            } else {
                                "Rain expected in $locName within a few hours. Plan field operations accordingly."
                            }

                            WeatherNotificationHelper.showWeatherNotification(
                                context = context,
                                title = testTitle,
                                body = testBody,
                                isSevere = false
                            )

                            Toast.makeText(
                                context,
                                if (isTelugu) "పరీక్షా నోటిఫికేషన్ పంపబడింది! 📲" else "Test notification sent! 📲",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_test_notification_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTelugu) "పరీక్షా నోటిఫికేషన్ పంపండి (Test Alert)" else "Send Test Notification",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
