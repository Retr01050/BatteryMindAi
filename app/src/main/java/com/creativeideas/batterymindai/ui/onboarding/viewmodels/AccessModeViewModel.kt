package com.creativeideas.batterymindai.ui.onboarding.viewmodels

import android.content.Context // <-- Aggiungi questo import
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext // <-- Aggiungi questo import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File
import javax.inject.Inject
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener // <-- E anche questo!

data class AccessModeUiState(
    val isRootAvailable: Boolean = false,
    val isShizukuAvailable: Boolean = false, // Significa che il servizio Shizuku è in esecuzione
    val isShizukuPermissionGranted: Boolean = false, // Significa che abbiamo il permesso
    val selectedMode: String = "No Root" // Partiamo sempre da No Root come default sicuro
)

@HiltViewModel
class AccessModeViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context // <-- Inietta il contesto
) : ViewModel() {


    private val _uiState = MutableStateFlow(AccessModeUiState())
    val uiState: StateFlow<AccessModeUiState> = _uiState.asStateFlow()

    companion object {
        // Codice di richiesta che useremo nel launcher
        const val SHIZUKU_PERMISSION_REQUEST_CODE = 101
    }

    // Lo chiamiamo all'inizio per impostare lo stato iniziale
    init {
        checkAccessModes()
    }

    /**
     * Controlla la disponibilità delle modalità e aggiorna lo stato della UI.
     * Questa funzione ora è il punto di riferimento unico per lo stato di Shizuku.
     */
    fun checkAccessModes() {
        viewModelScope.launch {
            val rootAvailable = isRootAvailable()
            val shizukuActive = isShizukuActive()
            val shizukuPermissionGranted = if (shizukuActive) isShizukuPermissionGranted() else false

            // Se l'utente aveva già selezionato una modalità e questa è ancora valida, la manteniamo.
            // Altrimenti, preselezioniamo la migliore disponibile.
            val currentSelection = _uiState.value.selectedMode
            val preSelectedMode = when {
                currentSelection == "Root" && rootAvailable -> "Root"
                currentSelection == "Shizuku" && shizukuPermissionGranted -> "Shizuku"
                rootAvailable -> "Root"
                shizukuPermissionGranted -> "Shizuku"
                else -> "No Root"
            }

            _uiState.value = _uiState.value.copy(
                isRootAvailable = rootAvailable,
                isShizukuAvailable = shizukuActive,
                isShizukuPermissionGranted = shizukuPermissionGranted,
                selectedMode = preSelectedMode
            )
        }
    }

    /**
     * Verifica se il servizio Shizuku è in esecuzione e raggiungibile.
     * FIX: Aggiunto controllo per vedere se Shizuku è installato.
     */
    private fun isShizukuActive(): Boolean {
        // Se l'app di Shizuku non è installata, non può essere attivo.
        if (!isShizukuInstalled()) {
            return false
        }
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * NUOVA FUNZIONE: Controlla se il package manager di Shizuku è presente sul dispositivo.
     */
    private fun isShizukuInstalled(): Boolean {
        return try {
            // FIX: Il nome del pacchetto corretto per l'app Shizuku è "moe.shizuku.manager"
            context.packageManager.getPackageInfo("moe.shizuku.manager", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Verifica se l'utente ha già concesso il permesso a Shizuku.
     */
    private fun isShizukuPermissionGranted(): Boolean {
        if (!isShizukuActive()) return false
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        return@withContext paths.any { File(it).exists() }
    }

    fun selectMode(mode: String) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun saveAccessMode() {
        viewModelScope.launch {
            appPreferences.setAccessMode(_uiState.value.selectedMode)
        }
    }
}