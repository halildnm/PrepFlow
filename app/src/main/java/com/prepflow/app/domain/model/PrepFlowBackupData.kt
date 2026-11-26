package com.prepflow.app.domain.model

import com.prepflow.app.data.local.entity.CustomSkillTagEntity
import com.prepflow.app.data.local.entity.ExamEntity
import com.prepflow.app.data.local.entity.HomeworkEntity
import com.prepflow.app.data.local.entity.HomeworkLinkEntity
import com.prepflow.app.data.local.entity.ResourceEntity
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import com.prepflow.app.data.local.entity.SubtaskEntity
import kotlinx.serialization.Serializable

@Serializable
data class PrepFlowBackupData(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val homeworks: List<HomeworkEntity> = emptyList(),
    val homeworkLinks: List<HomeworkLinkEntity> = emptyList(),
    val subtasks: List<SubtaskEntity> = emptyList(),
    val scheduleSessions: List<ScheduleSessionEntity> = emptyList(),
    val exams: List<ExamEntity> = emptyList(),
    val resources: List<ResourceEntity> = emptyList(),
    val customSkillTags: List<CustomSkillTagEntity> = emptyList()
)
