package com.creativeideas.batterymindai.data.models

data class BatteryStats(
    val level: Int,
    val temperature: Float,
    val voltage: Float,
    val health: String,
    val technology: String,
    val isCharging: Boolean,
    val chargingType: String,
    val estimatedTimeRemaining: Long,
    val batteryScore: Int,
    val timestamp: Long = System.currentTimeMillis()
)
