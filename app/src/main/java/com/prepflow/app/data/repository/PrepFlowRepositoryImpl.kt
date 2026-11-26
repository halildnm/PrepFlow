package com.prepflow.app.data.repository

import androidx.room.withTransaction
import com.prepflow.app.data.local.PrepFlowDatabase
import com.prepflow.app.data.local.entity.CustomSkillTagEntity
import com.prepflow.app.data.local.entity.ExamEntity
import com.prepflow.app.data.local.entity.HomeworkEntity
import com.prepflow.app.data.local.entity.HomeworkLinkEntity
import com.prepflow.app.data.local.entity.HomeworkWithDetails
import com.prepflow.app.data.local.entity.ResourceEntity
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import com.prepflow.app.data.local.entity.SubtaskEntity
import com.prepflow.app.domain.model.HomeworkPriority
import com.prepflow.app.domain.model.HomeworkStatus
import com.prepflow.app.domain.model.PrepFlowBackupData
import com.prepflow.app.domain.model.ResourceUnitType
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.domain.repository.WidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class PrepFlowRepositoryImpl(
    private val database: PrepFlowDatabase,
    private val widgetUpdaterProvider: () -> WidgetUpdater
) : PrepFlowRepository {

    private val homeworkDao = database.homeworkDao()
    private val scheduleDao = database.scheduleDao()
    private val examDao = database.examDao()
    private val resourceDao = database.resourceDao()
    private val customTagDao = database.customSkillTagDao()

    private val jsonCodec = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun observeAllHomeworks(): Flow<List<HomeworkWithDetails>> =
        homeworkDao.observeAllWithDetails()

    override fun observeHomeworkById(homeworkId: Long): Flow<HomeworkWithDetails?> =
        homeworkDao.observeByIdWithDetails(homeworkId)

    override suspend fun getUpcomingActiveHomeworks(limit: Int): List<HomeworkEntity> =
        homeworkDao.getUpcomingActiveHomeworks(limit)

    override suspend fun saveHomeworkWithDetails(
        homework: HomeworkEntity,
        links: List<HomeworkLinkEntity>,
        subtasks: List<SubtaskEntity>
    ): Long {
        val savedId = database.withTransaction {
            val targetId = if (homework.id == 0L) {
                homeworkDao.insertHomework(homework)
            } else {
                homeworkDao.updateHomework(homework)
                homework.id
            }

            homeworkDao.deleteLinksByHomeworkId(targetId)
            val normalizedLinks = links
                .filter { it.url.isNotBlank() }
                .map {
                    it.copy(
                        id = 0L,
                        homeworkId = targetId,
                        label = it.label.ifBlank { "Bağlantı" }.trim(),
                        url = it.url.trim()
                    )
                }
            if (normalizedLinks.isNotEmpty()) {
                homeworkDao.insertLinks(normalizedLinks)
            }

            homeworkDao.deleteSubtasksByHomeworkId(targetId)
            val normalizedSubtasks = subtasks
                .filter { it.title.isNotBlank() }
                .map {
                    it.copy(
                        id = 0L,
                        homeworkId = targetId,
                        title = it.title.trim()
                    )
                }
            if (normalizedSubtasks.isNotEmpty()) {
                homeworkDao.insertSubtasks(normalizedSubtasks)
            }

            targetId
        }
        widgetUpdaterProvider().refreshWidgets()
        return savedId
    }

    override suspend fun updateHomeworkStatus(homeworkId: Long, status: HomeworkStatus) {
        homeworkDao.updateHomeworkStatus(homeworkId, status)
        widgetUpdaterProvider().refreshWidgets()
    }

    override suspend fun toggleSubtask(subtaskId: Long, isCompleted: Boolean) {
        homeworkDao.updateSubtaskCompletion(subtaskId, isCompleted)
    }

    override suspend fun deleteHomework(homeworkId: Long) {
        homeworkDao.deleteHomeworkById(homeworkId)
        widgetUpdaterProvider().refreshWidgets()
    }

    override fun observeAllScheduleSessions(): Flow<List<ScheduleSessionEntity>> =
        scheduleDao.observeAllSessions()

    override fun observeSessionsForDay(dayOfWeek: Int): Flow<List<ScheduleSessionEntity>> =
        scheduleDao.observeSessionsForDay(dayOfWeek)

    override suspend fun getSessionsForDaySnapshot(dayOfWeek: Int): List<ScheduleSessionEntity> =
        scheduleDao.getSessionsForDaySnapshot(dayOfWeek)

    override suspend fun saveScheduleSession(session: ScheduleSessionEntity): Long {
        val id = if (session.id == 0L) {
            scheduleDao.insertSession(session)
        } else {
            scheduleDao.updateSession(session)
            session.id
        }
        widgetUpdaterProvider().refreshWidgets()
        return id
    }

    override suspend fun deleteScheduleSession(sessionId: Long) {
        scheduleDao.deleteSessionById(sessionId)
        widgetUpdaterProvider().refreshWidgets()
    }

    override fun observeAllExams(): Flow<List<ExamEntity>> = examDao.observeAllExams()

    override suspend fun saveExam(exam: ExamEntity): Long {
        return if (exam.id == 0L) {
            examDao.insertExam(exam)
        } else {
            examDao.updateExam(exam)
            exam.id
        }
    }

    override suspend fun deleteExam(examId: Long) {
        examDao.deleteExamById(examId)
    }

    override fun observeAllResources(): Flow<List<ResourceEntity>> =
        resourceDao.observeAllResources()

    override suspend fun saveResource(resource: ResourceEntity): Long {
        val safeResource = resource.copy(
            totalCount = resource.totalCount.coerceAtLeast(1),
            currentCount = resource.currentCount.coerceIn(0, resource.totalCount.coerceAtLeast(1))
        )
        return if (safeResource.id == 0L) {
            resourceDao.insertResource(safeResource)
        } else {
            resourceDao.updateResource(safeResource)
            safeResource.id
        }
    }

    override suspend fun updateResourceProgress(resourceId: Long, newCount: Int) {
        resourceDao.updateResourceProgress(resourceId, newCount.coerceAtLeast(0))
    }

    override suspend fun deleteResource(resourceId: Long) {
        resourceDao.deleteResourceById(resourceId)
    }

    override fun observeCustomSkillTags(): Flow<List<CustomSkillTagEntity>> =
        customTagDao.observeAllCustomTags()

    override suspend fun addCustomSkillTag(name: String, colorHex: Long) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            customTagDao.upsertTag(CustomSkillTagEntity(name = trimmed, colorHex = colorHex))
        }
    }

    override suspend fun deleteCustomSkillTag(name: String) {
        customTagDao.deleteTag(name)
    }

    override suspend fun exportToJson(): String {
        val snapshot = PrepFlowBackupData(
            homeworks = homeworkDao.getAllHomeworksSnapshot(),
            homeworkLinks = homeworkDao.getAllLinksSnapshot(),
            subtasks = homeworkDao.getAllSubtasksSnapshot(),
            scheduleSessions = scheduleDao.getAllSessionsSnapshot(),
            exams = examDao.getAllExamsSnapshot(),
            resources = resourceDao.getAllResourcesSnapshot(),
            customSkillTags = customTagDao.getAllCustomTagsSnapshot()
        )
        return jsonCodec.encodeToString(snapshot)
    }

    override suspend fun importFromJson(jsonString: String) {
        val backup = jsonCodec.decodeFromString<PrepFlowBackupData>(jsonString)
        database.withTransaction {
            homeworkDao.clearAllHomeworks()
            scheduleDao.clearAllSessions()
            examDao.clearAllExams()
            resourceDao.clearAllResources()
            customTagDao.clearAllTags()

            backup.homeworks.forEach { homeworkDao.insertHomework(it) }
            if (backup.homeworkLinks.isNotEmpty()) homeworkDao.insertLinks(backup.homeworkLinks)
            if (backup.subtasks.isNotEmpty()) homeworkDao.insertSubtasks(backup.subtasks)
            if (backup.scheduleSessions.isNotEmpty()) scheduleDao.insertSessions(backup.scheduleSessions)
            if (backup.exams.isNotEmpty()) examDao.insertExams(backup.exams)
            if (backup.resources.isNotEmpty()) resourceDao.insertResources(backup.resources)
            if (backup.customSkillTags.isNotEmpty()) customTagDao.upsertTags(backup.customSkillTags)
        }
        widgetUpdaterProvider().refreshWidgets()
    }

    override suspend fun seedDemoData() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        fun millisAt(daysOffset: Long, hour: Int, minute: Int): Long {
            return today.plusDays(daysOffset)
                .atTime(LocalTime.of(hour, minute))
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        }

        database.withTransaction {
            val sampleSessions = listOf(
                ScheduleSessionEntity(
                    dayOfWeek = DayOfWeek.MONDAY.value,
                    title = "Main Course — B1+ Grammar",
                    skillTag = "Grammar",
                    startTime = "09:00",
                    endTime = "10:40",
                    roomOrNote = "B-204 • Unit 5 Conditionals"
                ),
                ScheduleSessionEntity(
                    dayOfWeek = DayOfWeek.MONDAY.value,
                    title = "Academic Reading Lab",
                    skillTag = "Reading",
                    startTime = "11:00",
                    endTime = "12:30",
                    roomOrNote = "B-204 • Skimming & Inference"
                ),
                ScheduleSessionEntity(
                    dayOfWeek = DayOfWeek.TUESDAY.value,
                    title = "Listening & Note-Taking",
                    skillTag = "Listening",
                    startTime = "09:30",
                    endTime = "11:10",
                    roomOrNote = "Dil Lab 2 • Lecture 4"
                ),
                ScheduleSessionEntity(
                    dayOfWeek = DayOfWeek.WEDNESDAY.value,
                    title = "Essay Writing Workshop",
                    skillTag = "Writing",
                    startTime = "10:00",
                    endTime = "11:50",
                    roomOrNote = "A-108 • Opinion Essay Taslağı"
                ),
                ScheduleSessionEntity(
                    dayOfWeek = DayOfWeek.THURSDAY.value,
                    title = "Speaking & Presentation",
                    skillTag = "Speaking",
                    startTime = "13:30",
                    endTime = "15:10",
                    roomOrNote = "C-301 • 3 Dakikalık Sunum"
                ),
                ScheduleSessionEntity(
                    dayOfWeek = DayOfWeek.FRIDAY.value,
                    title = "Vocabulary & Collocations",
                    skillTag = "Vocabulary",
                    startTime = "09:00",
                    endTime = "10:40",
                    roomOrNote = "B-204 • Academic Word List"
                )
            )
            val todayDow = today.dayOfWeek.value
            val extraTodaySessions = if (todayDow > 5) {
                listOf(
                    ScheduleSessionEntity(
                        dayOfWeek = todayDow,
                        title = "Hafta Sonu Tekrar — Grammar",
                        skillTag = "Grammar",
                        startTime = "11:00",
                        endTime = "12:30",
                        roomOrNote = "Kütüphane • Genel Tekrar"
                    ),
                    ScheduleSessionEntity(
                        dayOfWeek = todayDow,
                        title = "Mock Listening Pratiği",
                        skillTag = "Listening",
                        startTime = "14:00",
                        endTime = "15:15",
                        roomOrNote = "Ev Çalışması"
                    )
                )
            } else {
                emptyList()
            }
            scheduleDao.insertSessions(sampleSessions + extraTodaySessions)

            val hw1Id = homeworkDao.insertHomework(
                HomeworkEntity(
                    title = "Opinion Essay: Remote Education",
                    description = "En az 300 kelimelik 4 paragraflık taslak hazırla ve portala yükle.",
                    dueDateTime = millisAt(daysOffset = 1, hour = 23, minute = 59),
                    skillTag = "Writing",
                    priority = HomeworkPriority.URGENT,
                    status = HomeworkStatus.IN_PROGRESS
                )
            )
            homeworkDao.insertSubtasks(
                listOf(
                    SubtaskEntity(homeworkId = hw1Id, title = "Thesis statement ve ana fikirleri çıkar", isCompleted = true),
                    SubtaskEntity(homeworkId = hw1Id, title = "2 body paragrafını örneklerle yaz", isCompleted = true),
                    SubtaskEntity(homeworkId = hw1Id, title = "Bağlaçları ve dilbilgisini kontrol et", isCompleted = false),
                    SubtaskEntity(homeworkId = hw1Id, title = "Turnitin / Portal üzerinden teslim et", isCompleted = false)
                )
            )
            homeworkDao.insertLinks(
                listOf(
                    HomeworkLinkEntity(homeworkId = hw1Id, label = "Online Portal", url = "https://dictionary.cambridge.org"),
                    HomeworkLinkEntity(homeworkId = hw1Id, label = "Bağlaç Rehberi", url = "https://learnenglish.britishcouncil.org")
                )
            )

            val hw2Id = homeworkDao.insertHomework(
                HomeworkEntity(
                    title = "Unit 5 Workbook — Relative Clauses",
                    description = "Sayfa 42–45 arası tüm alıştırmalar tamamlanacak.",
                    dueDateTime = millisAt(daysOffset = -1, hour = 18, minute = 0),
                    skillTag = "Grammar",
                    priority = HomeworkPriority.URGENT,
                    status = HomeworkStatus.TODO
                )
            )
            homeworkDao.insertSubtasks(
                listOf(
                    SubtaskEntity(homeworkId = hw2Id, title = "Defining / Non-defining farkı tekrarı", isCompleted = true),
                    SubtaskEntity(homeworkId = hw2Id, title = "Sayfa 44–45 test soruları", isCompleted = false)
                )
            )

            val hw3Id = homeworkDao.insertHomework(
                HomeworkEntity(
                    title = "TED Talk Dinleme Özeti",
                    description = "Seçilen konuşmayı izleyip 10 akademik kelime ve 5 cümlelik özet yaz.",
                    dueDateTime = millisAt(daysOffset = 3, hour = 20, minute = 0),
                    skillTag = "Listening",
                    priority = HomeworkPriority.MEDIUM,
                    status = HomeworkStatus.TODO
                )
            )
            homeworkDao.insertLinks(
                listOf(
                    HomeworkLinkEntity(homeworkId = hw3Id, label = "TED Talks", url = "https://www.ted.com/talks")
                )
            )

            examDao.insertExams(
                listOf(
                    ExamEntity(
                        title = "Pop Quiz 1",
                        date = millisAt(daysOffset = -10, hour = 10, minute = 0),
                        coveredUnits = "Unit 1–2 Grammar & Vocab",
                        weightPercent = 10.0,
                        score = 86.0
                    ),
                    ExamEntity(
                        title = "Writing Portfolio 1",
                        date = millisAt(daysOffset = -4, hour = 14, minute = 0),
                        coveredUnits = "Cause & Effect Paragraph",
                        weightPercent = 15.0,
                        score = 78.5
                    ),
                    ExamEntity(
                        title = "Midterm 1 (Ara Sınav)",
                        date = millisAt(daysOffset = 5, hour = 9, minute = 30),
                        coveredUnits = "Unit 1–5 Tüm Beceriler",
                        weightPercent = 35.0,
                        score = null
                    )
                )
            )

            resourceDao.insertResources(
                listOf(
                    ResourceEntity(
                        name = "Main Course Book (B1+)",
                        unitType = ResourceUnitType.UNIT,
                        totalCount = 12,
                        currentCount = 5,
                        skillTag = "Grammar"
                    ),
                    ResourceEntity(
                        name = "Academic Reading Anthology",
                        unitType = ResourceUnitType.PAGE,
                        totalCount = 180,
                        currentCount = 74,
                        skillTag = "Reading"
                    ),
                    ResourceEntity(
                        name = "Online Practice Platform",
                        unitType = ResourceUnitType.UNIT,
                        totalCount = 10,
                        currentCount = 4,
                        skillTag = "Vocabulary"
                    )
                )
            )
        }
        widgetUpdaterProvider().refreshWidgets()
    }
}
