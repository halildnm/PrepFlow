package com.prepflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prepflow.app.data.local.entity.CustomSkillTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomSkillTagDao {

    @Query("SELECT * FROM custom_skill_tags ORDER BY name ASC")
    fun observeAllCustomTags(): Flow<List<CustomSkillTagEntity>>

    @Query("SELECT * FROM custom_skill_tags ORDER BY name ASC")
    suspend fun getAllCustomTagsSnapshot(): List<CustomSkillTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(tag: CustomSkillTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTags(tags: List<CustomSkillTagEntity>)

    @Query("DELETE FROM custom_skill_tags WHERE name = :name")
    suspend fun deleteTag(name: String)

    @Query("DELETE FROM custom_skill_tags")
    suspend fun clearAllTags()
}
