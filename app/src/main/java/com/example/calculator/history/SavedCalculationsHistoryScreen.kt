package com.example.calculator.history

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.components.CalculatorTopHeader
import com.example.calculator.components.SavedHistoryItemCard
import com.example.data.model.SavedCalculationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCalculationsHistoryScreen(
    savedCalculations: List<SavedCalculationEntity>,
    appLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDeleteCalculation: (Int) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CalculatorTopHeader(
                titleEn = "Saved Calculations History",
                titleTe = "భద్రపరచిన లెక్కల వివరాలు",
                appLanguage = appLanguage,
                onLanguageSelected = onLanguageSelected,
                onBack = onBack
            )
        },
        floatingActionButton = {
            if (savedCalculations.isNotEmpty()) {
                SmallFloatingActionButton(
                    onClick = { showClearDialog = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear All")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (savedCalculations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (appLanguage == "te") "ఇంకా ఏ లెక్కలు దాచలేదు" else "No saved calculations yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Text(
                            text = if (appLanguage == "te") "ఏదైనా క్యాలిక్యులేటర్‌లో SAVE బటన్ నొక్కండి" else "Use any calculator and tap 'SAVE' to store results here",
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(savedCalculations, key = { it.id }) { item ->
                        SavedHistoryItemCard(
                            calculation = item,
                            onDelete = onDeleteCalculation
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(if (appLanguage == "te") "అన్ని లెక్కలు తొలగించాలా?" else "Clear All History?") },
            text = { Text(if (appLanguage == "te") "భద్రపరచిన అన్ని క్యాలిక్యులేషన్స్ రద్దవుతాయి." else "This will delete all your saved calculation records permanently.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                        Toast.makeText(context, if (appLanguage == "te") "తొలగించబడ్డాయి" else "Cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(if (appLanguage == "te") "అవును, తొలగించు" else "Yes, Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(if (appLanguage == "te") "రద్దు చేయి" else "Cancel")
                }
            }
        )
    }
}
