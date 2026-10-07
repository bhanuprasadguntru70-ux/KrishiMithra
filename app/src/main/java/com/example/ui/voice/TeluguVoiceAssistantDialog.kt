package com.example.ui.voice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeluguVoiceAssistantDialog(
    voiceState: TeluguVoiceState,
    onDismiss: () -> Unit,
    onQuerySubmitted: (queryText: String, language: String) -> Unit
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(voiceState.selectedLanguage) }

    // Pulsing animation for Mic listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Telugu Voice AI",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "రైతు వాయిస్ AI (Telugu Voice Assistant)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "తెలుగులో సహజంగా మాట్లాడండి 🎙️",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Language Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val langs = listOf("Telugu" to "తెలుగు 🇮🇳", "English" to "English 🇬🇧", "Hindi" to "హిందీ 🇮🇳")
                        langs.forEach { (code, label) ->
                            FilterChip(
                                selected = selectedLanguage == code,
                                onClick = {
                                    selectedLanguage = code
                                    voiceState.selectedLanguage = code
                                },
                                label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = if (selectedLanguage == code) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    // Main Status & Voice Wave Visualizer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (voiceState.sttManager.isListening) {
                                // Sound wave visualization bar
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val baseRms = voiceState.rmsLevel
                                    repeat(7) { index ->
                                        val heightMultiplier = when (index % 4) {
                                            0 -> 1.0f
                                            1 -> 1.6f
                                            2 -> 2.2f
                                            else -> 1.3f
                                        }
                                        val barHeight = ((12 + baseRms * 5) * heightMultiplier).coerceIn(10f, 60f)
                                        Box(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .height(barHeight.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(
                                                        alpha = (0.5f + (index * 0.07f)).coerceAtMost(1.0f)
                                                    )
                                                )
                                        )
                                    }
                                }

                                Text(
                                    text = if (voiceState.partialText.isNotBlank()) {
                                        "\"${voiceState.partialText}\""
                                    } else {
                                        "తెలుగులో మాట్లాడండి (Listening...)"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            } else if (voiceState.ttsEngine.isSpeaking) {
                                // Speaking audio animation
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Speaking",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.scale(pulseScale)
                                    )
                                    Text(
                                        text = "సమాధానం చదివి వినిపిస్తోంది... (Speaking Telugu audio)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else if (voiceState.recognizedText.isNotBlank()) {
                                Text(
                                    text = "మీ ప్రశ్న: \"${voiceState.recognizedText}\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = voiceState.statusText,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Mic Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stop / Clear button
                        OutlinedButton(
                            onClick = {
                                voiceState.stopListening()
                                voiceState.stopSpeaking()
                                voiceState.recognizedText = ""
                                voiceState.partialText = ""
                            },
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("ఆపు (Stop)", fontSize = 12.sp)
                        }

                        // Big Mic Pulse Button
                        Box(contentAlignment = Alignment.Center) {
                            if (voiceState.sttManager.isListening) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                )
                            }

                            FloatingActionButton(
                                onClick = {
                                    if (voiceState.sttManager.isListening) {
                                        voiceState.stopListening()
                                    } else {
                                        voiceState.startListening { text ->
                                            if (text.isNotBlank()) {
                                                onQuerySubmitted(text, selectedLanguage)
                                            }
                                        }
                                    }
                                },
                                containerColor = if (voiceState.sttManager.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    imageVector = if (voiceState.sttManager.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mic",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Send query button if recognized text is present
                        Button(
                            onClick = {
                                val text = voiceState.recognizedText.ifBlank { voiceState.partialText }
                                if (text.isNotBlank()) {
                                    onQuerySubmitted(text, selectedLanguage)
                                }
                            },
                            enabled = voiceState.recognizedText.isNotBlank() || voiceState.partialText.isNotBlank(),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("అడుగు (Ask)", fontSize = 12.sp)
                        }
                    }

                    // Quick Telugu Farmer Query Pills
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "త్వరిత తెలుగు ప్రశ్నలు (Sample Voice Queries):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val quickQueries = listOf(
                            "🌦️ ఈరోజు వాతావరణం మరియు వర్షపాతం వివరాలు చెప్పు",
                            "💰 మిర్చి మరియు వరి పంట నేటి మార్కెట్ ధరలు ఎంత?",
                            "🌾 వరి పంటకు ఆకు నల్లి మరియు తెగుళ్ళ నివారణ మందులు ఏవి?",
                            "🏛️ వైఎస్సార్ రైతు భరోసా, పీఎం కిసాన్ పథకాల వివరాలు"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickQueries.forEach { query ->
                                SuggestionChip(
                                    onClick = {
                                        voiceState.recognizedText = query
                                        onQuerySubmitted(query, selectedLanguage)
                                    },
                                    label = { Text(query, fontSize = 11.sp) },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
