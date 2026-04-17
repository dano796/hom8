package com.homeflow.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Migrar estados de tareas de inglés a español
            database.execSQL("UPDATE tasks SET estado = 'PENDIENTE' WHERE estado = 'PENDING'")
            database.execSQL("UPDATE tasks SET estado = 'TERMINADO' WHERE estado = 'DONE' OR estado = 'COMPLETED'")
            database.execSQL("UPDATE tasks SET estado = 'EN_CURSO' WHERE estado = 'IN_PROGRESS'")
            database.execSQL("UPDATE tasks SET estado = 'ATRASADO' WHERE estado = 'OVERDUE'")
            
            // Migrar prioridades de inglés a español
            database.execSQL("UPDATE tasks SET prioridad = 'ALTA' WHERE prioridad = 'HIGH'")
            database.execSQL("UPDATE tasks SET prioridad = 'MEDIA' WHERE prioridad = 'MEDIUM'")
            database.execSQL("UPDATE tasks SET prioridad = 'BAJA' WHERE prioridad = 'LOW'")
            
            // Migrar categorías de gastos de inglés a español
            database.execSQL("UPDATE expenses SET categoria = 'COMIDA' WHERE categoria = 'FOOD'")
            database.execSQL("UPDATE expenses SET categoria = 'SUPERMERCADO' WHERE categoria = 'SUPERMARKET'")
            database.execSQL("UPDATE expenses SET categoria = 'SERVICIOS' WHERE categoria = 'SERVICES'")
            database.execSQL("UPDATE expenses SET categoria = 'TRANSPORTE' WHERE categoria = 'TRANSPORT'")
            database.execSQL("UPDATE expenses SET categoria = 'OCIO' WHERE categoria = 'ENTERTAINMENT'")
            database.execSQL("UPDATE expenses SET categoria = 'LIMPIEZA' WHERE categoria = 'CLEANING'")
            database.execSQL("UPDATE expenses SET categoria = 'SALUD' WHERE categoria = 'HEALTH'")
            database.execSQL("UPDATE expenses SET categoria = 'OTROS' WHERE categoria = 'OTHER'")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HomeFlowDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HomeFlowDatabase::class.java,
            "homeflow_database"
        )
            .addMigrations(MIGRATION_4_5)
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
