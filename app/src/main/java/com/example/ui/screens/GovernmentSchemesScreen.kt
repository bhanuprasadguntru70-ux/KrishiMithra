package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.AgriViewModel

data class Scheme(
    val name: String,
    val nameTe: String,
    val type: String, // "Central", "State"
    val category: String, // "Financial Support", "Insurance", "Irrigation", "Solar", "Organic"
    val benefits: String,
    val eligibility: String,
    val documents: List<String>,
    val website: String,
    val deadline: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovernmentSchemesScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All") } // "All", "Central", "State"
    var selectedCategoryFilter by remember { mutableStateOf("All") } // "All", "Financial Support", "Insurance", "Irrigation", "Solar", "Organic"

    val uriHandler = LocalUriHandler.current
    var showApplySuccessDialog by remember { mutableStateOf<String?>(null) }

    val schemes = remember {
        listOf(
            Scheme(
                name = "PM-KISAN Samman Nidhi",
                nameTe = "పీఎం కిసాన్ సమ్మాన్ నిధి",
                type = "Central",
                category = "Financial Support",
                benefits = "₹6,000 per year directly transferred to the bank account in 3 equal installments of ₹2,000.",
                eligibility = "Small and marginal farmer families holding cultivable land up to 2 hectares.",
                documents = listOf("Aadhaar Card", "Land Ownership Documents (Pattadar Passbook)", "Bank Account Details (linked to Aadhaar)", "Mobile Number"),
                website = "https://pmkisan.gov.in",
                deadline = "August 15, 2026",
                description = "An initiative by the Government of India that provides an absolute income support of ₹6,000 per annum to all landholding farmer families across the country."
            ),
            Scheme(
                name = "YSR Rythu Bharosa - PM KISAN",
                nameTe = "వైఎస్ఆర్ రైతు భరోసా - పీఎం కిసాన్",
                type = "State",
                category = "Financial Support",
                benefits = "₹13,500 per annum (₹7,500 from State + ₹6,000 from Central PM-Kisan) to support cultivation costs.",
                eligibility = "All landholding farmer families in Andhra Pradesh, including tenant farmers of SC, ST, BC, and Minority groups.",
                documents = listOf("Aadhaar Card", "Pattadar Adangal / Webland copy", "Tenant Agreement (if applicable)", "Bank Passbook"),
                website = "https://rythubharosa.ap.gov.in",
                deadline = "September 30, 2026",
                description = "Andhra Pradesh State government scheme to assist farmers in purchase of seed, fertilizer, and agricultural inputs."
            ),
            Scheme(
                name = "Pradhan Mantri Fasal Bima Yojana (PMFBY)",
                nameTe = "ప్రధాన మంత్రి ఫసల్ బీమా యోజన",
                type = "Central",
                category = "Insurance",
                benefits = "Comprehensive insurance coverage against crop failure from natural calamities, pests, and diseases. Low premium rate of 1.5% to 2% for food & oilseeds.",
                eligibility = "All farmers growing notified crops in notified areas, including sharecroppers and tenant farmers.",
                documents = listOf("Land Record Copy", "Sowing Certificate / Crop Sowing Report", "Aadhaar Card", "Bank Account Details"),
                website = "https://pmfby.gov.in",
                deadline = "July 31, 2026",
                description = "Yield-based crop insurance scheme aiming to provide financial relief to farmers suffering crop loss/damage."
            ),
            Scheme(
                name = "Kisan Credit Card (KCC) Loans",
                nameTe = "కిసాన్ క్రెడిట్ కార్డ్ రుణాలు",
                type = "Central",
                category = "Financial Support",
                benefits = "Provides short-term credit for crop cultivation at extremely low interest rates (effectively 4% after timely repayment subvention). Up to ₹3 Lakhs limit.",
                eligibility = "All land-holding farmers, tenant farmers, sharecroppers, and joint liability farming groups.",
                documents = listOf("Duly filled KCC Application Form", "Land revenue records / lease agreement", "Aadhaar Card / PAN Card", "Passport size photograph"),
                website = "https://www.sbi.co.in/web/personal-banking/loans/agriculture-loans",
                deadline = "Ongoing Scheme",
                description = "Ensures farmers have easy and cheap access to credit for buying seeds, fertilizers, pesticides, and machinery."
            ),
            Scheme(
                name = "PM-KUSUM Solar Pump Subsidy",
                nameTe = "పీఎం కుసుమ్ సోలార్ పంప్ సబ్సిడీ",
                type = "Central",
                category = "Solar",
                benefits = "Up to 90% subsidy (60% Central/State subsidy + 30% bank loan) to install stand-alone solar water pumps.",
                eligibility = "Individual farmers, water user associations, co-operatives, or community farming groups.",
                documents = listOf("Aadhaar Card", "Land Naksha / Adangal copy", "Borewell/Well ownership certificate", "Bank Passbook copy"),
                website = "https://mnre.gov.in",
                deadline = "October 31, 2026",
                description = "Enables farmers to install solar power pumps, replacing grid-connected pumps and dirty diesel pump systems."
            ),
            Scheme(
                name = "Subsidized Drip & Sprinkler Irrigation",
                nameTe = "రాయితీ మైక్రో నీటిపారుదల",
                type = "State",
                category = "Irrigation",
                benefits = "Up to 90% subsidy for small and marginal farmers to install drip or sprinkler irrigation systems on their fields.",
                eligibility = "Farmers with cultivable land with a functional water source (borewell/pond).",
                documents = listOf("Soil & Water test report (if applicable)", "Aadhaar Card", "Land registration certificates", "Income Certificate"),
                website = "http://www.apmicroirrigation.ap.gov.in",
                deadline = "December 31, 2026",
                description = "Saves water and improves crop yields significantly by delivering water directly to the plant root zone."
            )
        )
    }

    val filteredSchemes = schemes.filter { scheme ->
        val matchesSearch = scheme.name.contains(searchQuery, ignoreCase = true) || 
                            scheme.nameTe.contains(searchQuery, ignoreCase = true) ||
                            scheme.description.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedTypeFilter == "All" || scheme.type == selectedTypeFilter
        val matchesCategory = selectedCategoryFilter == "All" || scheme.category == selectedCategoryFilter
        matchesSearch && matchesType && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Govt Schemes & Subsidies", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Central & State Welfare Support", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        // Filters card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Rythu Bharosa, Solar, PM-Kisan...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Type Filter row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    listOf("All", "Central", "State").forEach { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = { selectedTypeFilter = type },
                            label = { Text(type, fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Category Filter row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sector:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    listOf("All", "Financial Support", "Insurance", "Irrigation", "Solar").forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 9.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }
        }

        if (filteredSchemes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = "Empty", tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No schemes found matching criteria.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredSchemes) { scheme ->
                    SchemeCard(
                        scheme = scheme,
                        onApply = {
                            showApplySuccessDialog = scheme.name
                        },
                        onWebsiteClick = {
                            uriHandler.openUri(scheme.website)
                        }
                    )
                }
            }
        }
    }

    // Success dialog
    if (showApplySuccessDialog != null) {
        Dialog(onDismissRequest = { showApplySuccessDialog = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Application Initiated! 🎉",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "We have simulated a secure API call to register your farm credentials for the '${showApplySuccessDialog}' scheme. Your details have been submitted to the Krishi Seva gateway. Keep checking notifications for verification updates.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = { showApplySuccessDialog = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dhanyavadalu / Okay")
                    }
                }
            }
        }
    }
}

@Composable
fun SchemeCard(
    scheme: Scheme,
    onApply: () -> Unit,
    onWebsiteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val badgeColor = if (scheme.type == "Central") {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val badgeTextColor = if (scheme.type == "Central") {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${scheme.type} Scheme",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = scheme.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = scheme.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = scheme.nameTe,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = scheme.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                    // Benefits
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Benefits", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Key Benefits:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Text(scheme.benefits, fontSize = 12.sp, modifier = Modifier.padding(start = 22.dp), lineHeight = 18.sp)
                    }

                    // Eligibility
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FactCheck, contentDescription = "Eligibility", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Eligibility Criteria:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Text(scheme.eligibility, fontSize = 12.sp, modifier = Modifier.padding(start = 22.dp), lineHeight = 18.sp)
                    }

                    // Required Documents
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilePresent, contentDescription = "Documents", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Required Documents:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        scheme.documents.forEach { doc ->
                            Row(
                                modifier = Modifier.padding(start = 22.dp, top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(doc, fontSize = 12.sp)
                            }
                        }
                    }

                    // Deadline Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Deadline", tint = Color.Red, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Apply Before: ${scheme.deadline}",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onWebsiteClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = "Website", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Portal", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onApply,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Apply", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
