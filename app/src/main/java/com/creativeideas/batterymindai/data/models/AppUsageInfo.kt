package com.creativeideas.batterymindai.data.models

data class AppUsageInfo(
    val appName: String,
    val packageName: String,
    val powerConsumption: Float,      // Deve essere Float
    val usageTime: Long,
    val batteryPercentage: Float,
    val isRunningInBackground: Boolean, // Corretto il typo da 'ln' a 'In'
    val isSystemApp: Boolean
)