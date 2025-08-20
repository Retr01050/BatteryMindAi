package com.creativeideas.batterymindai.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.creativeideas.batterymindai.data.database.entities.AppUsageEntity
import java.util.Date

@Dao
interface AppUsageDao {

    @Query("SELECT * FROM app_usage ORDER BY timestamp DESC")
    fun getAllAppUsage(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE timestamp >= :startDate ORDER BY batteryUsage DESC")
    fun getTopBatteryConsumers(startDate: Date): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getAppUsageByPackage(packageName: String): Flow<List<AppUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsage(usage: AppUsageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsageList(usageList: List<AppUsageEntity>)

    @Query("DELETE FROM app_usage WHERE timestamp < :cutoffDate")
    suspend fun deleteOldUsage(cutoffDate: Date)
}
