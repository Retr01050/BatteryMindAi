package com.creativeideas.batterymindai.ui.onboarding.screens

// Rimuovi l'import per rememberLauncherForActivityResult se non serve altrove
// import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.creativeideas.batterymindai.ui.onboarding.viewmodels.AccessModeViewModel
import kotlinx.coroutines.delay
import rikka.shizuku.Shizuku


@Composable
fun AccessModeScreen(
    onNext: () -> Unit,
    viewModel: AccessModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    // FIX DEFINITIVO: Usiamo il listener di Shizuku che è più stabile
    val shizukuPermissionListener = remember {
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            // Quando riceviamo una risposta, aggiorniamo la UI
            if (requestCode == AccessModeViewModel.SHIZUKU_PERMISSION_REQUEST_CODE) {
                viewModel.checkAccessModes()
            }
        }
    }

    // Aggiungiamo e rimuoviamo il listener in base al ciclo di vita del Composable
    DisposableEffect(Unit) {
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        onDispose {
            Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
        viewModel.checkAccessModes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(animationSpec = tween(800))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF4A9EFF), modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Choose Access Mode", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Higher access allows for more powerful optimizations.", color = Color(0xFF9CA3AF), fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(visible = isVisible, enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) + slideInVertically(animationSpec = tween(800, delayMillis = 400))) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AccessModeCard(
                    title = "Root",
                    description = "Maximum control. Automatic system changes.",
                    isSelected = uiState.selectedMode == "Root",
                    isEnabled = uiState.isRootAvailable,
                    onClick = { viewModel.selectMode("Root") }
                )
                AccessModeCard(
                    title = "Shizuku",
                    description = "High control via ADB. No root needed.",
                    isSelected = uiState.selectedMode == "Shizuku",
                    isEnabled = uiState.isShizukuAvailable,
                    onClick = {
                        viewModel.selectMode("Shizuku")
                        if (!uiState.isShizukuPermissionGranted) {
                            // Questo è il metodo standard per chiedere il permesso
                            Shizuku.requestPermission(AccessModeViewModel.SHIZUKU_PERMISSION_REQUEST_CODE)
                        }
                    }
                )
                AccessModeCard(
                    title = "No Root",
                    description = "Standard functionality. Manual actions required.",
                    isSelected = uiState.selectedMode == "No Root",
                    isEnabled = true,
                    onClick = { viewModel.selectMode("No Root") }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = isVisible, enter = fadeIn(animationSpec = tween(800, delayMillis = 800)) + slideInVertically(animationSpec = tween(800, delayMillis = 800))) {
            Button(
                onClick = {
                    viewModel.saveAccessMode()
                    onNext()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.selectedMode.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A9EFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AccessModeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick)
            .then(if (isSelected) Modifier.border(width = 2.dp, color = Color(0xFF4A9EFF), shape = RoundedCornerShape(16.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF4A9EFF).copy(alpha = 0.1f) else Color(0xFF2A2D3A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = if (isEnabled) Color.White else Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, color = if (isEnabled) Color(0xFF9CA3AF) else Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
            }
            if (isEnabled) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4A9EFF), unselectedColor = Color.Gray)
                )
            } else {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Unavailable", tint = Color.Gray)
            }
        }
    }
}