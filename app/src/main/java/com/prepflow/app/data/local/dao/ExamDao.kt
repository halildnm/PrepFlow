package com.prepflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.prepflow.app.data.local.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Query("SELECT * FROM exams ORDER BY date ASC")
    fun observeAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY date ASC")
    suspend fun getAllExamsSnapshot(): List<ExamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :examId")
    suspend fun deleteExamById(examId: Long)

    @Query("DELETE FROM exams")
    suspend fun clearAllExams()
}
