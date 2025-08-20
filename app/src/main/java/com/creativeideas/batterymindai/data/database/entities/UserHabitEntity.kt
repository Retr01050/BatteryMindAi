package com.creativeideas.batterymindai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_habits")
data class UserHabitEntity(
    @PrimaryKey val key: String, // Es. "avg_charge_time_hour", "most_used_app_evening"
    val value: String
)