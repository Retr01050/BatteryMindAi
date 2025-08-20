package com.creativeideas.batterymindai.ui.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.creativeideas.batterymindai.ui.onboarding.screens.AiChoiceScreen

@Composable
fun OnboardingNavigation(
    onFinish: () -> Unit,
    onThemeChanged: (Boolean) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "access_mode",
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // FIX: Aggiunto nuovo composable per la scelta della modalità
        composable("access_mode") {
            AccessModeScreen(
                onNext = {
                    navController.navigate("welcome")
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onNext = {
                    navController.navigate("features")
                }
            )
        }

        composable("features") {
            FeaturesScreen(
                onNext = {
                    navController.navigate("customization")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("customization") {
            CustomizationScreen(
                onNext = {
                    navController.navigate("ai_choice") // <-- MODIFICA: Va alla scelta dell'IA
                },
                onBack = { navController.popBackStack() },
                onThemeChanged = onThemeChanged
            )
        }

        // NUOVA SCHERMATA NEL FLUSSO
        composable("ai_choice") {
            AiChoiceScreen(
                onNext = {
                    navController.navigate("permissions")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("permissions") {
            PermissionsScreen(
                onFinish = onFinish,
                onBack = { navController.popBackStack() }
            )
        }
    }
}