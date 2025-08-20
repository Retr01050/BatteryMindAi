package com.creativeideas.batterymindai.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color // FIX: Aggiunto l'import mancante
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.models.AppUsageInfo
import com.creativeideas.batterymindai.ui.viewmodels.StatisticsViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Questo blocco riesegue il caricamento dei dati ogni volta
    // che l'utente torna a questa schermata.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text(
                    text = "Usage Statistics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Battery History (Last 24h)",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.batteryHistory.size > 1) {
                            BatteryLevelChart(
                                batteryHistory = uiState.batteryHistory,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Not enough historical data to show a chart.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Top 5 App Consumers",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        if (uiState.appUsageHistory.isNotEmpty()) {
                            uiState.appUsageHistory.forEach { app ->
                                AppUsageItem(app = app)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            Text(
                                "Usage stats permission not granted or no usage data available.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryLevelChart(
    batteryHistory: List<BatteryStatsEntity>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val path = Path()
        val reversedHistory = batteryHistory.reversed()
        val stepX = size.width / (reversedHistory.size - 1).coerceAtLeast(1)

        reversedHistory.forEachIndexed { index, stats ->
            val x = index * stepX
            val y = size.height - (stats.batteryLevel / 100f * size.height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            drawCircle(color = primaryColor, radius = 6f, center = Offset(x, y))
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4f)
        )
    }
}

@Composable
private fun AppUsageItem(app: AppUsageInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            val usageMinutes = app.usageTime / 60000
            if (usageMinutes > 0) {
                Text(
                    text = "$usageMinutes min used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        val consumptionText = if (app.powerConsumption > 1) {
            "${"%.1f".format(app.powerConsumption)} mAh"
        } else {
            "${"%.1f".format(app.batteryPercentage)} %"
        }
        Text(
            text = consumptionText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                app.batteryPercentage > 15 || app.powerConsumption > 100 -> MaterialTheme.colorScheme.error
                app.batteryPercentage > 8 || app.powerConsumption > 50 -> Color(0xFFFF9800) // Giallo/Arancione
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}