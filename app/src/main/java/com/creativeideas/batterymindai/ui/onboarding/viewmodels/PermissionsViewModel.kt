package com.creativeideas.batterymindai.ui.onboarding.viewmodels

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionsUiState(
    val usageAccessGranted: Boolean = false,
    val batteryOptimizationDisabled: Boolean = false,
    val permissionProgress: Float = 0f,
    val canFinish: Boolean = false
)

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun checkPermissions() {
        viewModelScope.launch {
            val usageAccess = hasUsageStatsPermission()
            val batteryOptimization = isBatteryOptimizationDisabled()

            val progress = calculateProgress(usageAccess, batteryOptimization)
            val canFinish = usageAccess // Usage access is required to continue

            _uiState.value = _uiState.value.copy(
                usageAccessGranted = usageAccess,
                batteryOptimizationDisabled = batteryOptimization,
                permissionProgress = progress,
                canFinish = canFinish
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            appPreferences.setOnboardingCompleted(true)
            appPreferences.setUsageAccessGranted(_uiState.value.usageAccessGranted)
            appPreferences.setBatteryOptimizationEnabled(_uiState.value.batteryOptimizationDisabled)
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        @Suppress("DEPRECATION")
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun calculateProgress(usageAccess: Boolean, batteryOptimization: Boolean): Float {
        var progress = 0f
        if (usageAccess) progress += 0.7f // Usage access is more important
        if (batteryOptimization) progress += 0.3f
        return progress
    }

    /**
     * Performs a dummy call to the UsageStats API to make the app appear in the settings list.
     */
    fun triggerUsageStatsRegistration() {
        // This call will fail if we don't have the permission, which is fine.
        // Its only purpose is to register our app with the system.
        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            // Query a small time interval
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}