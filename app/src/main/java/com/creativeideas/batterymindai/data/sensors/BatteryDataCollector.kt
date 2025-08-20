package com.creativeideas.batterymindai.data.sensors

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Singleton
class BatteryDataCollector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getCurrentBatteryStats(): BatteryStatsEntity = withContext(Dispatchers.IO) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (scale > 0) (level * 100 / scale) else 0

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = temperature / 10.0f

        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val healthInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        val plugTypeInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val currentNow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } else {
            0
        }

        val batteryScore = calculateBatteryScore(batteryPct, tempCelsius, healthInt, isCharging, voltage, currentNow)

        BatteryStatsEntity(
            timestamp = Date(),
            batteryLevel = batteryPct,
            isCharging = isCharging,
            temperature = tempCelsius,
            voltage = voltage,
            health = getBatteryHealthString(healthInt),
            technology = technology,
            plugType = getChargingTypeString(plugTypeInt),
            screenOnTime = 0L,
            cpuUsage = 0f,
            networkUsage = 0L,
            batteryScore = batteryScore
        )
    }

    private fun calculateBatteryScore(level: Int, temp: Float, health: Int, isCharging: Boolean, voltage: Int, current: Int): Int {
        var score = 100
        score -= when {
            level < 10 -> 30; level < 20 -> 20; level < 30 -> 10; else -> 0
        }
        score -= when {
            temp > 45 -> 25; temp > 40 -> 15; temp < 0 -> 20; else -> 0
        }
        score -= when (health) {
            BatteryManager.BATTERY_HEALTH_DEAD -> 20; BatteryManager.BATTERY_HEALTH_OVERHEAT -> 15; else -> 0
        }
        if (isCharging && level > 95) score -= 5
        return max(0, min(100, score))
    }

    private fun getBatteryHealthString(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"; BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat";
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"; BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage";
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"; BatteryManager.BATTERY_HEALTH_COLD -> "Cold";
            else -> "Unknown"
        }
    }

    private fun getChargingTypeString(plugType: Int): String {
        return when (plugType) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"; BatteryManager.BATTERY_PLUGGED_USB -> "USB";
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"; else -> "Unplugged"
        }
    }
}