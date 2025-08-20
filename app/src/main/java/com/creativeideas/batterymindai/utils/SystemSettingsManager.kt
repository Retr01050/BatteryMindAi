package com.creativeideas.batterymindai.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.DataOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) {
    private suspend fun executeShellCommand(command: String): Boolean = withContext(Dispatchers.IO) {
        val accessMode = appPreferences.getAccessMode()
        if (accessMode == "No Root") return@withContext false

        return@withContext try {
            val process: Process = if (accessMode == "Root") {
                val p = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(p.outputStream)
                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()
                p
            } else {
                // Per ora, non implementiamo l'esecuzione di comandi via Shizuku
                // poiché è complessa. Se si seleziona Shizuku, ci affideremo ai metodi standard.
                return@withContext false
            }
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun setBrightness(brightness: Int): Boolean {
        val clamped = brightness.coerceIn(0, 255)
        if (executeShellCommand("settings put system screen_brightness $clamped")) {
            return true
        }
        if (!canWriteSystemSettings()) {
            requestWriteSettingsPermission()
            return false
        }
        return try {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, clamped)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun setScreenTimeout(timeoutMs: Int): Boolean {
        if (executeShellCommand("settings put system screen_off_timeout $timeoutMs")) {
            return true
        }
        if (!canWriteSystemSettings()) {
            requestWriteSettingsPermission()
            return false
        }
        return try {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeoutMs)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun enablePowerSaveMode(): Boolean = withContext(Dispatchers.IO) {
        if (executeShellCommand("settings put global low_power 1")) {
            return@withContext true
        }
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (powerManager.isPowerSaveMode) return@withContext true
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun setLocationEnabled(enabled: Boolean): Boolean {
        val mode = if (enabled) 3 else 0 // 3 = HIGH_ACCURACY, 0 = OFF
        if (executeShellCommand("settings put secure location_mode $mode")) {
            return true
        }
        return try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true // Restituisce true perché l'azione di guida dell'utente è riuscita
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Suppress("DEPRECATION")
    suspend fun setWifiEnabled(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val command = "svc wifi " + if (enabled) "enable" else "disable"
        if (executeShellCommand(command)) {
            return@withContext true
        }
        return@withContext try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = enabled
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun enableDoNotDisturb(): Boolean = withContext(Dispatchers.IO) {
        val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nManager.isNotificationPolicyAccessGranted) {
            requestDndPermission()
            return@withContext false
        }
        return@withContext try {
            nManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun setAirplaneModeEnabled(enabled: Boolean): Boolean {
        val value = if (enabled) 1 else 0
        val broadcastState = if (enabled) "true" else "false"
        val command = "settings put global airplane_mode_on $value && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state $broadcastState"
        if (executeShellCommand(command)) {
            return true
        }
        return try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true // Restituisce true perché l'azione di guida dell'utente è riuscita
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun canWriteSystemSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    fun requestWriteSettingsPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun requestDndPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}