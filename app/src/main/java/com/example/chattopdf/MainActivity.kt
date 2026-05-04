package com.example.chattopdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chattopdf.navigation.Screen
import com.example.chattopdf.ui.components.ChatScreen
import com.example.chattopdf.ui.screen.SettingsScreen
import com.example.chattopdf.ui.theme.ChatToPdfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This makes the app go under the status bar and navigation bar
        enableEdgeToEdge()

        setContent {
            ChatToPdfTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.Chat) {
                        composable(Screen.Chat) {
                            ChatScreen(
                                paddingValues = innerPadding,
                                onNavigateToSettings = { navController.navigate(Screen.Settings) }
                            )
                        }
                        composable(Screen.Settings) {
                            SettingsScreen(onBackClick = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}