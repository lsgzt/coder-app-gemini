package com.pocketdev.app.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketdev.app.ui.navigation.AppNavigation
import com.pocketdev.app.ui.theme.PocketDevTheme
import com.pocketdev.app.viewmodels.EditorViewModel
import com.pocketdev.app.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDark = settingsViewModel.theme.let {
                val themeVal = runBlocking { it.first() }
                when (themeVal) {
                    "light" -> false
                    else -> true
                }
            }

            PocketDevTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}
