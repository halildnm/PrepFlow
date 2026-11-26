package com.prepflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "custom_skill_tags")
data class CustomSkillTagEntity(
    @PrimaryKey
    val name: String,
    val colorHex: Long
)
