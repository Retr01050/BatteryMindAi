package com.creativeideas.batterymindai.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * [Principio Jobs] Un servizio semplice e pulito. La sua unica responsabilità è
 * fare da ponte tra la logica dell'app (l'Analyzer) e il gestore del modello (LlmManager).
 * Non conosce i dettagli dell'implementazione, chiede solo un risultato.
 */
@Singleton
class OnDeviceAIService @Inject constructor(
    private val llmManager: LlmManager
) {

    /**
     * Genera suggerimenti usando il modello on-device caricato.
     * @param prompt Il contesto e i dati formattati per il modello.
     * @return Una lista di suggerimenti grezzi, o una lista vuota se l'IA non è pronta o fallisce.
     */
    suspend fun getSuggestions(prompt: String): List<GeneratedSuggestion> {
        if (!llmManager.isReady.value) {
            return emptyList()
        }

        val rawResponse = llmManager.generateResponse(prompt) ?: return emptyList()

        // Qui avremo la logica per parsare la 'rawResponse' (che è una stringa)
        // e trasformarla in una List<GeneratedSuggestion>.
        // Per ora, simuliamo il parsing.
        // TODO: Implementare un parser robusto per l'output del LLM.
        return parseResponse(rawResponse)
    }

    // Funzione di parsing di placeholder. Andrà sostituita con una più intelligente.
    private fun parseResponse(rawResponse: String): List<GeneratedSuggestion> {
        // Esempio molto basilare. In un caso reale, ci si aspetterebbe un output JSON
        // o un formato strutturato dal prompt.
        val suggestions = mutableListOf<GeneratedSuggestion>()
        if (rawResponse.contains("temperatura")) {
            suggestions.add(
                GeneratedSuggestion(
                    title = "Temperature Alert",
                    description = "Your device is running hot. Consider closing background apps.",
                    priority = "High",
                    category = "Temperature",
                    impact = "Medium",
                    action = "thermal_optimize",
                    isActionable = true
                )
            )
        }
        suggestions.add(
            GeneratedSuggestion(
                title = "AI Insight",
                description = rawResponse.take(150), // Prendi una parte della risposta grezza
                priority = "Medium",
                category = "General",
                impact = "Low",
                action = null,
                isActionable = false
            )
        )
        return suggestions
    }
}