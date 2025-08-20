// C:\Users\Alessandro\AndroidStudioProjects\BatteryMind\app\src\main\java\com\creativeideas\batterymindai\data\database\entities\BatteryStatsEntity.kt

package com.creativeideas.batterymindai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "battery_stats")
data class BatteryStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Date,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val temperature: Float,
    val voltage: Int,
    // FIX: Cambiato il tipo in String per corrispondere a ciò che producono BatteryRepository e BatteryDataCollector.
    val health: String,
    val technology: String,
    // FIX: Cambiato il tipo in String per maggiore leggibilità e per corrispondere ai collettori.
    val plugType: String,
    val screenOnTime: Long,
    val cpuUsage: Float,
    val networkUsage: Long,
    val batteryScore: Int
)