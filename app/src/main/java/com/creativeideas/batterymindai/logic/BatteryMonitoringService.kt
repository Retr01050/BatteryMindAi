package com.creativeideas.batterymindai.logic

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.creativeideas.batterymindai.MainActivity
import com.creativeideas.batterymindai.R
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.data.repository.BatteryRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class BatteryMonitoringService : Service() {

    @Inject
    lateinit var batteryRepository: BatteryRepository
    @Inject
    lateinit var appPreferences: AppPreferences

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val ALERT_NOTIFICATION_ID = 2
        private const val CHANNEL_ID_FOREGROUND = "battery_monitoring_foreground"
        private const val CHANNEL_ID_ALERTS = "battery_alerts"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
        startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    val batteryStats = batteryRepository.getCurrentBatteryInfo()
                    batteryRepository.insertBatteryStats(batteryStats)

                    if (appPreferences.isNotificationsEnabled()) {
                        checkForAlerts(batteryStats)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // Il servizio in background non deve essere troppo aggressivo.
                // Controlla ogni 5 minuti per non consumare troppa batteria.
                delay(5 * 60 * 1000L)
            }
        }
    }

    private fun checkForAlerts(stats: BatteryStatsEntity) {
        // Controllo Batteria Scarica
        if (appPreferences.isBatteryAlertsEnabled()) {
            val lowThreshold = appPreferences.getLowBatteryThreshold()
            if (stats.batteryLevel <= lowThreshold && !stats.isCharging) {
                sendAlertNotification(
                    title = "Low Battery Warning",
                    message = "Battery is at ${stats.batteryLevel}%. Consider charging your device."
                )
            }
        }

        // Controllo Temperatura Alta
        if (appPreferences.isTemperatureAlertsEnabled()) {
            val highThreshold = appPreferences.getHighTemperatureThreshold()
            if (stats.temperature >= highThreshold) {
                sendAlertNotification(
                    title = "High Temperature Alert",
                    message = "Device temperature is ${stats.temperature}°C. Avoid heavy usage to cool it down."
                )
            }
        }
    }

    private fun sendAlertNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_battery_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        with(NotificationManagerCompat.from(this)) {
            // Usiamo un ID univoco basato sul timestamp per garantire che ogni notifica
            // venga mostrata come nuova, anche se il titolo è lo stesso.
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    private fun createNotificationChannels() {
        val foregroundChannel = NotificationChannel(
            CHANNEL_ID_FOREGROUND,
            "Battery Monitoring Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notification to keep the monitoring service running."
        }

        val alertsChannel = NotificationChannel(
            CHANNEL_ID_ALERTS,
            "Battery Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows alerts for low battery and high temperature."
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(foregroundChannel)
        notificationManager.createNotificationChannel(alertsChannel)
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
            .setContentTitle("BatteryMind AI")
            .setContentText("Monitoring battery status in the background.")
            .setSmallIcon(R.drawable.ic_battery_notification)
            .setOngoing(true)
            .build()
    }
}