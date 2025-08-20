package com.creativeideas.batterymindai.logic.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.creativeideas.batterymindai.data.repository.BatteryRepository
import com.creativeideas.batterymindai.utils.SystemSettingsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class AutoOptimizeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    // Inietta le dipendenze necessarie per prendere decisioni
    private val batteryRepository: BatteryRepository,
    private val systemSettingsManager: SystemSettingsManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Esegui la logica di ottimizzazione automatica
            performAutoOptimization()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun performAutoOptimization() {
        // Esempio di logica "intelligente":
        // Se è notte (tra le 23 e le 6) e il telefono non è in carica,
        // prova ad attivare il risparmio energetico.
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNightTime = currentHour >= 23 || currentHour <= 6

        val latestStats = batteryRepository.getLatestBatteryStats() ?: return

        if (isNightTime && !latestStats.isCharging) {
            // Qui l'app esegue un'azione in autonomia.
            // Questo funzionerà solo se l'utente ha concesso i permessi di Root/Shizuku.
            systemSettingsManager.enablePowerSaveMode()

            // Si potrebbero aggiungere molte altre regole:
            // - Se la temperatura > 40°C, riduci la luminosità.
            // - Se la batteria < 15%, attiva il risparmio energetico.
            // - Se il GPS è attivo da più di 1 ora e non ci sono app di mappe in uso,
            //   invia una notifica per suggerire di disattivarlo (l'azione diretta è rischiosa).
        }
    }
}