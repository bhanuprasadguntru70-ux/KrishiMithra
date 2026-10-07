package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.AgriViewModel
import com.example.ui.ChatMessage
import com.example.ui.voice.TeluguVoiceAssistantDialog
import com.example.ui.voice.rememberTeluguVoiceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    var selectedLanguage by remember { mutableStateOf("Telugu") }
    LaunchedEffect(appLanguage) {
        selectedLanguage = if (appLanguage == "te") "Telugu" else "English"
    }
    var isAutoVoiceEnabled by remember { mutableStateOf(false) }

    val teluguVoiceState = rememberTeluguVoiceState(
        onFinalSpokenText = { spoken ->
            inputMessage = spoken
        }
    )

    // Multimodal attachments
    var attachedImage by remember { mutableStateOf<Bitmap?>(null) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var isCameraActive by remember { mutableStateOf(false) }

    var isRecordingActive by remember { mutableStateOf(false) }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasAudioPermission = granted
            if (granted) {
                isRecordingActive = true
            } else {
                Toast.makeText(context, "Microphone permission is required to record voice queries.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // TTS state
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsSpeaking by remember { mutableStateOf(false) }
    var speakingText by remember { mutableStateOf("") }

    val languages = listOf(
        "English", "Telugu", "Hindi", "Tamil", "Kannada", 
        "Malayalam", "Marathi", "Bengali", "Gujarati", 
        "Punjabi", "Odia", "Assamese", "Urdu"
    )
    var langMenuExpanded by remember { mutableStateOf(false) }

    // Check camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher for camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                isCameraActive = true
            } else {
                Toast.makeText(context, "Camera permission is required to snap crop leaf photos.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        attachedImage = bitmap
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load image from gallery.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Speech Recognition Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isRecordingActive = false
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                inputMessage = spokenText
            }
        }
    }

    // Start Voice Input Intent
    val startVoiceInput = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val localeString = when (selectedLanguage) {
                "Telugu" -> "te-IN"
                "Hindi" -> "hi-IN"
                "Tamil" -> "ta-IN"
                "Kannada" -> "kn-IN"
                "Malayalam" -> "ml-IN"
                "Marathi" -> "mr-IN"
                "Bengali" -> "bn-IN"
                "Gujarati" -> "gu-IN"
                "Punjabi" -> "pa-IN"
                "Odia" -> "or-IN"
                "Assamese" -> "as-IN"
                "Urdu" -> "ur-IN"
                else -> "en-US"
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeString)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeString)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeString)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now in $selectedLanguage...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Speech recognition is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecordingActive) {
        if (isRecordingActive) {
            delay(300)
            startVoiceInput()
        }
    }

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(0.95f)
            }
        }
        currentUser?.let {
            if (it.language.isNotBlank()) {
                val primaryLang = it.language.split(",").firstOrNull()?.trim() ?: "English"
                if (languages.contains(primaryLang)) {
                    selectedLanguage = primaryLang
                }
            }
        }
    }

    // Shut down TTS on dispose
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Auto-scroll to bottom on new message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // TTS speaker helper
    val speakResponse: (String) -> Unit = { text ->
        if (tts != null) {
            val cleanText = text.replace(Regex("[*#_`]"), "") // clean markdown
            speakingText = cleanText
            val locale = when (selectedLanguage) {
                "Telugu" -> Locale("te", "IN")
                "Hindi" -> Locale("hi", "IN")
                "Tamil" -> Locale("ta", "IN")
                "Kannada" -> Locale("kn", "IN")
                "Malayalam" -> Locale("ml", "IN")
                "Marathi" -> Locale("mr", "IN")
                "Bengali" -> Locale("bn", "IN")
                "Gujarati" -> Locale("gu", "IN")
                "Punjabi" -> Locale("pa", "IN")
                "Odia" -> Locale("or", "IN")
                "Assamese" -> Locale("as", "IN")
                "Urdu" -> Locale("ur", "IN")
                else -> Locale.US
            }
            tts?.language = locale
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "AgriAiTts")
            isTtsSpeaking = true
        }
    }

    val stopSpeaking = {
        tts?.stop()
        isTtsSpeaking = false
    }

    // Auto-read response if Auto-Voice is enabled and a new model message arrives
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            val lastMsg = chatMessages.last()
            if (!lastMsg.isUser && isAutoVoiceEnabled) {
                speakResponse(lastMsg.text)
            }
        }
    }

    if (isCameraActive) {
        CameraCaptureView(
            onImageCaptured = { bitmap ->
                attachedImage = bitmap
                isCameraActive = false
            },
            onClose = {
                isCameraActive = false
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
            // --- Top Bar with Language Selector & TTS Toggle ---
            TopAppBar(
                title = {
                    Column {
                        Text("Agri AI 24/7 Assistant 🤖", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Ready to speak in $selectedLanguage", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Auto-voice reply toggle
                    IconButton(
                        onClick = { 
                            isAutoVoiceEnabled = !isAutoVoiceEnabled 
                            if (!isAutoVoiceEnabled) {
                                stopSpeaking()
                            } else {
                                Toast.makeText(context, "AI Voice Replies activated!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isAutoVoiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Auto Voice Toggle",
                            tint = if (isAutoVoiceEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // Language Selector
                    Box {
                        IconButton(onClick = { langMenuExpanded = true }) {
                            Icon(imageVector = Icons.Default.Translate, contentDescription = "Change Language", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(
                            expanded = langMenuExpanded,
                            onDismissRequest = { langMenuExpanded = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        selectedLanguage = lang
                                        viewModel.setAppLanguage(if (lang == "Telugu") "te" else "en")
                                        langMenuExpanded = false
                                        Toast.makeText(context, "Voice language switched to $lang", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = { 
                        viewModel.clearChat() 
                        stopSpeaking()
                    }) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            // --- Active Chat List ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatMessages) { message ->
                        ChatMessageBubble(
                            message = message,
                            onTextToSpeech = { speakResponse(message.text) }
                        )
                    }

                    if (isChatLoading) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Agri AI is thinking...", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // Speaking Status overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isTtsSpeaking,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Speaking", tint = Color.White)
                                Text(
                                    text = "Speaking response in $selectedLanguage...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { stopSpeaking() }, modifier = Modifier.size(24.dp)) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // --- Image Preview Overlay ---
            if (attachedImage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            bitmap = attachedImage!!.asImageBitmap(),
                            contentDescription = "Attached Image Preview",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Photo attached 📸",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ready to analyze crop disease, fertilizers or planning.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = { attachedImage = null },
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // --- Features & Topics Scroll Row (Shortcut Pills) ---
            Text(
                text = "💡 Tap to ask or diagnose crop issue:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { showSourcePicker = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Scan", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🩺 Scan Leaf / Disease Detect",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                FeaturePill(
                    title = "🧪 Fertilizer Guide",
                    onClick = {
                        inputMessage = "Provide tailored fertilizer recommendations, application methods, dosage guidelines and safe usage precautions for my farm crops."
                    }
                )
                FeaturePill(
                    title = "🌿 Organic Farming Tips",
                    onClick = {
                        inputMessage = "Give me organic farming suggestions, bio-pesticides recipes (like neem oil or Azolla feed), compost guidance, and sustainable methods."
                    }
                )
                FeaturePill(
                    title = "🏛️ Govt Schemes",
                    onClick = {
                        inputMessage = "Explain latest government agriculture schemes, Rythu Bharosa, PM-Kisan financial support, and subsidy application requirements."
                    }
                )
                FeaturePill(
                    title = "🌦️ Weather Advisory",
                    onClick = {
                        inputMessage = "Provide weather-guided farming sallah (advisory), rain guidance, crop protective actions and pesticide spray timing guidelines."
                    }
                )
                FeaturePill(
                    title = "📈 Market Rates & Tips",
                    onClick = {
                        inputMessage = "Show latest crop market price trends, APMC mandi rates, and suggestions on when to sell crop yield for maximum profit."
                    }
                )
                FeaturePill(
                    title = "📅 Crop Rotation Plan",
                    onClick = {
                        inputMessage = "Design a crop planning schedule, crop rotation tips, nursery management guides and soil health restoration advice."
                    }
                )
            }

            // --- Chat Input Bar ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Camera / Attachment Selector
                    IconButton(
                        onClick = { showSourcePicker = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Attach image", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Mic Voice STT Button
                    IconButton(
                        onClick = {
                            if (hasAudioPermission) {
                                isRecordingActive = true
                            } else {
                                audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                            .testTag("mic_voice_button")
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Message Text Field
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Ask Agri AI ($selectedLanguage)...", fontSize = 13.sp) },
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp)
                    )

                    // Submit Button
                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank() || attachedImage != null) {
                                viewModel.sendMessage(inputMessage, selectedLanguage, attachedImage)
                                inputMessage = ""
                                attachedImage = null
                                stopSpeaking()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        enabled = inputMessage.isNotBlank() || attachedImage != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputMessage.isNotBlank() || attachedImage != null) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            } // Closes Card
        } // Closes main Column

        // --- Voice Assistant Recording Overlay ---
        if (isRecordingActive) {
            TeluguVoiceAssistantDialog(
                voiceState = teluguVoiceState,
                onDismiss = { isRecordingActive = false },
                onQuerySubmitted = { queryText, lang ->
                    isRecordingActive = false
                    selectedLanguage = lang
                    viewModel.sendMessage(queryText, lang, attachedImage)
                    inputMessage = ""
                    attachedImage = null
                }
            )
        }
        }
    }

    // --- Modern Source Picker Dialog ---
    if (showSourcePicker) {
        AlertDialog(
            onDismissRequest = { showSourcePicker = false },
            title = {
                Text(
                    text = "Select Leaf / Farm Image",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Option 1: Camera
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSourcePicker = false
                                if (hasCameraPermission) {
                                    isCameraActive = true
                                } else {
                                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            }
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Take a Photo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Use camera to snap symptoms on crop leaf",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Option 2: Gallery
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSourcePicker = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Upload from Gallery",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select from photos on your device",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourcePicker = false }) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
fun FeaturePill(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onTextToSpeech: () -> Unit
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }
    val textColor = if (isUser) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val bubbleShape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (message.image != null) {
                    Image(
                        bitmap = message.image.asImageBitmap(),
                        contentDescription = "Attached Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .padding(bottom = 6.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                if (!isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onTextToSpeech, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "TTS Speak",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundWaveVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(80.dp)
            .padding(vertical = 16.dp)
    ) {
        val heights = listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 0.4f, 0.7f)
        heights.forEachIndexed { index, baseScale ->
            val duration = 400 + (index * 120)
            val animatedScale by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            val barHeight = (40.dp * (animatedScale * baseScale + 0.2f)).coerceAtLeast(6.dp)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(barHeight)
                    .background(
                        color = if (index % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}
