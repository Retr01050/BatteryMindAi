package com.creativeideas.batterymindai.data.models

// FIX: Renamed DischargeStatus to DischargeLevel to match the existing enum.
// FIX: Corrected estimatedTimeRemaining to be a String as expected by UI.
data class DischargeInfo(
    val currentRate: Float,
    val averageRate: Float,
    val estimatedTimeRemaining: String, // Deve essere String
    val status: DischargeLevel,
    val backgroundActivities: List<BackgroundActivity>,
    val activeSettings: List<ActiveSetting>,
    val cpuUsage: Float,
    val gpuUsage: Float,
    val networkUsage: NetworkUsage,
    val screenBrightness: Int
)

// This was likely intended instead of the non-existent 'DischargeStatus'
enum class DischargeLevel {
    LOW, MEDIUM, HIGH
}

data class BackgroundActivity(
    val appName: String,
    val priority: String,
    val powerConsumption: Float,
    val description: String
)

data class ActiveSetting(
    val name: String,
    val impact: String,
    val description: String
)

data class NetworkUsage(
    val wifiUsage: Float,
    val mobileDataUsage: Float,
    val bluetoothUsage: Float
)