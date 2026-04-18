package com.hom8.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hom8.app.data.local.dao.ActivityLogDao
import com.hom8.app.data.local.dao.ExpenseDao
import com.hom8.app.data.local.dao.HomeDao
import com.hom8.app.data.local.dao.LabelDao
import com.hom8.app.data.local.dao.NotificationDao
import com.hom8.app.data.local.dao.PaymentDao
import com.hom8.app.data.local.dao.TaskDao
import com.hom8.app.data.local.dao.UserDao
import com.hom8.app.data.local.dao.UserStatsDao
import com.hom8.app.data.local.entity.ActivityLogEntity
import com.hom8.app.data.local.entity.ExpenseEntity
import com.hom8.app.data.local.entity.HomeEntity
import com.hom8.app.data.local.entity.LabelEntity
import com.hom8.app.data.local.entity.NotificationEntity
import com.hom8.app.data.local.entity.PaymentEntity
import com.hom8.app.data.local.entity.TaskEntity
import com.hom8.app.data.local.entity.UserEntity
import com.hom8.app.data.local.entity.UserStatsEntity

@Database(
    entities = [
        TaskEntity::class,
        UserEntity::class,
        HomeEntity::class,
        LabelEntity::class,
        ExpenseEntity::class,
        PaymentEntity::class,
        NotificationEntity::class,
        ActivityLogEntity::class,
        UserStatsEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class HomeFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun homeDao(): HomeDao
    abstract fun labelDao(): LabelDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun userStatsDao(): UserStatsDao
}
