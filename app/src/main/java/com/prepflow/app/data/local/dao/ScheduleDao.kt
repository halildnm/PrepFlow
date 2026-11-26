package com.prepflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_sessions ORDER BY dayOfWeek ASC, startTime ASC")
    fun observeAllSessions(): Flow<List<ScheduleSessionEntity>>

    @Query("SELECT * FROM schedule_sessions WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun observeSessionsForDay(dayOfWeek: Int): Flow<List<ScheduleSessionEntity>>

    @Query("SELECT * FROM schedule_sessions WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    suspend fun getSessionsForDaySnapshot(dayOfWeek: Int): List<ScheduleSessionEntity>

    @Query("SELECT * FROM schedule_sessions ORDER BY dayOfWeek ASC, startTime ASC")
    suspend fun getAllSessionsSnapshot(): List<ScheduleSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScheduleSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ScheduleSessionEntity>)

    @Update
    suspend fun updateSession(session: ScheduleSessionEntity)

    @Delete
    suspend fun deleteSession(session: ScheduleSessionEntity)

    @Query("DELETE FROM schedule_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM schedule_sessions")
    suspend fun clearAllSessions()
}
