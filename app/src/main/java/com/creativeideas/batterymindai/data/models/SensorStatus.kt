package com.creativeideas.batterymindai.data.models

data class SensorStatus(
    val name: String,
    val isActive: Boolean,
    val powerConsumption: Float, // Deve essere Float
    val description: String
)