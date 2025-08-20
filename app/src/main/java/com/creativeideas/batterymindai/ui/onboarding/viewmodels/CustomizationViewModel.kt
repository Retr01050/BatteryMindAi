package com.creativeideas.batterymindai.ui.onboarding.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import javax.inject.Inject

data class CustomizationUiState(
    val isLightTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val autoOptimizeEnabled: Boolean = false,
    val advancedMonitoringEnabled: Boolean = false
)

@HiltViewModel
class CustomizationViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomizationUiState())
    val uiState: StateFlow<CustomizationUiState> = _uiState.asStateFlow()

    init {
        loadCurrentPreferences()
    }

    private fun loadCurrentPreferences() {
        _uiState.value = _uiState.value.copy(
            isLightTheme = appPreferences.isLightTheme(),
            notificationsEnabled = appPreferences.isNotificationsEnabled(),
            autoOptimizeEnabled = appPreferences.isAutoOptimizeEnabled(),
            advancedMonitoringEnabled = appPreferences.isAdvancedMonitoringEnabled()
        )
    }

    fun setLightTheme(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isLightTheme = enabled)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun setAutoOptimizeEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoOptimizeEnabled = enabled)
    }

    fun setAdvancedMonitoringEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(advancedMonitoringEnabled = enabled)
    }

    fun savePreferences() {
        viewModelScope.launch {
            val state = _uiState.value
            appPreferences.setLightTheme(state.isLightTheme)
            appPreferences.setNotificationsEnabled(state.notificationsEnabled)
            appPreferences.setAutoOptimizeEnabled(state.autoOptimizeEnabled)
            appPreferences.setAdvancedMonitoringEnabled(state.advancedMonitoringEnabled)
        }
    }
}
