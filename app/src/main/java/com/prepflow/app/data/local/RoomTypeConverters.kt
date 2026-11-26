package com.prepflow.app.data.local

import androidx.room.TypeConverter
import com.prepflow.app.domain.model.HomeworkPriority
import com.prepflow.app.domain.model.HomeworkStatus
import com.prepflow.app.domain.model.ResourceUnitType

class RoomTypeConverters {

    @TypeConverter
    fun fromHomeworkPriority(value: HomeworkPriority): String = value.name

    @TypeConverter
    fun toHomeworkPriority(value: String): HomeworkPriority =
        runCatching { HomeworkPriority.valueOf(value) }.getOrDefault(HomeworkPriority.MEDIUM)

    @TypeConverter
    fun fromHomeworkStatus(value: HomeworkStatus): String = value.name

    @TypeConverter
    fun toHomeworkStatus(value: String): HomeworkStatus =
        runCatching { HomeworkStatus.valueOf(value) }.getOrDefault(HomeworkStatus.TODO)

    @TypeConverter
    fun fromResourceUnitType(value: ResourceUnitType): String = value.name

    @TypeConverter
    fun toResourceUnitType(value: String): ResourceUnitType =
        runCatching { ResourceUnitType.valueOf(value) }.getOrDefault(ResourceUnitType.UNIT)
}
