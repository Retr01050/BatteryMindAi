package com.creativeideas.batterymindai.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.creativeideas.batterymindai.data.repository.BatteryRepository
import javax.inject.Inject

@AndroidEntryPoint
class BatteryReceiver : BroadcastReceiver() {

    @Inject
    lateinit var batteryRepository: BatteryRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                handleBatteryChanged(intent)
            }
            Intent.ACTION_POWER_CONNECTED -> {
                handlePowerConnected()
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                handlePowerDisconnected()
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                handleBootCompleted(context)
            }
        }
    }

    private fun handleBatteryChanged(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = (level * 100 / scale.toFloat()).toInt()

        // Salva i dati della batteria
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val batteryStats = batteryRepository.getCurrentBatteryInfo()
                batteryRepository.insertBatteryStats(batteryStats)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun handlePowerConnected() {
        // Gestisci connessione alimentazione
        // Potresti voler ottimizzare le impostazioni per la carica
    }

    private fun handlePowerDisconnected() {
        // Gestisci disconnessione alimentazione
        // Potresti voler attivare il risparmio energetico
    }

    private fun handleBootCompleted(context: Context) {
        // Avvia il servizio di monitoraggio al boot
        val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
        context.startForegroundService(serviceIntent)
    }
}
