package com.creativeideas.batterymindai.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.creativeideas.batterymindai.MainActivity
import com.creativeideas.batterymindai.ui.onboarding.screens.OnboardingNavigation
import com.creativeideas.batterymindai.ui.theme.BatteryMindAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // 1. Creiamo uno stato per gestire la scelta del tema in tempo reale.
            //    Inizialmente è null per usare il tema di sistema.
            var isDarkTheme by remember { mutableStateOf<Boolean?>(null) }

            // 2. Usiamo il nostro stato per impostare il tema.
            //    Se lo stato è null, usa il tema di sistema, altrimenti usa la scelta dell'utente.
            BatteryMindAITheme(darkTheme = isDarkTheme ?: isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OnboardingNavigation(
                        onFinish = {
                            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                            finish()
                        },
                        // 3. Passiamo la funzione per cambiare tema alla navigazione.
                        onThemeChanged = { isDark -> isDarkTheme = isDark }
                    )
                }
            }
        }
    }
}