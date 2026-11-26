package com.prepflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prepflow.app.domain.model.ResourceUnitType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val unitType: ResourceUnitType = ResourceUnitType.UNIT,
    val totalCount: Int,
    val currentCount: Int = 0,
    val skillTag: String
) {
    val progressFraction: Float
        get() = if (totalCount <= 0) 0f else (currentCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
}
