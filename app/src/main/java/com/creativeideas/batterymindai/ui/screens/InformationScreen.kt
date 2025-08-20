package com.creativeideas.batterymindai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.creativeideas.batterymindai.ui.viewmodels.InformationViewModel

@Composable
fun InformationScreen(
    viewModel: InformationViewModel = hiltViewModel()
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Device & System Info",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                InfoCard(title = "Device Information") {
                    InfoRow("Model", deviceInfo.model)
                    InfoRow("Android Version", deviceInfo.androidVersion)
                    InfoRow("API Level", deviceInfo.apiLevel)
                }
            }
            item {
                InfoCard(title = "Battery Specifications") {
                    InfoRow("Current Level", batteryInfo.level)
                    InfoRow("Voltage", batteryInfo.voltage)
                    InfoRow("Temperature", batteryInfo.temperature)
                    InfoRow("Technology", batteryInfo.technology)
                    InfoRow("Health", batteryInfo.health)
                }
            }
            item {
                InfoCard(title = "System Information") {
                    InfoRow("Total RAM", systemInfo.totalRam)
                    InfoRow("Available RAM", systemInfo.availableRam)
                    InfoRow("CPU Cores", systemInfo.cpuCores)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}