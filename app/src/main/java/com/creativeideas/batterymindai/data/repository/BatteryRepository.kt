package com.creativeideas.batterymindai.data.repository

import com.creativeideas.batterymindai.data.database.dao.BatteryStatsDao
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import com.creativeideas.batterymindai.data.sensors.BatteryDataCollector
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryRepository @Inject constructor(
    // FIX: Assicuriamoci che le dipendenze siano corrette
    private val batteryDataCollector: BatteryDataCollector,
    private val batteryStatsDao: BatteryStatsDao
) {

    suspend fun getCurrentBatteryInfo(): BatteryStatsEntity {
        return batteryDataCollector.getCurrentBatteryStats()
    }

    fun getAllBatteryStats(): Flow<List<BatteryStatsEntity>> {
        return batteryStatsDao.getAllBatteryStats()
    }

    fun getBatteryStatsFromDate(startDate: Date): Flow<List<BatteryStatsEntity>> {
        return batteryStatsDao.getBatteryStatsFromDate(startDate)
    }

    suspend fun getLatestBatteryStats(): BatteryStatsEntity? {
        return batteryStatsDao.getLatestBatteryStats()
    }

    suspend fun insertBatteryStats(stats: BatteryStatsEntity) {
        batteryStatsDao.insertBatteryStats(stats)
    }
}