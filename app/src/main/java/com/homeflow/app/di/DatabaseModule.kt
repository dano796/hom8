package com.homeflow.app.di

import android.content.Context
import androidx.room.Room
import com.homeflow.app.data.local.dao.ActivityLogDao
import com.homeflow.app.data.local.dao.ExpenseDao
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.dao.LabelDao
import com.homeflow.app.data.local.dao.NotificationDao
import com.homeflow.app.data.local.dao.PaymentDao
import com.homeflow.app.data.local.dao.TaskDao
import com.homeflow.app.data.local.dao.UserDao
import com.homeflow.app.data.local.database.HomeFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HomeFlowDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HomeFlowDatabase::class.java,
            "homeflow_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTaskDao(database: HomeFlowDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideUserDao(database: HomeFlowDatabase): UserDao = database.userDao()

    @Provides
    fun provideHomeDao(database: HomeFlowDatabase): HomeDao = database.homeDao()

    @Provides
    fun provideLabelDao(database: HomeFlowDatabase): LabelDao = database.labelDao()

    @Provides
    fun provideExpenseDao(database: HomeFlowDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun providePaymentDao(database: HomeFlowDatabase): PaymentDao = database.paymentDao()

    @Provides
    fun provideNotificationDao(database: HomeFlowDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun provideActivityLogDao(database: HomeFlowDatabase): ActivityLogDao = database.activityLogDao()
}
