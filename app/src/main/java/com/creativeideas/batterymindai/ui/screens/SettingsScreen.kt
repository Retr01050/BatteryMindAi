package com.creativeideas.batterymindai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.creativeideas.batterymindai.data.models.AIModelStatus
import com.creativeideas.batterymindai.logic.workers.ModelDownloadWorker
import com.creativeideas.batterymindai.ui.viewmodels.SettingsViewModel
import com.creativeideas.batterymindai.ui.viewmodels.SettingsViewModel.UiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val modelStatus by viewModel.modelStatus.collectAsState()
    val downloadWorkInfo by viewModel.getDownloadProgress().collectAsState(initial = null)
    var showMobileDataDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowMobileDataDialog -> showMobileDataDialog = true
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (showMobileDataDialog) {
        AlertDialog(
            onDismissRequest = { showMobileDataDialog = false },
            title = { Text("Nessuna Connessione Wi-Fi") },
            text = { Text("Vuoi scaricare il modello AI (~2.2 GB) usando la tua connessione dati mobile? Potrebbero essere applicati costi aggiuntivi.") },
            confirmButton = {
                Button(
                    onClick = {
                        // [CORRETTO] Chiama la nuova funzione specifica per il download mobile
                        viewModel.startMobileDownload()
                        showMobileDataDialog = false
                    }
                ) { Text("Sì, usa dati mobili") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showMobileDataDialog = false }) { Text("Annulla") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Advanced AI Engine", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(20.dp))

                        when (val status = modelStatus) {
                            is AIModelStatus.NotDownloaded, is AIModelStatus.DownloadFailed -> {
                                Button(onClick = { viewModel.onDownloadRequest() }) {
                                    Text("Download Advanced AI")
                                }
                                if (status is AIModelStatus.DownloadFailed) {
                                    Text(
                                        "Download failed: ${status.reason}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                            is AIModelStatus.Downloading -> {
                                val progress = downloadWorkInfo?.progress?.getInt(ModelDownloadWorker.KEY_PROGRESS, 0) ?: 0
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                                    Text("Downloading... $progress%", modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                            is AIModelStatus.Ready -> {
                                if (status.version == "Verifying...") {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                        Text("Verifying file integrity...", modifier = Modifier.padding(top = 8.dp))
                                    }
                                } else {
                                    Text("✅ Advanced AI (v${status.version}) is active.", color = Color(0xFF4CAF50))
                                }
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
                        Text(text = "App Settings", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(20.dp))
                        SettingToggleItem(
                            title = "Dark Mode",
                            subtitle = "Use the dark theme",
                            isChecked = uiState.darkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SettingToggleItem(
                            title = "Notifications",
                            subtitle = "Battery alerts and recommendations",
                            isChecked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SettingToggleItem(
                            title = "Auto Optimize",
                            subtitle = "Run optimizations automatically in background (Root/Shizuku needed)",
                            isChecked = uiState.autoOptimize,
                            onCheckedChange = { viewModel.setAutoOptimize(it) },
                            enabled = uiState.accessMode != "No Root"
                        )
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
                        Text(text = "Battery Settings", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(20.dp))
                        SettingToggleItem(
                            title = "Battery Alerts",
                            subtitle = "Low battery and charging notifications",
                            isChecked = uiState.batteryAlerts,
                            onCheckedChange = { viewModel.setBatteryAlerts(it) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SettingToggleItem(
                            title = "Temperature Alerts",
                            subtitle = "Overheating warnings",
                            isChecked = uiState.temperatureAlerts,
                            onCheckedChange = { viewModel.setTemperatureAlerts(it) }
                        )
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
                        Text(text = "Alert Thresholds", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(20.dp))
                        SliderSetting(
                            title = "Low Battery Alert",
                            subtitle = "${uiState.lowBatteryThreshold}%",
                            value = uiState.lowBatteryThreshold.toFloat(),
                            valueRange = 5f..30f,
                            onValueChange = { viewModel.setLowBatteryThreshold(it.toInt()) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SliderSetting(
                            title = "High Temperature Alert",
                            subtitle = "${uiState.highTemperatureThreshold}°C",
                            value = uiState.highTemperatureThreshold.toFloat(),
                            valueRange = 35f..50f,
                            onValueChange = { viewModel.setHighTemperatureThreshold(it.toInt()) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SliderSetting(
                            title = "Update Interval",
                            subtitle = "${uiState.updateInterval} seconds",
                            value = uiState.updateInterval.toFloat(),
                            valueRange = 1f..120f,
                            onValueChange = { viewModel.setUpdateInterval(it.toInt()) }
                        )
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
                        Text(text = "Permissions", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(20.dp))
                        PermissionItem(
                            title = "Usage Stats",
                            subtitle = "Required for app usage analysis",
                            isGranted = uiState.hasUsageStatsPermission,
                            onClick = { viewModel.requestUsageStatsPermission() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PermissionItem(
                            title = "Battery Optimization",
                            subtitle = "Disable for accurate monitoring",
                            isGranted = uiState.isBatteryOptimizationDisabled,
                            onClick = { viewModel.requestBatteryOptimizationDisable() }
                        )
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
                        Text(text = "About", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(20.dp))
                        AboutInfoRow("Version", "1.0.0-alpha")
                        AboutInfoRow("Build", "20240521")
                        AboutInfoRow("Developer", "Creative Ideas")
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.exportBatteryData() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Export Battery Data", fontSize = 16.sp)
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.resetAllSettings() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Reset All Settings", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SliderSetting(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isGranted) "Granted" else "Grant",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AboutInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}