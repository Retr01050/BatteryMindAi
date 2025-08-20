package com.creativeideas.batterymindai.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.creativeideas.batterymindai.data.models.AIModelStatus
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.data.repository.BatteryRepository
import com.creativeideas.batterymindai.data.repository.ModelRepository
import com.creativeideas.batterymindai.utils.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SettingsUiState(
    val darkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val autoOptimize: Boolean = false,
    val batteryAlerts: Boolean = true,
    val temperatureAlerts: Boolean = true,
    val lowBatteryThreshold: Int = 20,
    val highTemperatureThreshold: Int = 40,
    val updateInterval: Int = 30,
    val powerSaveModeEnabled: Boolean = false,
    val advancedMonitoring: Boolean = false,
    val hasUsageStatsPermission: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val accessMode: String = "No Root"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val permissionManager: PermissionManager,
    private val batteryRepository: BatteryRepository,
    private val modelRepository: ModelRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    sealed class UiEvent {
        data object ShowMobileDataDialog : UiEvent()
        data class ShowToast(val message: String) : UiEvent()
    }

    init {
        loadInitialState()
    }

    val modelStatus: StateFlow<AIModelStatus> = modelRepository.observeModelStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AIModelStatus.NotDownloaded)

    fun onDownloadRequest() {
        viewModelScope.launch {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: run {
                _uiEvents.emit(UiEvent.ShowToast("Nessuna connessione di rete disponibile."))
                return@launch
            }
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isWifiConnected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            if (isWifiConnected) {
                modelRepository.enqueueModelDownload(requireUnmeteredNetwork = true)
                _uiEvents.emit(UiEvent.ShowToast("Download AI in coda..."))
            } else {
                _uiEvents.emit(UiEvent.ShowMobileDataDialog)
            }
        }
    }

    fun startMobileDownload() {
        modelRepository.enqueueModelDownload(requireUnmeteredNetwork = false)
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowToast("Download AI in coda..."))
        }
    }

    fun getDownloadProgress(): Flow<WorkInfo?> {
        return workManager.getWorkInfosForUniqueWorkFlow("model-download-work")
            .map { it.firstOrNull() }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                darkMode = !appPreferences.isLightTheme(),
                notificationsEnabled = appPreferences.isNotificationsEnabled(),
                autoOptimize = appPreferences.isAutoOptimizeEnabled(),
                batteryAlerts = appPreferences.isBatteryAlertsEnabled(),
                temperatureAlerts = appPreferences.isTemperatureAlertsEnabled(),
                lowBatteryThreshold = appPreferences.getLowBatteryThreshold(),
                highTemperatureThreshold = appPreferences.getHighTemperatureThreshold(),
                updateInterval = appPreferences.getUpdateInterval(),
                powerSaveModeEnabled = appPreferences.isPowerSaveModeEnabled(),
                advancedMonitoring = appPreferences.isAdvancedMonitoringEnabled(),
                hasUsageStatsPermission = permissionManager.hasUsageStatsPermission(),
                isBatteryOptimizationDisabled = true, // Placeholder, la logica reale andrebbe qui
                accessMode = appPreferences.getAccessMode()
            )
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setLightTheme(!enabled)
            _uiState.value = _uiState.value.copy(darkMode = enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setNotificationsEnabled(enabled)
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        }
    }

    fun setAutoOptimize(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setAutoOptimizeEnabled(enabled)
            _uiState.value = _uiState.value.copy(autoOptimize = enabled)
        }
    }

    fun setBatteryAlerts(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setBatteryAlertsEnabled(enabled)
            _uiState.value = _uiState.value.copy(batteryAlerts = enabled)
        }
    }

    fun setTemperatureAlerts(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setTemperatureAlertsEnabled(enabled)
            _uiState.value = _uiState.value.copy(temperatureAlerts = enabled)
        }
    }

    fun setAdvancedMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setAdvancedMonitoringEnabled(enabled)
            _uiState.value = _uiState.value.copy(advancedMonitoring = enabled)
        }
    }

    fun setLowBatteryThreshold(threshold: Int) {
        viewModelScope.launch {
            appPreferences.setLowBatteryThreshold(threshold)
            _uiState.value = _uiState.value.copy(lowBatteryThreshold = threshold)
        }
    }

    fun setHighTemperatureThreshold(threshold: Int) {
        viewModelScope.launch {
            appPreferences.setHighTemperatureThreshold(threshold)
            _uiState.value = _uiState.value.copy(highTemperatureThreshold = threshold)
        }
    }

    fun setUpdateInterval(interval: Int) {
        viewModelScope.launch {
            appPreferences.setUpdateInterval(interval)
            _uiState.value = _uiState.value.copy(updateInterval = interval)
        }
    }

    fun requestUsageStatsPermission() {
        val intent = permissionManager.getUsageStatsPermissionIntent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun requestBatteryOptimizationDisable() {
        val intent = permissionManager.getBatteryOptimizationIntent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun exportBatteryData() {
        viewModelScope.launch {
            val history = batteryRepository.getAllBatteryStats().firstOrNull() ?: emptyList()

            if (history.isEmpty()) {
                _uiEvents.emit(UiEvent.ShowToast("Nessun dato da esportare."))
                return@launch
            }

            val csvHeader = "Timestamp,BatteryLevel,IsCharging,Temperature,Voltage,Health,PlugType,Score"
            val csvData = history.joinToString(separator = "\n") { stats ->
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(stats.timestamp)
                "$date,${stats.batteryLevel},${stats.isCharging},${stats.temperature},${stats.voltage},${stats.health},${stats.plugType},${stats.batteryScore}"
            }

            val content = "$csvHeader\n$csvData"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "BatteryMind AI Data Export")
                putExtra(Intent.EXTRA_TEXT, content)
            }

            val chooser = Intent.createChooser(intent, "Export Battery Data").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            appPreferences.setLightTheme(false)
            appPreferences.setNotificationsEnabled(true)
            appPreferences.setAutoOptimizeEnabled(false)
            appPreferences.setBatteryAlertsEnabled(true)
            appPreferences.setTemperatureAlertsEnabled(true)
            appPreferences.setLowBatteryThreshold(20)
            appPreferences.setHighTemperatureThreshold(40)
            appPreferences.setUpdateInterval(30)
            appPreferences.setPowerSaveModeEnabled(false)
            appPreferences.setAdvancedMonitoringEnabled(false)
            loadInitialState()
        }
    }
}