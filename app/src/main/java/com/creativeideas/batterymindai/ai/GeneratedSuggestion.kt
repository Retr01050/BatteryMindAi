// C:\Users\Alessandro\AndroidStudioProjects\BatteryMind\app\src\main\java\com\creativeideas\batterymindai\ai\GeneratedSuggestion.kt
package com.creativeideas.batterymindai.ai

/**
 * Un data class che rappresenta un suggerimento grezzo generato da un motore di IA (online o offline).
 * Serve come "contratto" interno al modulo AI prima della mappatura finale su AIRecommendation.
 */
data class GeneratedSuggestion(
    val title: String,
    val description: String,
    val priority: String,       // "High", "Medium", "Low"
    val category: String,       // "Temperature", "Power", "Apps", "Charging"
    val impact: String,         // "High", "Medium", "Low"
    val action: String?,        // Azione eseguibile (es. "enable_power_save")
    val isActionable: Boolean
)