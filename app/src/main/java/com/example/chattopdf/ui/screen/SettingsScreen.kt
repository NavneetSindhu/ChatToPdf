package com.example.chattopdf.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chattopdf.ui.components.PrimaryDarkGreen
import com.example.chattopdf.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Collect the dynamic background color so the settings screen matches the theme
    val screenBackground by viewModel.screenBgFlow.collectAsState(initial = Color(0xFFE9E9DD))

    // State for the Custom Color Picker Dialog
    var showColorPicker by remember { mutableStateOf(false) }

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
                    containerColor = screenBackground
                )
            )
        },
        containerColor = screenBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // --- Personalization (Theme Selection) ---
            item {
                SettingsGroup(title = "App Theme") {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select your preferred workspace color", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            // Preset 1: Default Paper
                            ThemePresetCircle(Color(0xFFE9E9DD)) {
                                viewModel.updateTheme(
                                    screenBg = Color(0xFFE9E9DD),
                                    profileBg = Color(0xFF080A09),
                                    border = Color(0xFF000000)
                                )
                            }

                            // Preset 2: Dark Slate
                            ThemePresetCircle(Color(0xFF1E1E1E)) {
                                viewModel.updateTheme(
                                    screenBg = Color(0xFF1E1E1E),
                                    profileBg = Color(0xFF4CAF50),
                                    border = Color(0xFF333333)
                                )
                            }

                            // Preset 3: Soft Blue
                            ThemePresetCircle(Color(0xFFF0F4F8)) {
                                viewModel.updateTheme(
                                    screenBg = Color(0xFFF0F4F8),
                                    profileBg = Color(0xFF1976D2),
                                    border = Color(0xFFB0BEC5)
                                )
                            }

                            // Custom Color Button (Rainbow gradient)
                            val rainbowBrush = Brush.sweepGradient(
                                listOf(Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow, Color.Red)
                            )
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(rainbowBrush)
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable { showColorPicker = true },
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Custom", tint = Color.White)
                            }
                        }
                    }
                }
            }

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

    // --- Custom Color Picker Dialog ---
    if (showColorPicker) {
        CustomColorPickerDialog(
            initialColor = screenBackground,
            onDismiss = { showColorPicker = false },
            onColorSelected = { selectedColor ->
                // Apply the custom color.
                // We use generic dark gray borders/profiles to match any custom color safely.
                viewModel.updateTheme(
                    screenBg = selectedColor,
                    profileBg = PrimaryDarkGreen,
                    border = Color.DarkGray
                )
                showColorPicker = false
            }
        )
    }
}

// --- Modern M3 Components ---

@Composable
fun CustomColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }

    val currentColor = Color(red, green, blue)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Theme", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // Color Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentColor)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // RGB Sliders
                Text("Red", fontSize = 12.sp, color = Color.Gray)
                Slider(
                    value = red,
                    onValueChange = { red = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                )

                Text("Green", fontSize = 12.sp, color = Color.Gray)
                Slider(
                    value = green,
                    onValueChange = { green = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                )

                Text("Blue", fontSize = 12.sp, color = Color.Gray)
                Slider(
                    value = blue,
                    onValueChange = { blue = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(currentColor) }) {
                Text("Apply Theme", color = PrimaryDarkGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun ThemePresetCircle(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(color)
            .border(2.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
    )
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
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
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingItem(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(description, color = Color.Gray, fontSize = 13.sp) },
        leadingContent = { Icon(icon, contentDescription = null, tint = PrimaryDarkGreen) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

@Composable
fun SettingSwitchItem(
    title: String, description: String, icon: ImageVector,
    isChecked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(description, color = Color.Gray, fontSize = 13.sp) },
        leadingContent = { Icon(icon, contentDescription = null, tint = PrimaryDarkGreen) },
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