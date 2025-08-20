package com.creativeideas.batterymindai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.creativeideas.batterymindai.data.models.AIRecommendation
import com.creativeideas.batterymindai.ui.components.AIRecommendationCard
import com.creativeideas.batterymindai.ui.components.DischargeRateBar
import com.creativeideas.batterymindai.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BatteryMind AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refreshData() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BatteryStatusCard(
                        batteryLevel = uiState.currentBatteryLevel,
                        isCharging = uiState.isCharging,
                        timeRemaining = uiState.estimatedTimeRemaining,
                        health = uiState.batteryHealth
                    )
                }

                uiState.dischargeInfo?.let { dischargeInfo ->
                    item {
                        DischargeRateBar(
                            dischargeInfo = dischargeInfo,
                            showDetails = uiState.showDischargeDetails,
                            onToggleDetails = { viewModel.toggleDischargeDetails() }
                        )
                    }
                }

                item {
                    BatteryMetricsRow(
                        batteryScore = uiState.batteryScore,
                        temperature = uiState.batteryTemperature.toInt(),
                        health = uiState.batteryHealth
                    )
                }

                // FIX: Aggiunto di nuovo questo item
                item {
                    QuickActionsGrid(
                        onOptimizeClick = { viewModel.executeQuickAction("optimize") },
                        onPowerSaveClick = { viewModel.executeQuickAction("power_save") },
                        onAnalyzeClick = { viewModel.executeQuickAction("analyze") }
                    )
                }

                if (uiState.aiRecommendations.isNotEmpty()) {
                    item {
                        Text(
                            text = "🤖 AI Recommendations",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }

                    items(uiState.aiRecommendations) { recommendation ->
                        AIRecommendationCard(
                            recommendation = recommendation,
                            onAccept = { viewModel.executeRecommendation(it) },
                            onDismiss = { viewModel.dismissRecommendation(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryStatusCard(
    batteryLevel: Int,
    isCharging: Boolean,
    timeRemaining: String,
    health: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Battery Status",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$batteryLevel%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isCharging) "Charging" else timeRemaining,
                        color = if (isCharging) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        text = health,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(batteryLevel / 100f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = when {
                                    batteryLevel > 50 -> listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                                    batteryLevel > 20 -> listOf(Color(0xFFFF9800), Color(0xFFFFC107))
                                    else -> listOf(Color(0xFFF44336), Color(0xFFFF5722))
                                }
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun BatteryMetricsRow(
    batteryScore: Int,
    temperature: Int,
    health: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            title = "Score",
            value = batteryScore.toString(),
            color = when {
                batteryScore >= 80 -> Color(0xFF4CAF50)
                batteryScore >= 60 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "Temp",
            value = "${temperature}°C",
            color = when {
                temperature > 40 -> Color(0xFFF44336)
                temperature > 35 -> Color(0xFFFF9800)
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "Health",
            value = health,
            color = when (health.lowercase()) {
                "good" -> Color(0xFF4CAF50)
                "fair" -> Color(0xFFFF9800)
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    onOptimizeClick: () -> Unit,
    onPowerSaveClick: () -> Unit,
    onAnalyzeClick: () -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Tune,
                label = "Optimize",
                onClick = onOptimizeClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.PowerSettingsNew,
                label = "Power Save",
                onClick = onPowerSaveClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.Analytics,
                label = "Analyze",
                onClick = onAnalyzeClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}