package com.example.chattopdf.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chattopdf.ui.components.PrimaryDarkGreen
import com.example.chattopdf.ui.components.ScreenBackground
import com.example.chattopdf.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    // Collect the state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBackground
                )
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Adds padding to the edges
            verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between cards
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // --- Document Settings ---
            item {
                SettingsGroup(title = "Scanning & PDF") {
                    SettingItem(
                        title = "Default Page Size",
                        description = uiState.defaultPageSize,
                        icon = Icons.Default.Description,
                        onClick = { /* Open bottom sheet to select size */ }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    SettingSwitchItem(
                        title = "OCR (Text Recognition)",
                        description = if (uiState.ocrEnabled) "Enabled (English)" else "Disabled",
                        icon = Icons.Default.Translate,
                        isChecked = uiState.ocrEnabled,
                        onCheckedChange = { viewModel.toggleOcr(it) }
                    )
                }
            }

            // --- Storage & Backup ---
            item {
                SettingsGroup(title = "Storage") {
                    SettingSwitchItem(
                        title = "Cloud Sync",
                        description = if (uiState.cloudSyncEnabled) "Google Drive (Connected)" else "Disconnected",
                        icon = Icons.Default.CloudUpload,
                        isChecked = uiState.cloudSyncEnabled,
                        onCheckedChange = { viewModel.toggleCloudSync(it) }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    SettingItem(
                        title = "Clear Cache",
                        description = "Current size: ${uiState.cacheSizeMB} MB",
                        icon = Icons.Default.DeleteSweep,
                        onClick = { viewModel.clearCache() }
                    )
                }
            }

            // --- Personalization ---
            item {
                SettingsGroup(title = "Interface") {
                    SettingSwitchItem(
                        title = "Dark Mode",
                        description = "Adjust app theme",
                        icon = Icons.Default.DarkMode,
                        isChecked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }

            // --- App Info ---
            item {
                SettingsGroup(title = "About") {
                    SettingItem(
                        title = "App Version",
                        description = uiState.appVersion,
                        icon = Icons.Default.Info,
                        onClick = { }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// --- Modern M3 Components ---

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            color = PrimaryDarkGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 1.dp, // Subtle shadow for depth
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(description, color = Color.Gray, fontSize = 13.sp) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = PrimaryDarkGreen)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

@Composable
fun SettingSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(description, color = Color.Gray, fontSize = 13.sp) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = PrimaryDarkGreen)
        },
        trailingContent = {
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = PrimaryDarkGreen)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}