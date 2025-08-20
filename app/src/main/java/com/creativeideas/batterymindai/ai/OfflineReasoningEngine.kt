// C:\Users\Alessandro\AndroidStudioProjects\BatteryMind\app\src\main\java\com\creativeideas\batterymindai\ai\OfflineReasoningEngine.kt

package com.creativeideas.batterymindai.ai

import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.models.AppUsageInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineReasoningEngine @Inject constructor() {

    /**
     * Genera suggerimenti basati su una logica a regole complessa, usando tutti i dati disponibili.
     * La firma del metodo ora è valida perché 'GeneratedSuggestion' è un tipo risolvibile.
     */
    fun getOfflineSuggestions(
        stats: BatteryStatsEntity,
        appUsage: List<AppUsageInfo>,
        habits: Map<String, String>
    ): List<GeneratedSuggestion> { // <-- FIX: Questo ora viene risolto correttamente.
        val suggestions = mutableListOf<GeneratedSuggestion>()
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        // REGOLA 1: Temperatura critica
        if (stats.temperature > 42.0f) {
            suggestions.add(createSuggestion("Critical Temperature", "Temperature is ${stats.temperature}°C. Close heavy apps and stop charging immediately.", "High", "Temperature", "High", action = "emergency_cooling", isActionable = false))
        }

        // REGOLA 2: Batteria scarica CONTESTUALE
        val avgChargeHour = habits["avg_charge_time_hour"]?.toIntOrNull()
        if (stats.batteryLevel < 20 && !stats.isCharging) {
            val title = "Low Battery Alert"
            var description = "Battery is at ${stats.batteryLevel}%. Enable power saving mode to extend usage."
            if (avgChargeHour != null && currentHour < avgChargeHour - 2) {
                description += " You usually charge around ${avgChargeHour}:00."
            }
            suggestions.add(createSuggestion(title, description, "High", "Power", "High", action = "enable_power_save", isActionable = true))
        }

        // REGOLA 3: Suggerimento proattivo basato sulle abitudini
        if (avgChargeHour != null && stats.batteryLevel < 35 && !stats.isCharging && currentHour in (avgChargeHour - 2)..(avgChargeHour)) {
            suggestions.add(createSuggestion("Habit Reminder", "It's almost your usual charging time (${avgChargeHour}:00) and battery is low. Good time to plug it in.", "Medium", "Charging", "Low", action = null, isActionable = false))
        }

        // REGOLA 4: Consumo anomalo di un'app
        val topConsumer = appUsage.firstOrNull()
        if (topConsumer != null && topConsumer.batteryPercentage > 25) {
            suggestions.add(createSuggestion("High Usage: ${topConsumer.appName}", "This app has used over 25% of your battery. Consider restricting its background activity.", "Medium", "Apps", "Medium", action = "optimize_app_${topConsumer.packageName}", isActionable = true))
        }

        // REGOLA 5: Suggerimento per la salute della batteria durante la notte
        val isNightTime = currentHour >= 23 || currentHour <= 5
        if (isNightTime && stats.isCharging && stats.batteryLevel > 90) {
            suggestions.add(createSuggestion("Protect Battery Health", "Battery is almost full. Unplugging overnight can extend its lifespan.", "Low", "Charging", "Medium", action = "notify_unplug", isActionable = false))
        }

        return suggestions.distinctBy { it.title }.sortedByDescending { it.priority == "High" }.take(2)
    }

    /**
     * Helper per creare un'istanza di GeneratedSuggestion. Anche questo ora è valido.
     */
    private fun createSuggestion(title: String, description: String, priority: String, category: String, impact: String, action: String?, isActionable: Boolean): GeneratedSuggestion {
        return GeneratedSuggestion(title, description, priority, category, impact, action, isActionable)
    }
}