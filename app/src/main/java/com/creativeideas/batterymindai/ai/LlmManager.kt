package com.creativeideas.batterymindai.ai

import android.content.Context
import android.util.Log
import com.creativeideas.batterymindai.data.repository.ModelRepository
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelRepository: ModelRepository
) {
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private var llmInference: LlmInference? = null

    init {
        // [Principio Jobs] L'inizializzazione avviene in background per non bloccare mai il thread principale.
        // L'app si avvia e vive, mentre il "cervello" si sveglia silenziosamente.
        CoroutineScope(Dispatchers.IO).launch {
            initialize()
        }
    }

    private suspend fun initialize() {
        if (!modelRepository.isModelReadyForInference()) {
            Log.d("LlmManager", "Model not ready for inference. Skipping initialization.")
            _isReady.value = false
            return
        }

        try {
            val modelFile = modelRepository.getModelFile()!!
            Log.d("LlmManager", "Initializing LLM from path: ${modelFile.absolutePath}")

            val options = LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .build()

            // [Principio Musk] L'operazione più costosa. Viene eseguita una sola volta.
            llmInference = LlmInference.createFromOptions(context, options)
            _isReady.value = true
            Log.d("LlmManager", "LLM Initialization successful.")

        } catch (e: Exception) {
            // Se il caricamento fallisce (es. file corrotto), ce ne accorgiamo qui.
            Log.e("LlmManager", "LLM Initialization failed", e)
            _isReady.value = false
            // Potremmo anche aggiornare lo stato del modello a FAILED qui.
            // modelRepository.updateModelStatus(AIModelStatus.DownloadFailed("Model loading failed"))
        }
    }

    /**
     * Esegue l'inferenza in modo sicuro.
     * @return La risposta del modello o null se non è pronto.
     */
    suspend fun generateResponse(prompt: String): String? = withContext(Dispatchers.Default) {
        if (!_isReady.value || llmInference == null) {
            Log.w("LlmManager", "Attempted to generate response, but LLM is not ready.")
            return@withContext null
        }

        return@withContext try {
            llmInference?.generateResponse(prompt)
        } catch (e: Exception) {
            Log.e("LlmManager", "Error during LLM inference", e)
            null
        }
    }
}