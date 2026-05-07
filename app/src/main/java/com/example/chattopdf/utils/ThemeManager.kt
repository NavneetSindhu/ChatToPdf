package com.example.chattopdf.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// MUST be at the very top of the file, outside the class!
private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

class ThemeManager(private val context: Context) {

    companion object {
        // Changed to intPreferencesKey
        val SCREEN_BG_KEY = intPreferencesKey("screen_bg_v2")
        val PROFILE_BG_KEY = intPreferencesKey("profile_bg_v2")
        val BORDER_COLOR_KEY = intPreferencesKey("border_color_v2")

        val DefaultScreenBg = Color(0xFFE9E9DD)
        val DefaultProfileBg = Color(0xFF080A09)
        val DefaultBorder = Color(0xFF000000)
    }

    // Convert saved Int back to Compose Color
    val screenBgFlow: Flow<Color> = context.dataStore.data.map { prefs ->
        Color(prefs[SCREEN_BG_KEY] ?: DefaultScreenBg.toArgb())
    }

    val profileBgFlow: Flow<Color> = context.dataStore.data.map { prefs ->
        Color(prefs[PROFILE_BG_KEY] ?: DefaultProfileBg.toArgb())
    }

    val borderColorFlow: Flow<Color> = context.dataStore.data.map { prefs ->
        Color(prefs[BORDER_COLOR_KEY] ?: DefaultBorder.toArgb())
    }

    // Save Compose Color as an Int (ARGB)
    suspend fun saveColors(screenBg: Color, profileBg: Color, border: Color) {
        context.dataStore.edit { prefs ->
            prefs[SCREEN_BG_KEY] = screenBg.toArgb()
            prefs[PROFILE_BG_KEY] = profileBg.toArgb()
            prefs[BORDER_COLOR_KEY] = border.toArgb()
        }
    }
}