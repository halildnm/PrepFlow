package com.prepflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.prepflow.app.data.local.entity.ResourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {

    @Query("SELECT * FROM resources ORDER BY id DESC")
    fun observeAllResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources ORDER BY id DESC")
    suspend fun getAllResourcesSnapshot(): List<ResourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ResourceEntity>)

    @Update
    suspend fun updateResource(resource: ResourceEntity)

    @Query("UPDATE resources SET currentCount = :currentCount WHERE id = :resourceId")
    suspend fun updateResourceProgress(resourceId: Long, currentCount: Int)

    @Delete
    suspend fun deleteResource(resource: ResourceEntity)

    @Query("DELETE FROM resources WHERE id = :resourceId")
    suspend fun deleteResourceById(resourceId: Long)

    @Query("DELETE FROM resources")
    suspend fun clearAllResources()
}
