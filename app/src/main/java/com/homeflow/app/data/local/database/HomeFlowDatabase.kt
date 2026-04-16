package com.homeflow.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.homeflow.app.data.local.dao.ActivityLogDao
import com.homeflow.app.data.local.dao.ExpenseDao
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.dao.LabelDao
import com.homeflow.app.data.local.dao.NotificationDao
import com.homeflow.app.data.local.dao.PaymentDao
import com.homeflow.app.data.local.dao.TaskDao
import com.homeflow.app.data.local.dao.UserDao
import com.homeflow.app.data.local.entity.ActivityLogEntity
import com.homeflow.app.data.local.entity.ExpenseEntity
import com.homeflow.app.data.local.entity.HomeEntity
import com.homeflow.app.data.local.entity.LabelEntity
import com.homeflow.app.data.local.entity.NotificationEntity
import com.homeflow.app.data.local.entity.PaymentEntity
import com.homeflow.app.data.local.entity.TaskEntity
import com.homeflow.app.data.local.entity.UserEntity

@Database(
    entities = [
        TaskEntity::class,
        UserEntity::class,
        HomeEntity::class,
        LabelEntity::class,
        ExpenseEntity::class,
        PaymentEntity::class,
        NotificationEntity::class,
        ActivityLogEntity::class
    ],
    version = 4,
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
}
