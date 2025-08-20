package com.creativeideas.batterymindai.data.database.dao

import androidx.room.*
import com.creativeideas.batterymindai.data.database.entities.UserHabitEntity

@Dao
interface UserHabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabit(habit: UserHabitEntity)

    @Query("SELECT * FROM user_habits")
    suspend fun getAllHabits(): List<UserHabitEntity>

    @Query("SELECT * FROM user_habits WHERE `key` = :key LIMIT 1")
    suspend fun getHabit(key: String): UserHabitEntity?
}