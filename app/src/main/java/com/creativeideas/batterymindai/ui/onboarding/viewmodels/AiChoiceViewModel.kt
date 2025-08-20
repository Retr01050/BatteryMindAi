package com.creativeideas.batterymindai.ui.onboarding.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.data.repository.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiChoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val modelRepository: ModelRepository
) : ViewModel() {

    sealed class UiEvent {
        data object ShowMobileDataDialog : UiEvent()
        data object NavigateToNextScreen : UiEvent()
    }

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    fun selectAIMode(mode: String) { // "BASE" o "ADVANCED"
        viewModelScope.launch {
            appPreferences.setAIMode(mode)
            if (mode == "BASE") {
                _uiEvents.emit(UiEvent.NavigateToNextScreen)
                return@launch
            }

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork ?: run {
                _uiEvents.emit(UiEvent.NavigateToNextScreen) // Nessuna rete, procedi comunque
                return@launch
            }
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isWifiConnected =
                capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true

            if (isWifiConnected) {
                modelRepository.enqueueModelDownload(requireUnmeteredNetwork = true)
                _uiEvents.emit(UiEvent.NavigateToNextScreen)
            } else {
                _uiEvents.emit(UiEvent.ShowMobileDataDialog)
            }
        }
    }

    fun startMobileDownload() {
        viewModelScope.launch {
            modelRepository.enqueueModelDownload(requireUnmeteredNetwork = false)
            _uiEvents.emit(UiEvent.NavigateToNextScreen)
        }
    }
}