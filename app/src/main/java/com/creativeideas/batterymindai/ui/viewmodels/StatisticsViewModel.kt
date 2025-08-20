package com.creativeideas.batterymindai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.models.AppUsageInfo
import com.creativeideas.batterymindai.data.repository.BatteryRepository
import com.creativeideas.batterymindai.data.sensors.AppUsageAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class StatisticsUiState(
    val batteryHistory: List<BatteryStatsEntity> = emptyList(),
    val appUsageHistory: List<AppUsageInfo> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val appUsageAnalyzer: AppUsageAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        refreshData() // Carica i dati all'avvio
    }

    /**
     * Carica o ricarica tutti i dati necessari per la schermata delle statistiche.
     */
    fun refreshData() {
        viewModelScope.launch {
            // Carica la cronologia della batteria
            val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val history = batteryRepository.getBatteryStatsFromDate(calendar.time).first()

            // Carica i dati di utilizzo delle app
            val appUsage = if (appUsageAnalyzer.hasUsageStatsPermission()) {
                appUsageAnalyzer.getRealAppUsage()
            } else {
                emptyList()
            }

            _uiState.value = StatisticsUiState(
                batteryHistory = history,
                appUsageHistory = appUsage.sortedByDescending { it.powerConsumption }.take(5)
            )
        }
    }
}