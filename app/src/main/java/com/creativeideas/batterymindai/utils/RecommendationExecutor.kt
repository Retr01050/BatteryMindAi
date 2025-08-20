package com.creativeideas.batterymindai.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.creativeideas.batterymindai.data.models.AIRecommendation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
// FIX: Aggiunti gli import mancanti, che sono la causa di tutti gli errori.
import android.provider.Settings

@Singleton
class RecommendationExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemSettingsManager: SystemSettingsManager,
    private val batteryOptimizer: BatteryOptimizer
) {

    suspend fun executeRecommendation(recommendation: AIRecommendation): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                // ... (il resto della logica when rimane invariato, ora compilerà correttamente)
                when (recommendation.action) {
                    "enable_power_save" -> { // Aggiunto per coerenza con OfflineReasoningEngine
                        val result = systemSettingsManager.enablePowerSaveMode()
                        if (result) {
                            showToast("✅ Power save mode settings opened")
                        } else {
                            showToast("⚠️ Could not open power save settings")
                        }
                        result
                    }

                    "set_brightness_10" -> {
                        val result = systemSettingsManager.setBrightness(25)
                        if (result) {
                            showToast("✅ Screen brightness reduced to 10%")
                        } else {
                            showToast("⚠️ Please grant Write Settings permission")
                        }
                        result
                    }

                    "disable_location" -> {
                        val result = systemSettingsManager.setLocationEnabled(false)
                        if (result) {
                            showToast("✅ Location services disabled")
                        } else {
                            showToast("⚠️ Please disable location services manually (requires ADB/root)")
                        }
                        result
                    }

                    "disable_hotspot" -> {
                        showToast("📱 Please manually disable mobile hotspot in settings")
                        openHotspotSettings()
                        true
                    }

                    "enable_wifi" -> {
                        val result = systemSettingsManager.setWifiEnabled(true)
                        if (result) {
                            showToast("✅ Wi-Fi enabled")
                        } else {
                            showToast("⚠️ Please enable Wi-Fi manually")
                        }
                        result
                    }

                    "enable_night_mode" -> {
                        val result1 = systemSettingsManager.enableDoNotDisturb()
                        val result2 = systemSettingsManager.setBrightness(50)
                        val success = result1 || result2
                        if (success) {
                            showToast("✅ Night mode optimizations applied")
                        } else {
                            showToast("⚠️ Please grant necessary permissions manually")
                        }
                        success
                    }

                    "thermal_optimize" -> {
                        systemSettingsManager.setBrightness(100)
                        systemSettingsManager.enablePowerSaveMode()
                        showToast("✅ Thermal optimization applied")
                        true
                    }

                    "emergency_cooling" -> {
                        showToast("🚨 Emergency cooling: Stop charging and close apps!")
                        openBatterySettings()
                        true
                    }


                    "set_airplane_mode" -> {
                        val result = systemSettingsManager.setAirplaneModeEnabled(true)
                        if (result) {
                            showToast("✅ Airplane mode enabled")
                        } else {
                            showToast("⚠️ Please enable airplane mode manually (requires ADB/root)")
                        }
                        result
                    }

                    "optimize_charging_pattern" -> {
                        showToast("💡 Tip: Charge between 20-80% for better battery health")
                        true
                    }

                    "temperature_management_tips" -> {
                        showToast("🌡️ Tip: Keep device cool and remove case while charging")
                        true
                    }

                    "notify_unplug" -> {
                        showToast("🔌 Consider unplugging the charger to preserve battery health")
                        true
                    }

                    "enable_thermal_charging" -> {
                        showToast("⚡ Tip: Switch to a slower charger or improve ventilation")
                        true
                    }

                    "prepare_for_low_battery" -> {
                        batteryOptimizer.optimizeBattery()
                        showToast("🔋 Battery optimization applied for extended usage")
                        true
                    }

                    "suggest_charging" -> {
                        showToast("🔌 Consider charging your device soon")
                        true
                    }

                    "suggest_optimal_charging" -> {
                        showToast("⚡ This is a good time to charge for tomorrow's usage")
                        true
                    }

                    else -> {
                        if (recommendation.action?.startsWith("optimize_app_") == true) {
                            val packageName = recommendation.action.substringAfter("optimize_app_")
                            showToast("📱 Please optimize $packageName in app settings")
                            openAppSettings(packageName)
                            true
                        } else {
                            showToast("Manual action required: ${recommendation.description}")
                            false
                        }
                    }
                }
            } catch (e: Exception) {
                showToast("❌ Failed to execute action: ${e.message}")
                e.printStackTrace() // FIX: Usa il parametro 'e'
                false
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun openHotspotSettings() {
        try {
            // FIX: Ora Intent, action, e addFlags vengono risolti grazie agli import
            val intent = Intent("android.settings.WIFI_AP_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openWifiSettings()
        }
    }

    private fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Could not open Wi-Fi settings.")
        }
    }

    private fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openAppSettings(context.packageName)
        }
    }

    // FIX: Queste funzioni ora compileranno correttamente grazie agli import
    private fun openAppSettings(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:$packageName".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Please find the app in Settings > Apps", Toast.LENGTH_SHORT).show()
        }
    }

    // Le funzioni seguenti avranno ancora il warning "is never used" finché
    // non verranno chiamate da qualche altra parte, ma non sono più errori di compilazione.
    fun getRequiredPermissions(action: String): List<String> {
        return when (action) {
            "set_brightness_10", "enable_night_mode" -> listOf("WRITE_SETTINGS")
            "disable_location" -> listOf("WRITE_SECURE_SETTINGS")
            "enable_ultra_power_save", "thermal_optimize" -> listOf("WRITE_SECURE_SETTINGS", "ROOT")
            "enable_wifi", "disable_hotspot", "set_airplane_mode" -> listOf("ROOT", "SHIZUKU")
            else -> emptyList()
        }
    }

    fun canWriteSystemSettings(): Boolean {
        return Settings.System.canWrite(context)
    }
}