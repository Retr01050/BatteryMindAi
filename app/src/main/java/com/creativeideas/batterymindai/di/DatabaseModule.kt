package com.creativeideas.batterymindai.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.creativeideas.batterymindai.data.database.BatteryDatabase
import com.creativeideas.batterymindai.data.database.dao.AppUsageDao
import com.creativeideas.batterymindai.data.database.dao.BatteryStatsDao
import com.creativeideas.batterymindai.data.database.dao.UserHabitDao
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import com.creativeideas.batterymindai.data.repository.BatteryRepository
import com.creativeideas.batterymindai.data.repository.ModelRepository // <-- FIX: IMPORT MANCANTE AGGIUNTO
import com.creativeideas.batterymindai.data.sensors.AppUsageAnalyzer
import com.creativeideas.batterymindai.data.sensors.BatteryDataCollector
import com.creativeideas.batterymindai.data.sensors.SystemSensorManager
import com.creativeideas.batterymindai.utils.BatteryOptimizer
import com.creativeideas.batterymindai.utils.PermissionManager
import com.creativeideas.batterymindai.utils.RecommendationExecutor
import com.creativeideas.batterymindai.utils.SystemSettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }

    @Provides
    @Singleton
    fun provideBatteryDatabase(@ApplicationContext context: Context): BatteryDatabase {
        return Room.databaseBuilder(
            context,
            BatteryDatabase::class.java,
            BatteryDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBatteryStatsDao(database: BatteryDatabase): BatteryStatsDao {
        return database.batteryStatsDao()
    }

    @Provides
    @Singleton
    fun provideAppUsageDao(database: BatteryDatabase): AppUsageDao {
        return database.appUsageDao()
    }

    @Provides
    @Singleton
    fun provideUserHabitDao(database: BatteryDatabase): UserHabitDao {
        return database.userHabitDao()
    }

    @Provides
    @Singleton
    fun provideBatteryDataCollector(@ApplicationContext context: Context): BatteryDataCollector {
        return BatteryDataCollector(context)
    }

    @Provides
    @Singleton
    fun provideBatteryRepository(
        batteryDataCollector: BatteryDataCollector,
        batteryStatsDao: BatteryStatsDao
    ): BatteryRepository {
        return BatteryRepository(batteryDataCollector, batteryStatsDao)
    }

    @Provides
    @Singleton
    fun provideSystemSensorManager(@ApplicationContext context: Context): SystemSensorManager {
        return SystemSensorManager(context)
    }

    @Provides
    @Singleton
    fun provideAppUsageAnalyzer(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences
    ): AppUsageAnalyzer {
        return AppUsageAnalyzer(context, appPreferences)
    }

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideSystemSettingsManager(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences
    ): SystemSettingsManager {
        return SystemSettingsManager(context, appPreferences)
    }

    @Provides
    @Singleton
    fun provideBatteryOptimizer(
        @ApplicationContext context: Context,
        systemSettingsManager: SystemSettingsManager
    ): BatteryOptimizer {
        return BatteryOptimizer(context, systemSettingsManager)
    }

    @Provides
    @Singleton
    fun provideRecommendationExecutor(
        @ApplicationContext context: Context,
        systemSettingsManager: SystemSettingsManager,
        batteryOptimizer: BatteryOptimizer
    ): RecommendationExecutor {
        return RecommendationExecutor(context, systemSettingsManager, batteryOptimizer)
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60000
            }
        }
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    // Provider per ModelRepository che ora compilerà correttamente
    @Provides
    @Singleton
    fun provideModelRepository(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences,
        workManager: WorkManager
    ): ModelRepository {
        return ModelRepository(context, appPreferences, workManager)
    }
}