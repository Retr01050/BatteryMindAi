package com.creativeideas.batterymindai.ui.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.creativeideas.batterymindai.ui.onboarding.viewmodels.CustomizationViewModel
import kotlinx.coroutines.delay

@Composable
fun CustomizationScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onThemeChanged: (Boolean) -> Unit,
    viewModel: CustomizationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    // FIX: Rimosso Modifier.background(...) per usare il colore del tema dal Surface genitore.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    // Usa i colori del tema corrente
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(animationSpec = tween(800))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Customize Your Experience",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Choose your preferred theme and settings",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) + slideInVertically(animationSpec = tween(800, delayMillis = 400))
        ) {
            Column {
                Text(
                    text = "Choose Theme",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ThemeCard(
                        title = "Dark Theme",
                        description = "Easy on the eyes",
                        icon = Icons.Default.DarkMode,
                        isSelected = !uiState.isLightTheme,
                        onClick = {
                            viewModel.setLightTheme(false)
                            onThemeChanged(true)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ThemeCard(
                        title = "Light Theme",
                        description = "Clean and bright",
                        icon = Icons.Default.LightMode,
                        isSelected = uiState.isLightTheme,
                        onClick = {
                            viewModel.setLightTheme(true)
                            onThemeChanged(false)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800, delayMillis = 600)) + slideInVertically(animationSpec = tween(800, delayMillis = 600))
        ) {
            Column {
                Text(
                    text = "Preferences",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        PreferenceRow(
                            title = "Enable Notifications",
                            description = "Get battery alerts and tips",
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = viewModel::setNotificationsEnabled
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PreferenceRow(
                            title = "Auto Optimization",
                            description = "Automatically apply battery optimizations",
                            checked = uiState.autoOptimizeEnabled,
                            onCheckedChange = viewModel::setAutoOptimizeEnabled
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PreferenceRow(
                            title = "Advanced Monitoring",
                            description = "Detailed battery and system monitoring",
                            checked = uiState.advancedMonitoringEnabled,
                            onCheckedChange = viewModel::setAdvancedMonitoringEnabled
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800, delayMillis = 800)) + slideInVertically(animationSpec = tween(800, delayMillis = 800))
        ) {
            Button(
                onClick = {
                    viewModel.savePreferences()
                    onNext()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .then(if (isSelected) Modifier.border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp)) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}