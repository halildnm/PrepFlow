package com.prepflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.prepflow.app.data.local.dao.CustomSkillTagDao
import com.prepflow.app.data.local.dao.ExamDao
import com.prepflow.app.data.local.dao.HomeworkDao
import com.prepflow.app.data.local.dao.ResourceDao
import com.prepflow.app.data.local.dao.ScheduleDao
import com.prepflow.app.data.local.entity.CustomSkillTagEntity
import com.prepflow.app.data.local.entity.ExamEntity
import com.prepflow.app.data.local.entity.HomeworkEntity
import com.prepflow.app.data.local.entity.HomeworkLinkEntity
import com.prepflow.app.data.local.entity.ResourceEntity
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import com.prepflow.app.data.local.entity.SubtaskEntity

@Database(
    entities = [
        HomeworkEntity::class,
        HomeworkLinkEntity::class,
        SubtaskEntity::class,
        ScheduleSessionEntity::class,
        ExamEntity::class,
        ResourceEntity::class,
        CustomSkillTagEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class PrepFlowDatabase : RoomDatabase() {

    abstract fun homeworkDao(): HomeworkDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun examDao(): ExamDao
    abstract fun resourceDao(): ResourceDao
    abstract fun customSkillTagDao(): CustomSkillTagDao

    companion object {
        private const val DATABASE_NAME = "prepflow_offline.db"

        @Volatile
        private var INSTANCE: PrepFlowDatabase? = null

        fun getInstance(context: Context): PrepFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PrepFlowDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
        }
    }
}
