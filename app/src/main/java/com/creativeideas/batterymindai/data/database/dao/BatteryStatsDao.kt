package com.creativeideas.batterymindai.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity
import java.util.Date

@Dao
interface BatteryStatsDao {

    @Query("SELECT * FROM battery_stats ORDER BY timestamp DESC")
    fun getAllBatteryStats(): Flow<List<BatteryStatsEntity>>

    @Query("SELECT * FROM battery_stats WHERE timestamp >= :startDate ORDER BY timestamp DESC")
    fun getBatteryStatsFromDate(startDate: Date): Flow<List<BatteryStatsEntity>>

    @Query("SELECT * FROM battery_stats ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestBatteryStats(): BatteryStatsEntity?

    @Query("SELECT AVG(batteryScore) FROM battery_stats WHERE timestamp >= :startDate")
    suspend fun getAverageBatteryScore(startDate: Date): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryStats(stats: BatteryStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryStatsList(statsList: List<BatteryStatsEntity>)

    @Query("DELETE FROM battery_stats WHERE timestamp < :cutoffDate")
    suspend fun deleteOldStats(cutoffDate: Date)
}
