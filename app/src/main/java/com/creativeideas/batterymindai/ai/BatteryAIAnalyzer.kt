package com.creativeideas.batterymindai.ai

import com.creativeideas.batterymindai.data.database.dao.UserHabitDao
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.models.AIRecommendation
import com.creativeideas.batterymindai.data.models.AppUsageInfo
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.data.repository.ModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryAIAnalyzer @Inject constructor(
    private val offlineReasoningEngine: OfflineReasoningEngine,
    private val onDeviceAIService: OnDeviceAIService, // <-- NUOVA DIPENDENZA
    private val modelRepository: ModelRepository,      // <-- NUOVA DIPENDENZA
    private val appPreferences: AppPreferences,        // <-- NUOVA DIPENDENZA
    private val userHabitDao: UserHabitDao
) {

    suspend fun generateRecommendations(
        batteryStats: BatteryStatsEntity,
        appUsage: List<AppUsageInfo>,
        history: List<BatteryStatsEntity>
    ): List<AIRecommendation> = withContext(Dispatchers.IO) {

        val useAdvancedMode = appPreferences.getAIMode() == "ADVANCED"
        val isModelReady = modelRepository.isModelReadyForInference()

        // [Principio Musk] Logica di decisione chiara e sequenziale.
        // Se l'utente vuole la modalità avanzata E il modello è pronto, la usiamo.
        // In tutti gli altri casi, usiamo il motore di base come fallback affidabile.
        if (useAdvancedMode && isModelReady) {
            try {
                val prompt = buildAdvancedPrompt(batteryStats, appUsage, history)
                val advancedSuggestions = onDeviceAIService.getSuggestions(prompt)

                // Se l'IA avanzata produce un risultato, lo usiamo.
                if (advancedSuggestions.isNotEmpty()) {
                    return@withContext mapToAIRecommendation(advancedSuggestions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Se l'inferenza fallisce, non ci arrendiamo. Facciamo fallback.
            }
        }

        // Fallback: Usa il motore a regole di base.
        val habitsMap = userHabitDao.getAllHabits().associate { it.key to it.value }
        val offlineSuggestions = offlineReasoningEngine.getOfflineSuggestions(batteryStats, appUsage, habitsMap)
        return@withContext mapToAIRecommendation(offlineSuggestions)
    }

    private fun mapToAIRecommendation(suggestions: List<GeneratedSuggestion>): List<AIRecommendation> {
        return suggestions.map { suggestion ->
            AIRecommendation(
                id = UUID.randomUUID().toString(),
                title = suggestion.title,
                description = suggestion.description,
                priority = suggestion.priority,
                category = suggestion.category,
                impact = suggestion.impact,
                actionable = suggestion.isActionable,
                action = suggestion.action.takeIf { suggestion.isActionable }
            )
        }
    }

    // Costruisce un prompt dettagliato per l'LLM.
    private fun buildAdvancedPrompt(
        stats: BatteryStatsEntity,
        usage: List<AppUsageInfo>,
        history: List<BatteryStatsEntity>
    ): String {
        // Questa è un'arte. Il prompt deve essere ingegnerizzato per ottenere risposte
        // concise e strutturate.
        val topApp = usage.firstOrNull()?.appName ?: "N/A"
        return """
        Sei BatteryMind AI, un assistente esperto di batterie per Android.
        Il tuo compito è fornire un suggerimento conciso, intelligente e personalizzato (massimo 2 frasi).
        Non salutare. Non usare frasi introduttive. Vai dritto al punto.

        CONTESTO ATTUALE:
        - Livello Batteria: ${stats.batteryLevel}%
        - In Carica: ${if (stats.isCharging) "Sì" else "No"}
        - Temperatura: ${stats.temperature}°C
        - App più usata di recente: $topApp

        Basandoti su questi dati, genera un singolo, utile suggerimento per l'utente.
        """.trimIndent()
    }
}