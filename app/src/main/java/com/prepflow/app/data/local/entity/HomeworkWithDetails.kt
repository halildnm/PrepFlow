package com.prepflow.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HomeworkWithDetails(
    @Embedded
    val homework: HomeworkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "homeworkId"
    )
    val links: List<HomeworkLinkEntity> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "homeworkId"
    )
    val subtasks: List<SubtaskEntity> = emptyList()
) {
    val completedSubtaskCount: Int
        get() = subtasks.count { it.isCompleted }

    val totalSubtaskCount: Int
        get() = subtasks.size

    val subtaskProgress: Float
        get() = if (subtasks.isEmpty()) 0f else completedSubtaskCount.toFloat() / subtasks.size.toFloat()
}
