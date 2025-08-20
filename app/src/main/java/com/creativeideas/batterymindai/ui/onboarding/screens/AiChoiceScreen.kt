package com.creativeideas.batterymindai.ui.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.creativeideas.batterymindai.ui.onboarding.viewmodels.AiChoiceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AiChoiceScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: AiChoiceViewModel = hiltViewModel()
) {
    var selectedMode by remember { mutableStateOf("BASE") }
    var isVisible by remember { mutableStateOf(false) }
    var showMobileDataDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    LaunchedEffect(viewModel.uiEvents) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is AiChoiceViewModel.UiEvent.NavigateToNextScreen -> onNext()
                is AiChoiceViewModel.UiEvent.ShowMobileDataDialog -> showMobileDataDialog = true
            }
        }
    }

    if (showMobileDataDialog) {
        MobileDataDownloadDialog(
            onConfirm = {
                viewModel.startMobileDownload()
                showMobileDataDialog = false
            },
            onDismiss = {
                showMobileDataDialog = false
                onNext()
            }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(tween(800))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Scegli il tuo Motore AI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Seleziona il livello di intelligenza per la tua app.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(48.dp))

        // Scelte
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800, 400)) + slideInVertically(tween(800, 400))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ChoiceCard(
                    title = "Consigli Intelligenti",
                    description = "Ottieni subito suggerimenti efficaci basati su regole. Leggero e immediato.",
                    icon = Icons.Default.Lightbulb,
                    isSelected = selectedMode == "BASE",
                    onClick = { selectedMode = "BASE" }
                )
                ChoiceCard(
                    title = "Assistente Proattivo",
                    description = "Scarica il nostro cervello AI per consigli unici e personalizzati che si adattano a te.",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = selectedMode == "ADVANCED",
                    onClick = { selectedMode = "ADVANCED" }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pulsante Continua
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800, 800)) + slideInVertically(tween(800, 800))
        ) {
            Button(
                onClick = {
                    viewModel.selectAIMode(selectedMode)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continua", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MobileDataDownloadDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download su Rete Mobile") },
        text = { Text("Il modello AI è grande (circa 2.4 GB). Sei sicuro di volerlo scaricare utilizzando la tua connessione dati mobile?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Sì, scarica")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun ChoiceCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}