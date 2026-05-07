package com.example.chattopdf.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. Define the State
data class SettingsUiState(
    val defaultPageSize: String = "A4",
    val ocrEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = false,
    val cacheSizeMB: Double = 12.4,
    val isDarkMode: Boolean = false,
    val appVersion: String = "1.0.4-beta"
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // --- Update Functions ---

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleOcr(enabled: Boolean) {
        _uiState.update { it.copy(ocrEnabled = enabled) }
    }

    fun toggleCloudSync(enabled: Boolean) {
        // Here you would normally trigger Google Drive Auth
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