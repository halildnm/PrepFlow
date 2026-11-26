package com.prepflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "schedule_sessions")
data class ScheduleSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dayOfWeek: Int,
    val title: String,
    val skillTag: String,
    val startTime: String,
    val endTime: String,
    val roomOrNote: String = ""
)
