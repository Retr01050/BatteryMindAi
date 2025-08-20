package com.creativeideas.batterymindai.data.models

// FIX: This is now the single, authoritative definition for AIRecommendation.
// It includes all fields needed by both BatteryAIAnalyzer and IntelligentBatteryAI.
data class AIRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val priority: String,       // "High", "Medium", "Low"
    val category: String,       // "Temperature", "Power", "Apps", "Charging"
    val impact: String,         // "High", "Medium", "Low"
    val actionable: Boolean,
    val action: String? = null, // Action to perform (e.g., "disable_gps")
    val estimatedSavings: String? = null
)