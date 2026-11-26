package com.prepflow.app.domain.repository

import com.prepflow.app.data.local.entity.CustomSkillTagEntity
import com.prepflow.app.data.local.entity.ExamEntity
import com.prepflow.app.data.local.entity.HomeworkEntity
import com.prepflow.app.data.local.entity.HomeworkLinkEntity
import com.prepflow.app.data.local.entity.HomeworkWithDetails
import com.prepflow.app.data.local.entity.ResourceEntity
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import com.prepflow.app.data.local.entity.SubtaskEntity
import com.prepflow.app.domain.model.HomeworkStatus
import kotlinx.coroutines.flow.Flow

interface PrepFlowRepository {

    fun observeAllHomeworks(): Flow<List<HomeworkWithDetails>>
    fun observeHomeworkById(homeworkId: Long): Flow<HomeworkWithDetails?>
    suspend fun getUpcomingActiveHomeworks(limit: Int = 3): List<HomeworkEntity>
    suspend fun saveHomeworkWithDetails(
        homework: HomeworkEntity,
        links: List<HomeworkLinkEntity>,
        subtasks: List<SubtaskEntity>
    ): Long
    suspend fun updateHomeworkStatus(homeworkId: Long, status: HomeworkStatus)
    suspend fun toggleSubtask(subtaskId: Long, isCompleted: Boolean)
    suspend fun deleteHomework(homeworkId: Long)

    fun observeAllScheduleSessions(): Flow<List<ScheduleSessionEntity>>
    fun observeSessionsForDay(dayOfWeek: Int): Flow<List<ScheduleSessionEntity>>
    suspend fun getSessionsForDaySnapshot(dayOfWeek: Int): List<ScheduleSessionEntity>
    suspend fun saveScheduleSession(session: ScheduleSessionEntity): Long
    suspend fun deleteScheduleSession(sessionId: Long)

    fun observeAllExams(): Flow<List<ExamEntity>>
    suspend fun saveExam(exam: ExamEntity): Long
    suspend fun deleteExam(examId: Long)

    fun observeAllResources(): Flow<List<ResourceEntity>>
    suspend fun saveResource(resource: ResourceEntity): Long
    suspend fun updateResourceProgress(resourceId: Long, newCount: Int)
    suspend fun deleteResource(resourceId: Long)

    fun observeCustomSkillTags(): Flow<List<CustomSkillTagEntity>>
    suspend fun addCustomSkillTag(name: String, colorHex: Long)
    suspend fun deleteCustomSkillTag(name: String)

    suspend fun exportToJson(): String
    suspend fun importFromJson(jsonString: String)
    suspend fun seedDemoData()
}
