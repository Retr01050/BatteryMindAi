// C:\Users\Alessandro\AndroidStudioProjects\BatteryMind\app\src\main\java\com\creativeideas\batterymindai\BatteryMindApplication.kt

package com.creativeideas.batterymindai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.creativeideas.batterymindai.logic.workers.AutoOptimizeWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class BatteryMindApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // L'unica responsabilità all'avvio è pianificare il lavoro in background.
        // Pulito, semplice, diretto.
        setupRecurringWork()
    }

    // --- L'INTERA FUNZIONE loadSecrets() E I RELATIVI IMPORT SONO STATI ELIMINATI ---

    /**
     * Pianifica il worker che si occuperà delle ottimizzazioni automatiche.
     */
    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<AutoOptimizeWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AutoOptimizeWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}