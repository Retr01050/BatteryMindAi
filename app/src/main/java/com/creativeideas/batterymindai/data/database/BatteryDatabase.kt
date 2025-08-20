package com.creativeideas.batterymindai.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.creativeideas.batterymindai.data.database.dao.BatteryStatsDao
import com.creativeideas.batterymindai.data.database.dao.AppUsageDao
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.database.entities.AppUsageEntity
import com.creativeideas.batterymindai.data.database.converters.DateConverters
import com.creativeideas.batterymindai.data.database.dao.UserHabitDao
import com.creativeideas.batterymindai.data.database.entities.UserHabitEntity

@Database(
    entities = [
        BatteryStatsEntity::class,
        AppUsageEntity::class,
        UserHabitEntity::class // <-- Aggiungi questo
    ],
    version = 2, // <-- IMPORTANTE: Incrementa la versione del database
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class BatteryDatabase : RoomDatabase() {

    abstract fun batteryStatsDao(): BatteryStatsDao
    abstract fun appUsageDao(): AppUsageDao

    abstract fun userHabitDao(): UserHabitDao // <-- Aggiungi questo
    companion object {
        const val DATABASE_NAME = "battery_mind_database"
    }
}
