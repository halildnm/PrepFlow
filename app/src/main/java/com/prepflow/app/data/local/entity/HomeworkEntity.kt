package com.prepflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prepflow.app.domain.model.HomeworkPriority
import com.prepflow.app.domain.model.HomeworkStatus
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "homeworks")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val dueDateTime: Long,
    val skillTag: String,
    val priority: HomeworkPriority = HomeworkPriority.MEDIUM,
    val status: HomeworkStatus = HomeworkStatus.TODO,
    val createdAt: Long = System.currentTimeMillis()
)
