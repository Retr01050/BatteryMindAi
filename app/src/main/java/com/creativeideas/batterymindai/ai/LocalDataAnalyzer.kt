package com.creativeideas.batterymindai.ai

import com.creativeideas.batterymindai.data.database.dao.UserHabitDao
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.database.entities.UserHabitEntity
import com.creativeideas.batterymindai.data.models.AppUsageInfo
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataAnalyzer @Inject constructor(
    private val userHabitDao: UserHabitDao
) {

    /**
     * Analizza la cronologia per imparare nuove abitudini e le salva nel database.
     * Questa funzione dovrebbe essere eseguita periodicamente in background (es. da un Worker).
     */
    suspend fun learnAndSaveHabits(
        batteryHistory: List<BatteryStatsEntity>,
        appUsageHistory: List<AppUsageInfo> // Richiede anche l'uso delle app per un'analisi completa
    ) {
        if (batteryHistory.size < 50) return // Serve una quantità di dati significativa

        val calendar = Calendar.getInstance()

        // --- Abitudine 1: Orario medio di inizio carica ---
        val chargeStartHours = batteryHistory.zipWithNext()
            .filter { (prev, curr) -> !prev.isCharging && curr.isCharging }
            .map { (_, curr) ->
                calendar.time = curr.timestamp
                calendar.get(Calendar.HOUR_OF_DAY)
            }
        if (chargeStartHours.isNotEmpty()) {
            val avgChargeHour = chargeStartHours.average().toInt()
            userHabitDao.upsertHabit(UserHabitEntity("avg_charge_time_hour", avgChargeHour.toString()))
        }

        // --- Abitudine 2: Livello medio della batteria a fine giornata (es. alle 22:00) ---
        val endOfDayLevels = batteryHistory.mapNotNull {
            calendar.time = it.timestamp
            if (calendar.get(Calendar.HOUR_OF_DAY) == 22) {
                it.batteryLevel
            } else {
                null
            }
        }
        if (endOfDayLevels.isNotEmpty()) {
            val avgEndOfDayLevel = endOfDayLevels.average().toInt()
            userHabitDao.upsertHabit(UserHabitEntity("avg_eod_battery_level", avgEndOfDayLevel.toString()))
        }

        // --- Abitudine 3: Giorno della settimana con il maggior consumo ---
        val consumptionByDay = batteryHistory
            .groupBy {
                calendar.time = it.timestamp
                calendar.get(Calendar.DAY_OF_WEEK)
            }
            .mapValues { (_, entries) ->
                // Calcola la "discesa" totale di batteria per quel giorno
                val dailyDrain = entries.zipWithNext()
                    .filterNot { (prev, curr) -> curr.isCharging }
                    .sumOf { (prev, curr) -> (prev.batteryLevel - curr.batteryLevel).coerceAtLeast(0) }
                dailyDrain
            }
        val heaviestDay = consumptionByDay.maxByOrNull { it.value }?.key
        if (heaviestDay != null) {
            val dayString = when (heaviestDay) {
                Calendar.SUNDAY -> "Sunday"
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                Calendar.SATURDAY -> "Saturday"
                else -> "Unknown"
            }
            userHabitDao.upsertHabit(UserHabitEntity("heaviest_usage_day", dayString))
        }

        // --- Abitudine 4: App più usata la sera (dalle 19:00 in poi) ---
        // Nota: per questo servirebbe una cronologia dell'uso delle app, che ora non abbiamo.
        // Simuliamo avendo la lista di uso delle ultime 24h.
        val eveningTopApp = appUsageHistory
            // Questo filtro è una semplificazione, idealmente filtreremmo per timestamp
            .maxByOrNull { it.usageTime }
        if (eveningTopApp != null) {
            userHabitDao.upsertHabit(UserHabitEntity("top_evening_app", eveningTopApp.appName))
        }

        // --- Abitudine 5: Tendenza della salute della batteria ---
        val firstHalfScore = batteryHistory.take(batteryHistory.size / 2).map { it.batteryScore }.average()
        val secondHalfScore = batteryHistory.drop(batteryHistory.size / 2).map { it.batteryScore }.average()
        val healthTrend = when {
            secondHalfScore < firstHalfScore - 5 -> "Declining"
            secondHalfScore > firstHalfScore + 5 -> "Improving"
            else -> "Stable"
        }
        userHabitDao.upsertHabit(UserHabitEntity("battery_health_trend", healthTrend))
    }

    /**
     * Analizza una lista di dati storici della batteria e ne estrae pattern significativi
     * per l'invio immediato all'IA (non salva nulla).
     */
    fun analyzeUsagePatterns(history: List<BatteryStatsEntity>): List<String> {
        if (history.size < 20) return emptyList()
        val patterns = mutableListOf<String>()
        val chargingSessions = history.zipWithNext().count { (prev, curr) -> !prev.isCharging && curr.isCharging }
        if (chargingSessions > 5) {
            patterns.add("FREQUENT_CHARGING")
        }
        val avgTemp = history.map { it.temperature }.average()
        if (avgTemp > 35.0) {
            patterns.add("HIGH_AVG_TEMPERATURE")
        }
        return patterns
    }
}