package com.prepflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = HomeworkEntity::class,
            parentColumns = ["id"],
            childColumns = ["homeworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["homeworkId"])]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val homeworkId: Long,
    val title: String,
    val isCompleted: Boolean = false
)
