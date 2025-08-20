package com.creativeideas.batterymindai.data.repository

import android.content.Context
import androidx.work.*
import com.creativeideas.batterymindai.data.models.AIModelStatus
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.logic.workers.ModelDownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val workManager: WorkManager
) {
    fun observeModelStatus(): Flow<AIModelStatus> = appPreferences.aiModelStatusFlow()
    fun getCurrentModelStatus(): AIModelStatus = appPreferences.getAIModelStatus()
    suspend fun updateModelStatus(status: AIModelStatus) {
        appPreferences.setAIModelStatus(status)
    }

    fun enqueueModelDownload(requireUnmeteredNetwork: Boolean) {
        val currentStatus = getCurrentModelStatus()
        if (currentStatus is AIModelStatus.Downloading) return

        val manifestUrl = "https://huggingface.co/Retr01050/BatteryMind-AI-Model/resolve/main/manifest.json"

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (requireUnmeteredNetwork) NetworkType.UNMETERED
                else NetworkType.CONNECTED
            )
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(workDataOf(ModelDownloadWorker.KEY_MANIFEST_URL to manifestUrl))
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "model-download-work",
            ExistingWorkPolicy.REPLACE, // <-- MODIFICA CHIAVE: Sostituisce un lavoro fallito.
            downloadWorkRequest
        )
    }

    fun isModelReadyForInference(): Boolean {
        val status = getCurrentModelStatus()
        if (status !is AIModelStatus.Ready) return false
        if (status.version == "Verifying...") return false

        val modelFile = File(status.path)
        return modelFile.exists()
    }

    fun getModelFile(): File? {
        val status = getCurrentModelStatus()
        return if (status is AIModelStatus.Ready) {
            File(status.path)
        } else {
            null
        }
    }
}