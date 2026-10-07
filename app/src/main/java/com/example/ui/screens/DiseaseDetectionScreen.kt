package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DiseaseReportEntity
import com.example.ui.AgriViewModel
import com.example.ui.util.FarmerTranslations
import com.example.ui.util.PortalLanguageSelector
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetectionScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingDisease.collectAsState()
    val report by viewModel.diseaseResult.collectAsState()
    val engineName by viewModel.lastClassificationEngine.collectAsState()
    val executionTimeMs by viewModel.lastExecutionTimeMs.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedSampleIndex by remember { mutableStateOf(-1) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var isCameraActive by remember { mutableStateOf(false) }

    // Check camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                isCameraActive = true
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
                        selectedBitmap = bitmap
                        selectedSampleIndex = -1 // Indicates a custom image (camera or gallery)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )

    // Hardcoded beautiful color blocks representing crop leaves for testing in simulator
    val leafSamples = listOf(
        LeafSample("Rice Leaf Blast", Color(0xFFC5E1A5), "Fungal leaf disease in Paddy"),
        LeafSample("Tomato Leaf Mold", Color(0xFFFFE082), "Fungal yellow spots on tomato leaves"),
        LeafSample("Wheat Leaf Rust", Color(0xFFFFCC80), "Orange powdery rust pustules on wheat"),
        LeafSample("Cotton Leaf Curl", Color(0xFF80CBC4), "Thickened, upward curling leaves on cotton")
    )

    if (isCameraActive) {
        CameraCaptureView(
            onImageCaptured = { bitmap ->
                selectedBitmap = bitmap
                selectedSampleIndex = -1 // custom photo
                isCameraActive = false
            },
            onClose = {
                isCameraActive = false
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // TopAppBar
            TopAppBar(
                title = { Text("Disease Detection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    PortalLanguageSelector(
                        currentLanguage = appLanguage,
                        onLanguageSelected = { viewModel.setAppLanguage(it) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Upload / Camera Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    if (selectedBitmap != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (selectedSampleIndex >= 0) {
                                // Draw sample crop visual block
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(leafSamples[selectedSampleIndex].color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = "Scanning",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = leafSamples[selectedSampleIndex].name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Ready for Gemini AI Diagnostics",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            } else {
                                // Draw actual captured or gallery image
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "Selected Leaf Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Gorgeous diagnostic scan filter overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = "Scanning Active",
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Custom Leaf Photo",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Ready for Gemini AI Diagnostics",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // Remove image button
                            IconButton(
                                onClick = {
                                    selectedBitmap = null
                                    selectedSampleIndex = -1
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    showSourcePicker = true
                                },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Take Photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Take Photo or Upload Image",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tap to upload plant leaf picture",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Sample Selector (Highly Useful for Demonstrations)
                if (selectedBitmap == null) {
                    Text(
                        text = "Select a Leaf Sample to Diagnostic Scan:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        leafSamples.forEachIndexed { index, sample ->
                            Card(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(90.dp)
                                    .clickable {
                                        val conf = Bitmap.Config.ARGB_8888
                                        val mockBitmap = Bitmap.createBitmap(100, 100, conf)
                                        selectedBitmap = mockBitmap
                                        selectedSampleIndex = index
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = sample.color)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = sample.name,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = sample.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF1E351E)
                                        )
                                        Text(
                                            text = sample.desc,
                                            fontSize = 9.sp,
                                            color = Color(0xFF1E351E).copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Analyze CTA Button
                if (selectedBitmap != null) {
                    Button(
                        onClick = {
                            selectedBitmap?.let {
                                viewModel.analyzeDiseaseImage(it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("analyze_leaf_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TensorFlow Lite AI Diagnosing Leaf...")
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = "TFLite")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Leaf with TensorFlow Lite", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Diagnosis Report Area
                AnimatedVisibility(
                    visible = report != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    report?.let { details ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Header with Match Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "PATHOLOGY REPORT",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = details.cropName,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${details.confidenceScore.toInt()}% AI Confidence") },
                                        leadingIcon = { Icon(Icons.Default.Verified, contentDescription = "Confidence", tint = MaterialTheme.colorScheme.primary) }
                                    )
                                }

                                // On-Device TensorFlow Lite Banner
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ElectricBolt,
                                                contentDescription = "TFLite",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = engineName,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        if (executionTimeMs > 0) {
                                            Text(
                                                text = "${executionTimeMs} ms",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                // Disease details
                                ReportSection(
                                    label = "DISEASE IDENTIFIED",
                                    value = details.diseaseName,
                                    icon = Icons.Default.BugReport,
                                    iconColor = MaterialTheme.colorScheme.error
                                )

                                ReportSection(
                                    label = "BIOLOGICAL CAUSE",
                                    value = details.cause,
                                    icon = Icons.Default.Info
                                )

                                ReportSection(
                                    label = "VISUAL SYMPTOMS",
                                    value = details.symptoms,
                                    icon = Icons.Default.Visibility
                                )

                                ReportSection(
                                    label = "ORGANIC SOLUTION (RECOMMENDED)",
                                    value = details.organicSolution,
                                    icon = Icons.Default.Eco,
                                    iconColor = MaterialTheme.colorScheme.secondary
                                )

                                ReportSection(
                                    label = "CHEMICAL TREATMENT",
                                    value = details.chemicalSolution,
                                    icon = Icons.Default.Science
                                )

                                ReportSection(
                                    label = "PREVENTIVE STRATEGY",
                                    value = details.preventiveMeasures,
                                    icon = Icons.Default.Shield
                                )

                                ReportSection(
                                    label = "LOCAL SUPPORT CENTRE",
                                    value = details.nearbyOffice,
                                    icon = Icons.Default.Business
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Save & Share report
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            // Share Report Simulated
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share")
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveDiseaseReport(details)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save Report")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modern M3 selection Dialog
    if (showSourcePicker) {
        AlertDialog(
            onDismissRequest = { showSourcePicker = false },
            title = {
                Text(
                    text = "Select Image Source",
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
                                text = "Use device camera to snap symptoms",
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

data class LeafSample(
    val name: String,
    val color: Color,
    val desc: String
)

@Composable
fun ReportSection(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
