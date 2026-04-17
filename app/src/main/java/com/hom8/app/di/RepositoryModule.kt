package com.hom8.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Repository bindings will be added here as implementations are created in later phases.
// Example:
// @Binds @Singleton abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule
