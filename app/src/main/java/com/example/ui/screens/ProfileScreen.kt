package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit,
    onAdminClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onLogout: () -> Unit,
    onOpenAuth: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notificationToggle by viewModel.notificationsEnabled.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }

    // Edit states
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editDistrict by remember { mutableStateOf("") }
    var editState by remember { mutableStateOf("") }
    var editLandHolding by remember { mutableStateOf("") }
    var editPrimaryCrops by remember { mutableStateOf("") }
    var editLanguage by remember { mutableStateOf("English") }

    val languages = listOf("English", "Telugu", "Hindi")
    var langExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            editName = it.name
            editPhone = it.phone
            editDistrict = it.district
            editState = it.state
            editLandHolding = it.landHolding ?: ""
            editPrimaryCrops = it.primaryCrops ?: ""
            editLanguage = it.language
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text(if (currentUser != null) "Farmer Profile" else "Welcome Farmer", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (currentUser != null) {
                    IconButton(onClick = {
                        if (isEditMode) {
                            // Save edits
                            viewModel.updateProfile(
                                currentUser!!.copy(
                                    name = editName,
                                    phone = editPhone,
                                    district = editDistrict,
                                    state = editState,
                                    landHolding = editLandHolding.ifBlank { null },
                                    primaryCrops = editPrimaryCrops.ifBlank { null },
                                    language = editLanguage
                                )
                            )
                        }
                        isEditMode = !isEditMode
                    }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Save" else "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        if (currentUser == null) {
            // UNAUTHENTICATED WELCOME STATE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Generic Farmer Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome Farmer",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Log in or create a farmer account to manage your private profile, land holding, crops, and financial records.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onOpenAuth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Login, contentDescription = "Log In")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log In / Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        } else {
            // AUTHENTICATED FARMER PROFILE STATE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar Icon Block
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentUser!!.photoUri != null) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Uploaded Photo",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(50.dp)
                        )
                    } else {
                        Text(
                            text = editName.take(1).uppercase().ifBlank { "F" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = currentUser!!.email,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Editable user fields card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Farmer Profile Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        if (isEditMode) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = editDistrict,
                                onValueChange = { editDistrict = it },
                                label = { Text("District") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = editState,
                                onValueChange = { editState = it },
                                label = { Text("State") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = editLandHolding,
                                onValueChange = { editLandHolding = it },
                                label = { Text("Land Holding (e.g. 3.5 Acres)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = editPrimaryCrops,
                                onValueChange = { editPrimaryCrops = it },
                                label = { Text("Primary Crops (e.g. Paddy, Tomato)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Language Selector Dropdown
                            ExposedDropdownMenuBox(
                                expanded = langExpanded,
                                onExpandedChange = { langExpanded = !langExpanded }
                            ) {
                                OutlinedTextField(
                                    value = editLanguage,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Preferred Language") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = langExpanded,
                                    onDismissRequest = { langExpanded = false }
                                ) {
                                    languages.forEach { lang ->
                                        DropdownMenuItem(
                                            text = { Text(lang) },
                                            onClick = {
                                                editLanguage = lang
                                                langExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            StaticProfileRow(Icons.Default.Person, "Farmer Name", editName.ifBlank { "Not provided" })
                            StaticProfileRow(Icons.Default.Email, "Email Address", currentUser!!.email)
                            StaticProfileRow(Icons.Default.Phone, "Mobile Number", editPhone.ifBlank { "Not provided" })
                            StaticProfileRow(Icons.Default.LocationCity, "District / Location", if (editDistrict.isNotBlank()) "$editDistrict${if (editState.isNotBlank()) ", $editState" else ""}" else "Not provided")
                            StaticProfileRow(Icons.Default.Landscape, "Land Holding", editLandHolding.ifBlank { "Not provided" })
                            StaticProfileRow(Icons.Default.Eco, "Primary Crops", editPrimaryCrops.ifBlank { "Not provided" })
                            StaticProfileRow(Icons.Default.Language, "Interface Language", editLanguage)
                        }
                    }
                }

                // Premium Status Card or Upgrade option
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentUser!!.isPremium) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = if (currentUser!!.isPremium) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPremiumClick() }
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentUser!!.isPremium) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentUser!!.isPremium) Icons.Default.WorkspacePremium else Icons.Default.CardMembership,
                                    contentDescription = "Subscription",
                                    tint = if (currentUser!!.isPremium) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (currentUser!!.isPremium) "Premium Active (${currentUser!!.subscriptionPlan})" else "Unlock Premium AI Tools",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (currentUser!!.isPremium) "Your plan is active and unlocked." else "Tap to view plans and get started.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Premium details", tint = Color.Gray)
                    }
                }

                // App settings configuration card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Preferences & System",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Dark Theme Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.DarkMode, contentDescription = "Dark Theme", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Dark Mode Theme", fontSize = 14.sp)
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() }
                            )
                        }

                        // Notification Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Push Alerts (Rain/Disease)", fontSize = 14.sp)
                            }
                            Switch(
                                checked = notificationToggle,
                                onCheckedChange = { viewModel.toggleNotifications() }
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                        // Admin panel navigation row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAdminClick() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color.Gray)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Access Administrative Console", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Admin Nav", tint = Color.Gray)
                        }
                    }
                }

                // Logout button
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(top = 8.dp, bottom = 32.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Farmer Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun StaticProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
