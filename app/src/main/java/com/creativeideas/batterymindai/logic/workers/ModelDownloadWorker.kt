package com.creativeideas.batterymindai.logic.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.creativeideas.batterymindai.R
import com.creativeideas.batterymindai.data.models.AIModelStatus
import com.creativeideas.batterymindai.data.models.ModelManifest
import com.creativeideas.batterymindai.data.repository.ModelRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.core.isNotEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val modelRepository: ModelRepository,
    private val httpClient: HttpClient
) : CoroutineWorker(appContext, workerParams) {

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val KEY_MANIFEST_URL = "manifest_url"
        const val KEY_PROGRESS = "progress"
        private const val MODEL_FILENAME = "on-device-ai-model.bin"
        private const val TEMP_MODEL_FILENAME = "on-device-ai-model.tmp"
        const val NOTIFICATION_CHANNEL_ID = "model_download_channel"
        const val NOTIFICATION_ID = 42
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        return ForegroundInfo(NOTIFICATION_ID, createNotification("Starting download...", 0, true))
    }

    private fun createNotification(contentText: String, progress: Int, indeterminate: Boolean): android.app.Notification {
        return NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Downloading Advanced AI")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_battery_notification) // Assicurati che esista
            .setOngoing(true)
            .setProgress(100, progress, indeterminate)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Model Downloads",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override suspend fun doWork(): Result {
        val manifestUrl = inputData.getString(KEY_MANIFEST_URL) ?: run {
            Log.e("ModelDownloadWorker", "Manifest URL is missing. Work failed.")
            return Result.failure()
        }

        // Avvia il servizio in primo piano con la notifica iniziale
        setForeground(getForegroundInfo())
        Log.d("ModelDownloadWorker", "Work started in foreground. Manifest URL: $manifestUrl")

        val tempFile = File(appContext.filesDir, TEMP_MODEL_FILENAME)

        try {
            modelRepository.updateModelStatus(AIModelStatus.Downloading)
            Log.d("ModelDownloadWorker", "Status updated to Downloading.")

            Log.d("ModelDownloadWorker", "Fetching manifest...")
            val manifest = httpClient.get(manifestUrl).body<ModelManifest>()
            Log.d("ModelDownloadWorker", "Manifest fetched successfully. Model URL: ${manifest.url}")

            Log.d("ModelDownloadWorker", "Starting model download to: ${tempFile.absolutePath}")
            httpClient.prepareGet(manifest.url).execute { httpResponse ->
                if (httpResponse.status.value !in 200..299) {
                    throw Exception("Download failed: HTTP ${httpResponse.status.value}")
                }
                val body = httpResponse.bodyAsChannel()
                val totalBytes = httpResponse.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: manifest.sizeInBytes
                var bytesCopied = 0L
                var lastLoggedProgress = -1

                Log.d("ModelDownloadWorker", "Model size: $totalBytes bytes. Starting stream copy.")
                FileOutputStream(tempFile).use { fileOutputStream ->
                    while (!body.isClosedForRead) {
                        val packet = body.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (packet.isNotEmpty) {
                            val bytes = packet.readBytes()
                            fileOutputStream.write(bytes)
                            bytesCopied += bytes.size
                            if (totalBytes > 0) {
                                val progress = (bytesCopied * 100 / totalBytes).toInt()
                                val progressText = "${bytesCopied / 1_000_000} MB / ${totalBytes / 1_000_000} MB"

                                setProgress(workDataOf(KEY_PROGRESS to progress))
                                notificationManager.notify(NOTIFICATION_ID, createNotification(progressText, progress, false))

                                if (progress % 5 == 0 && progress != lastLoggedProgress) {
                                    Log.d("ModelDownloadWorker", "Download progress: $progress%")
                                    lastLoggedProgress = progress
                                }
                            }
                        }
                    }
                }
            }
            Log.d("ModelDownloadWorker", "Download completed. Total bytes: ${tempFile.length()}")

            notificationManager.notify(NOTIFICATION_ID, createNotification("Verifying file...", 100, true))
            Log.d("ModelDownloadWorker", "Verifying file integrity...")
            modelRepository.updateModelStatus(AIModelStatus.Ready("Verifying...", "", 0L, ""))
            val calculatedChecksum = calculateSha256(tempFile)
            Log.d("ModelDownloadWorker", "Calculated Checksum: $calculatedChecksum")
            Log.d("ModelDownloadWorker", "Expected Checksum:   ${manifest.checksum}")

            if (calculatedChecksum.equals(manifest.checksum, ignoreCase = true)) {
                Log.d("ModelDownloadWorker", "Checksum MATCH. Finalizing file.")
                val modelFile = File(appContext.filesDir, MODEL_FILENAME)
                if (modelFile.exists()) modelFile.delete()
                tempFile.renameTo(modelFile)
                modelRepository.updateModelStatus(
                    AIModelStatus.Ready(
                        version = manifest.latestVersion,
                        path = modelFile.absolutePath,
                        sizeInBytes = manifest.sizeInBytes,
                        checksum = manifest.checksum
                    )
                )
                Log.d("ModelDownloadWorker", "Work successful.")
                return Result.success()
            } else {
                Log.e("ModelDownloadWorker", "Checksum MISMATCH. Deleting temporary file.")
                throw Exception("Checksum mismatch")
            }
        } catch (e: Exception) {
            Log.e("ModelDownloadWorker", "An exception occurred during work", e)
            tempFile.delete()
            modelRepository.updateModelStatus(AIModelStatus.DownloadFailed(e.message ?: "Unknown error"))
            return Result.failure()
        } finally {
            // Rimuovi la notifica in ogni caso
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    private suspend fun calculateSha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return@withContext digest.digest().joinToString("") { "%02x".format(it) }
    }
}