package com.creativeideas.batterymindai.ui.viewmodels

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativeideas.batterymindai.data.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceInfoState(
    val model: String = "N/A",
    val androidVersion: String = "N/A",
    val apiLevel: String = "N/A"
)

data class BatteryInfoState(
    val level: String = "N/A",
    val voltage: String = "N/A",
    val temperature: String = "N/A",
    val technology: String = "N/A",
    val health: String = "N/A"
)

data class SystemInfoState(
    val totalRam: String = "N/A",
    val availableRam: String = "N/A",
    val cpuCores: String = Runtime.getRuntime().availableProcessors().toString()
)

@HiltViewModel
class InformationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    private val _deviceInfo = MutableStateFlow(DeviceInfoState())
    val deviceInfo: StateFlow<DeviceInfoState> = _deviceInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfoState())
    val batteryInfo: StateFlow<BatteryInfoState> = _batteryInfo.asStateFlow()

    private val _systemInfo = MutableStateFlow(SystemInfoState())
    val systemInfo: StateFlow<SystemInfoState> = _systemInfo.asStateFlow()

    init {
        loadAllInfo()
    }

    private fun loadAllInfo() {
        // Carica informazioni statiche sul dispositivo
        _deviceInfo.value = DeviceInfoState(
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            apiLevel = Build.VERSION.SDK_INT.toString()
        )

        // Carica informazioni sulla RAM
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val totalRam = memInfo.totalMem / (1024 * 1024)
            val availRam = memInfo.availMem / (1024 * 1024)
            _systemInfo.value = _systemInfo.value.copy(
                totalRam = "$totalRam MB",
                availableRam = "$availRam MB"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _systemInfo.value = _systemInfo.value.copy(
                totalRam = "Error",
                availableRam = "Error"
            )
        }

        // Carica informazioni sulla batteria in modo asincrono
        viewModelScope.launch {
            try {
                val stats = batteryRepository.getCurrentBatteryInfo()
                _batteryInfo.value = BatteryInfoState(
                    level = "${stats.batteryLevel}%",
                    voltage = "${stats.voltage} mV",
                    temperature = "${stats.temperature}°C",
                    technology = stats.technology,
                    health = stats.health
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}