package com.creativeideas.batterymindai.ui.onboarding.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.data.repository.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiChoiceViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val modelRepository: ModelRepository
) : ViewModel() {

    fun selectAIMode(mode: String) { // "BASE" o "ADVANCED"
        viewModelScope.launch {
            appPreferences.setAIMode(mode)
            if (mode == "ADVANCED") {
                modelRepository.enqueueModelDownload(requireUnmeteredNetwork = false)
            }
        }
    }
}