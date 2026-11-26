package com.prepflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.prepflow.app.data.local.entity.HomeworkEntity
import com.prepflow.app.data.local.entity.HomeworkLinkEntity
import com.prepflow.app.data.local.entity.HomeworkWithDetails
import com.prepflow.app.data.local.entity.SubtaskEntity
import com.prepflow.app.domain.model.HomeworkStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {

    @Transaction
    @Query("SELECT * FROM homeworks ORDER BY dueDateTime ASC")
    fun observeAllWithDetails(): Flow<List<HomeworkWithDetails>>

    @Transaction
    @Query("SELECT * FROM homeworks WHERE id = :homeworkId LIMIT 1")
    fun observeByIdWithDetails(homeworkId: Long): Flow<HomeworkWithDetails?>

    @Query("SELECT * FROM homeworks WHERE status != 'DONE' ORDER BY dueDateTime ASC LIMIT :limit")
    suspend fun getUpcomingActiveHomeworks(limit: Int): List<HomeworkEntity>

    @Query("SELECT * FROM homeworks")
    suspend fun getAllHomeworksSnapshot(): List<HomeworkEntity>

    @Query("SELECT * FROM homework_links")
    suspend fun getAllLinksSnapshot(): List<HomeworkLinkEntity>

    @Query("SELECT * FROM subtasks")
    suspend fun getAllSubtasksSnapshot(): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity): Long

    @Update
    suspend fun updateHomework(homework: HomeworkEntity)

    @Query("UPDATE homeworks SET status = :status WHERE id = :homeworkId")
    suspend fun updateHomeworkStatus(homeworkId: Long, status: HomeworkStatus)

    @Delete
    suspend fun deleteHomework(homework: HomeworkEntity)

    @Query("DELETE FROM homeworks WHERE id = :homeworkId")
    suspend fun deleteHomeworkById(homeworkId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<HomeworkLinkEntity>)

    @Query("DELETE FROM homework_links WHERE homeworkId = :homeworkId")
    suspend fun deleteLinksByHomeworkId(homeworkId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Query("DELETE FROM subtasks WHERE homeworkId = :homeworkId")
    suspend fun deleteSubtasksByHomeworkId(homeworkId: Long)

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :subtaskId")
    suspend fun updateSubtaskCompletion(subtaskId: Long, isCompleted: Boolean)

    @Query("DELETE FROM homeworks")
    suspend fun clearAllHomeworks()
}
