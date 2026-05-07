package com.example.chattopdf.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattopdf.utils.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Define the State (Removed isDarkMode since ThemeManager handles colors now)
data class SettingsUiState(
    val defaultPageSize: String = "A4",
    val ocrEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = false,
    val cacheSizeMB: Double = 12.4,
    val appVersion: String = "1.0.4-beta"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize ThemeManager using the Application Context
    private val themeManager = ThemeManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Expose Theme Flows so the Settings Screen can also react to color changes
    val screenBgFlow = themeManager.screenBgFlow

    // --- Update Functions ---

    fun updateTheme(screenBg: Color, profileBg: Color, border: Color) {
        viewModelScope.launch {
            themeManager.saveColors(screenBg, profileBg, border)
        }
    }

    fun toggleOcr(enabled: Boolean) {
        _uiState.update { it.copy(ocrEnabled = enabled) }
    }

    fun toggleCloudSync(enabled: Boolean) {
        // Normally trigger Google Drive Auth here
        _uiState.update { it.copy(cloudSyncEnabled = enabled) }
    }

    fun updatePageSize(size: String) {
        _uiState.update { it.copy(defaultPageSize = size) }
    }

    fun clearCache() {
        // Logic to clear local temporary PDF files
        _uiState.update { it.copy(cacheSizeMB = 0.0) }
    }
}