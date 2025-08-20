package com.creativeideas.batterymindai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.ui.navigation.BatteryMindNavigation
import com.creativeideas.batterymindai.ui.onboarding.OnboardingActivity
import com.creativeideas.batterymindai.ui.theme.BatteryMindAITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            if (!appPreferences.isOnboardingCompleted()) {
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                finish()
                return@launch
            }
            // --- LOGICA DI DOWNLOAD RIMOSSA DA QUI ---
            // L'app si avvia e basta. La responsabilità del download è
            // interamente delegata alle interazioni dell'utente nelle Impostazioni.

            setContent {
                val isLightTheme by appPreferences.isLightThemeFlow().collectAsState(initial = false)
                LaunchedEffect(isLightTheme) {
                    configureStatusBar(isLightTheme)
                }
                BatteryMindAITheme(darkTheme = !isLightTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        BatteryMindNavigation()
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun configureStatusBar(isLightTheme: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = isLightTheme
        window.statusBarColor = if (isLightTheme) {
            Color.White.copy(alpha = 0.95f).value.toInt()
        } else {
            Color(0xFF1A1D29).value.toInt()
        }
    }
}