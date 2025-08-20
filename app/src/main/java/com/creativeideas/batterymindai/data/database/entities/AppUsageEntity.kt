package com.creativeideas.batterymindai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Date,
    val usageTime: Long,
    val batteryUsage: Float,
    val networkUsage: Long,
    val cpuUsage: Float,
    val isSystemApp: Boolean
)
