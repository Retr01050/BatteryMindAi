package com.creativeideas.batterymindai.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.creativeideas.batterymindai.data.models.AIModelStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Singleton
class AppPreferences @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- NUOVE PREFERENZE PER L'IA ON-DEVICE ---
    private val _aiModelStatus = MutableStateFlow(getAIModelStatus())
    private val _aiMode = MutableStateFlow(getString(KEY_AI_MODE, "BASE"))

    // --- StateFlow per aggiornamenti reattivi ---
    private val _isLightTheme = MutableStateFlow(getBoolean(KEY_LIGHT_THEME, false))
    private val _notificationsEnabled = MutableStateFlow(getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    private val _autoOptimizeEnabled = MutableStateFlow(getBoolean(KEY_AUTO_OPTIMIZE_ENABLED, false))
    private val _batteryAlertsEnabled = MutableStateFlow(getBoolean(KEY_BATTERY_ALERTS_ENABLED, true))
    private val _temperatureAlertsEnabled = MutableStateFlow(getBoolean(KEY_TEMPERATURE_ALERTS_ENABLED, true))
    private val _lowBatteryThreshold = MutableStateFlow(getInt(KEY_LOW_BATTERY_THRESHOLD, 20))
    private val _highTemperatureThreshold = MutableStateFlow(getInt(KEY_HIGH_TEMPERATURE_THRESHOLD, 40))
    private val _updateInterval = MutableStateFlow(getInt(KEY_UPDATE_INTERVAL, 30))
    private val _powerSaveModeEnabled = MutableStateFlow(getBoolean(KEY_POWER_SAVE_MODE_ENABLED, false))
    private val _advancedMonitoringEnabled = MutableStateFlow(getBoolean(KEY_ADVANCED_MONITORING_ENABLED, false))
    private val _onboardingCompleted = MutableStateFlow(getBoolean(KEY_ONBOARDING_COMPLETED, false))
    private val _usageAccessGranted = MutableStateFlow(getBoolean(KEY_USAGE_ACCESS_GRANTED, false))
    private val _batteryOptimizationEnabled = MutableStateFlow(getBoolean(KEY_BATTERY_OPTIMIZATION_ENABLED, false))

    // FIX: Aggiunta la preferenza per la modalità di accesso
    private val _accessMode = MutableStateFlow(getString(KEY_ACCESS_MODE, "No Root"))

    // --- Flow Getters (per la UI) ---

    // --- NUOVI FLOW GETTERS ---
    fun aiModelStatusFlow(): Flow<AIModelStatus> = _aiModelStatus.asStateFlow()
    fun aiModeFlow(): Flow<String> = _aiMode.asStateFlow()
    fun isLightThemeFlow(): Flow<Boolean> = _isLightTheme.asStateFlow()
    fun accessModeFlow(): Flow<String> = _accessMode.asStateFlow()
    // ... (altri flow getters se necessari in futuro)

    // --- Setters (per modificare le preferenze)
    // --- NUOVI SETTERS ---
    suspend fun setAIModelStatus(status: AIModelStatus) {
        val jsonStatus = Json.encodeToString(status)
        putString(KEY_AI_MODEL_STATUS, jsonStatus)
        _aiModelStatus.value = status
    }

    suspend fun setAIMode(mode: String) { // "BASE" o "ADVANCED"
        putString(KEY_AI_MODE, mode)
        _aiMode.value = mode
    }
    suspend fun setLightTheme(enabled: Boolean) {
        putBoolean(KEY_LIGHT_THEME, enabled)
        _isLightTheme.value = enabled
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
        _notificationsEnabled.value = enabled
    }

    suspend fun setAutoOptimizeEnabled(enabled: Boolean) {
        putBoolean(KEY_AUTO_OPTIMIZE_ENABLED, enabled)
        _autoOptimizeEnabled.value = enabled
    }

    suspend fun setBatteryAlertsEnabled(enabled: Boolean) {
        putBoolean(KEY_BATTERY_ALERTS_ENABLED, enabled)
        _batteryAlertsEnabled.value = enabled
    }

    suspend fun setTemperatureAlertsEnabled(enabled: Boolean) {
        putBoolean(KEY_TEMPERATURE_ALERTS_ENABLED, enabled)
        _temperatureAlertsEnabled.value = enabled
    }

    suspend fun setLowBatteryThreshold(threshold: Int) {
        putInt(KEY_LOW_BATTERY_THRESHOLD, threshold)
        _lowBatteryThreshold.value = threshold
    }

    suspend fun setHighTemperatureThreshold(threshold: Int) {
        putInt(KEY_HIGH_TEMPERATURE_THRESHOLD, threshold)
        _highTemperatureThreshold.value = threshold
    }

    suspend fun setUpdateInterval(interval: Int) {
        putInt(KEY_UPDATE_INTERVAL, interval)
        _updateInterval.value = interval
    }

    suspend fun setPowerSaveModeEnabled(enabled: Boolean) {
        putBoolean(KEY_POWER_SAVE_MODE_ENABLED, enabled)
        _powerSaveModeEnabled.value = enabled
    }

    suspend fun setAdvancedMonitoringEnabled(enabled: Boolean) {
        putBoolean(KEY_ADVANCED_MONITORING_ENABLED, enabled)
        _advancedMonitoringEnabled.value = enabled
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        putBoolean(KEY_ONBOARDING_COMPLETED, completed)
        _onboardingCompleted.value = completed
    }

    suspend fun setUsageAccessGranted(granted: Boolean) {
        putBoolean(KEY_USAGE_ACCESS_GRANTED, granted)
        _usageAccessGranted.value = granted
    }

    suspend fun setBatteryOptimizationEnabled(enabled: Boolean) {
        putBoolean(KEY_BATTERY_OPTIMIZATION_ENABLED, enabled)
        _batteryOptimizationEnabled.value = enabled
    }

    // FIX: Aggiunto il setter per la modalità di accesso
    suspend fun setAccessMode(mode: String) {
        putString(KEY_ACCESS_MODE, mode)
        _accessMode.value = mode
    }

    // --- Getters Sincroni (per accesso immediato non-UI) ---
    fun isLightTheme(): Boolean = getBoolean(KEY_LIGHT_THEME, false)
    fun isNotificationsEnabled(): Boolean = getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    fun isAutoOptimizeEnabled(): Boolean = getBoolean(KEY_AUTO_OPTIMIZE_ENABLED, false)
    fun isBatteryAlertsEnabled(): Boolean = getBoolean(KEY_BATTERY_ALERTS_ENABLED, true)
    fun isTemperatureAlertsEnabled(): Boolean = getBoolean(KEY_TEMPERATURE_ALERTS_ENABLED, true)
    fun getLowBatteryThreshold(): Int = getInt(KEY_LOW_BATTERY_THRESHOLD, 20)
    fun getHighTemperatureThreshold(): Int = getInt(KEY_HIGH_TEMPERATURE_THRESHOLD, 40)
    fun getUpdateInterval(): Int = getInt(KEY_UPDATE_INTERVAL, 30)
    fun isPowerSaveModeEnabled(): Boolean = getBoolean(KEY_POWER_SAVE_MODE_ENABLED, false)
    fun isAdvancedMonitoringEnabled(): Boolean = getBoolean(KEY_ADVANCED_MONITORING_ENABLED, false)
    fun isOnboardingCompleted(): Boolean = getBoolean(KEY_ONBOARDING_COMPLETED, false)
    // --- NUOVI GETTERS SINCRONI ---
    fun getAIModelStatus(): AIModelStatus {
        val jsonStatus = getString(KEY_AI_MODEL_STATUS, "")
        return if (jsonStatus.isEmpty()) {
            AIModelStatus.NotDownloaded
        } else {
            try {
                Json.decodeFromString<AIModelStatus>(jsonStatus)
            } catch (e: Exception) {
                // Se la deserializzazione fallisce, è un errore critico. Torniamo allo stato di base.
                AIModelStatus.NotDownloaded
            }
        }
    }

    // FIX: Aggiunto il getter sincrono per la modalità di accesso
    fun getAccessMode(): String = getString(KEY_ACCESS_MODE, "No Root")

    // --- Funzioni helper private ---
    private fun getBoolean(key: String, defaultValue: Boolean): Boolean = prefs.getBoolean(key, defaultValue)
    private fun getInt(key: String, defaultValue: Int): Int = prefs.getInt(key, defaultValue)
    private fun getString(key: String, defaultValue: String): String = prefs.getString(key, defaultValue) ?: defaultValue
    private fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    private fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    private fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()

    fun getAIMode(): String = getString(KEY_AI_MODE, "BASE")

    companion object {
        private const val PREFS_NAME = "battery_mind_prefs"
        private const val KEY_LIGHT_THEME = "light_theme"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        private const val KEY_AI_MODEL_STATUS = "ai_model_status"
        private const val KEY_AI_MODE = "ai_mode"
        private const val KEY_AUTO_OPTIMIZE_ENABLED = "auto_optimize_enabled"
        private const val KEY_BATTERY_ALERTS_ENABLED = "battery_alerts_enabled"
        private const val KEY_TEMPERATURE_ALERTS_ENABLED = "temperature_alerts_enabled"
        private const val KEY_LOW_BATTERY_THRESHOLD = "low_battery_threshold"
        private const val KEY_HIGH_TEMPERATURE_THRESHOLD = "high_temperature_threshold"
        private const val KEY_UPDATE_INTERVAL = "update_interval"
        private const val KEY_POWER_SAVE_MODE_ENABLED = "power_save_mode_enabled"
        private const val KEY_ADVANCED_MONITORING_ENABLED = "advanced_monitoring_enabled"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_USAGE_ACCESS_GRANTED = "usage_access_granted"
        private const val KEY_BATTERY_OPTIMIZATION_ENABLED = "battery_optimization_enabled"

        // FIX: Aggiunta la chiave per la modalità di accesso
        private const val KEY_ACCESS_MODE = "access_mode"
    }
}