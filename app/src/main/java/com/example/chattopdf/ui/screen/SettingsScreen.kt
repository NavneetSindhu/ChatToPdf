package com.example.chattopdf.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chattopdf.ui.components.PrimaryDarkGreen
import com.example.chattopdf.ui.components.ScreenBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // --- Document Settings ---
            item { SettingHeader("Scanning & PDF") }
            item {
                SettingItem(
                    title = "Default Page Size",
                    description = "A4",
                    icon = Icons.Default.Description
                )
            }
            item {
                SettingItem(
                    title = "OCR (Text Recognition)",
                    description = "Enabled (English)",
                    icon = Icons.Default.Translate
                )
            }

            // --- Storage & Backup ---
            item { SettingHeader("Storage") }
            item {
                SettingItem(
                    title = "Cloud Sync",
                    description = "Google Drive (Disconnected)",
                    icon = Icons.Default.CloudUpload
                )
            }
            item {
                SettingItem(
                    title = "Clear Cache",
                    description = "Current size: 12MB",
                    icon = Icons.Default.DeleteSweep
                )
            }

            // --- Personalization ---
            item { SettingHeader("Interface") }
            item {
                ListItem(
                    headlineContent = { Text("Dark Mode") },
                    leadingContent = { Icon(Icons.Default.DarkMode, null) },
                    trailingContent = { Switch(checked = false, onCheckedChange = {}) }
                )
            }

            // --- App Info ---
            item { SettingHeader("About") }
            item {
                SettingItem(
                    title = "App Version",
                    description = "1.0.4-beta",
                    icon = Icons.Default.Info
                )
            }
        }
    }
}

@Composable
fun SettingHeader(title: String) {
    Text(
        text = title,
        color = PrimaryDarkGreen,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingItem(title: String, description: String, icon: ImageVector) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description, color = Color.Gray) },
        leadingContent = { Icon(icon, contentDescription = null, tint = PrimaryDarkGreen) },
        modifier = Modifier.clickable { /* Add Logic */ }
    )
}