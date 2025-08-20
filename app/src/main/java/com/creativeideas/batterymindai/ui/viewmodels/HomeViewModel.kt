package com.creativeideas.batterymindai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativeideas.batterymindai.ai.BatteryAIAnalyzer
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.models.*
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.data.repository.BatteryRepository
import com.creativeideas.batterymindai.data.sensors.AppUsageAnalyzer
import com.creativeideas.batterymindai.data.sensors.SystemSensorManager
import com.creativeideas.batterymindai.utils.BatteryOptimizer
import com.creativeideas.batterymindai.utils.RecommendationExecutor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentBatteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val estimatedTimeRemaining: String = "Calculating...",
    val batteryHealth: String = "Unknown",
    val batteryTemperature: Float = 0.0f,
    val batteryScore: Int = 0,
    val aiRecommendations: List<AIRecommendation> = emptyList(),
    val appUsage: List<AppUsageInfo> = emptyList(),
    val sensorStatuses: List<SensorStatus> = emptyList(),
    val dischargeInfo: DischargeInfo? = null,
    val showDischargeDetails: Boolean = false,
    val quickActionResults: List<String> = emptyList(),
    val autoOptimizeEnabled: Boolean = false,
    val batteryHistory: List<BatteryStatsEntity> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val aiAnalyzer: BatteryAIAnalyzer,
    private val batteryOptimizer: BatteryOptimizer,
    private val appUsageAnalyzer: AppUsageAnalyzer,
    private val systemSensorManager: SystemSensorManager,
    private val recommendationExecutor: RecommendationExecutor,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Carica i dati che cambiano lentamente una sola volta all'inizio
            val initialAppUsage = if (appUsageAnalyzer.hasUsageStatsPermission()) {
                appUsageAnalyzer.getRealAppUsage()
            } else { emptyList() }

            val batteryHistory = batteryRepository.getAllBatteryStats().first()

            _uiState.value = _uiState.value.copy(
                appUsage = initialAppUsage,
                batteryHistory = batteryHistory
            )

            // Avvia il ciclo di aggiornamento per i dati in tempo reale
            while (isActive) {
                loadRealTimeData()
                val updateIntervalMillis = (appPreferences.getUpdateInterval() * 1000L).coerceAtLeast(1000L)
                delay(updateIntervalMillis)
            }
        }
    }

    private fun loadRealTimeData() {
        viewModelScope.launch {
            try {
                val batteryStats = batteryRepository.getCurrentBatteryInfo()
                val sensorStatuses = systemSensorManager.getCurrentSensorStatus()
                val appUsage = _uiState.value.appUsage
                val history = _uiState.value.batteryHistory

                val dischargeInfo = generateDischargeInfo(batteryStats, appUsage, sensorStatuses)

                val recommendations = aiAnalyzer.generateRecommendations(
                    batteryStats = batteryStats,
                    appUsage = appUsage,
                    history = history
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentBatteryLevel = batteryStats.batteryLevel,
                    isCharging = batteryStats.isCharging,
                    estimatedTimeRemaining = calculateTimeRemaining(batteryStats, appUsage),
                    batteryHealth = batteryStats.health,
                    batteryTemperature = batteryStats.temperature,
                    batteryScore = batteryStats.batteryScore,
                    aiRecommendations = recommendations,
                    sensorStatuses = sensorStatuses,
                    dischargeInfo = dischargeInfo
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load real-time data: ${e.message}"
                )
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Ricarica tutto, inclusa la cronologia e l'uso delle app
            val fullAppUsage = if (appUsageAnalyzer.hasUsageStatsPermission()) {
                appUsageAnalyzer.getRealAppUsage()
            } else { emptyList() }
            val batteryHistory = batteryRepository.getAllBatteryStats().first()

            _uiState.value = _uiState.value.copy(
                appUsage = fullAppUsage,
                batteryHistory = batteryHistory,
                isLoading = false
            )
            loadRealTimeData()
        }
    }

    fun executeRecommendation(recommendation: AIRecommendation) {
        viewModelScope.launch {
            recommendation.action?.let {
                val success = recommendationExecutor.executeRecommendation(recommendation)
                if (success) {
                    val updatedRecommendations = _uiState.value.aiRecommendations.filter { it.id != recommendation.id }
                    _uiState.value = _uiState.value.copy(
                        aiRecommendations = updatedRecommendations,
                        quickActionResults = listOf("✅ ${recommendation.title} applied.")
                    )
                    refreshData()
                }
            }
        }
    }

    fun dismissRecommendation(recommendation: AIRecommendation) {
        val updatedRecommendations = _uiState.value.aiRecommendations.filter { it.id != recommendation.id }
        _uiState.value = _uiState.value.copy(aiRecommendations = updatedRecommendations)
    }

    private fun calculateTimeRemaining(
        batteryStats: BatteryStatsEntity,
        appUsage: List<AppUsageInfo>
    ): String {
        val totalPowerConsumption = appUsage.sumOf { it.powerConsumption.toDouble() }.toFloat()
        val basePowerConsumption = 2.0f
        val totalConsumption = totalPowerConsumption + basePowerConsumption

        return if (batteryStats.isCharging) {
            val timeToFull = ((100 - batteryStats.batteryLevel) * 1.5f).toInt()
            if (timeToFull <= 0) "Fully Charged" else "${timeToFull / 60}h ${timeToFull % 60}m until full"
        } else {
            if (totalConsumption > 0.1f) {
                val hoursRemaining = batteryStats.batteryLevel / totalConsumption
                val minutesRemaining = ((hoursRemaining - hoursRemaining.toInt()) * 60).toInt()
                "${hoursRemaining.toInt()}h ${minutesRemaining}m remaining"
            } else {
                "Calculating..."
            }
        }
    }

    private fun generateDischargeInfo(
        batteryStats: BatteryStatsEntity,
        appUsage: List<AppUsageInfo>,
        sensorStatuses: List<SensorStatus>
    ): DischargeInfo {
        val totalAppPowerConsumption = appUsage.sumOf { it.powerConsumption.toDouble() }.toFloat()
        val currentRate = totalAppPowerConsumption + 5.0f
        val averageRate = currentRate * 0.8f
        val status = when {
            currentRate > 20f -> DischargeLevel.HIGH
            currentRate > 10f -> DischargeLevel.MEDIUM
            else -> DischargeLevel.LOW
        }
        val backgroundActivities = appUsage.filter { it.isRunningInBackground }.map { app ->
            BackgroundActivity(
                appName = app.appName,
                priority = when {
                    app.powerConsumption > 15f -> "High"
                    app.powerConsumption > 8f -> "Medium"
                    else -> "Low"
                },
                powerConsumption = app.powerConsumption,
                description = "${app.appName} running in background"
            )
        }
        val activeSettings = sensorStatuses.filter { it.isActive }.map { sensor ->
            ActiveSetting(
                name = sensor.name,
                impact = when {
                    sensor.powerConsumption > 10f -> "High"
                    sensor.powerConsumption > 5f -> "Medium"
                    else -> "Low"
                },
                description = sensor.description
            )
        }
        val wifiSensor = sensorStatuses.find { it.name == "WiFi" }
        val bluetoothSensor = sensorStatuses.find { it.name == "Bluetooth" }
        val mobileSensor = sensorStatuses.find { it.name == "Mobile Data" }
        val networkUsage = NetworkUsage(
            wifiUsage = wifiSensor?.powerConsumption ?: 0f,
            mobileDataUsage = mobileSensor?.powerConsumption ?: 0f,
            bluetoothUsage = bluetoothSensor?.powerConsumption ?: 0f
        )
        val timeRemainingStr = calculateTimeRemaining(batteryStats, appUsage)

        return DischargeInfo(
            currentRate = currentRate,
            averageRate = averageRate,
            estimatedTimeRemaining = timeRemainingStr,
            status = status,
            backgroundActivities = backgroundActivities,
            activeSettings = activeSettings,
            cpuUsage = 45.2f,
            gpuUsage = 12.8f,
            networkUsage = networkUsage,
            screenBrightness = 75
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun toggleDischargeDetails() {
        _uiState.value = _uiState.value.copy(showDischargeDetails = !_uiState.value.showDischargeDetails)
    }

    fun clearQuickActionResult() {
        _uiState.value = _uiState.value.copy(quickActionResults = emptyList())
    }

    fun executeQuickAction(action: String) {
        viewModelScope.launch {
            try {
                val results = when (action) {
                    "optimize" -> batteryOptimizer.performQuickOptimization()
                    "power_save" -> batteryOptimizer.enablePowerSaveMode()
                    "analyze" -> batteryOptimizer.analyzeCurrentUsage()
                    else -> emptyList()
                }
                _uiState.value = _uiState.value.copy(quickActionResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to execute action: ${e.message}")
            }
        }
    }
}