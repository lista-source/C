package com.example.campusflow.di

import android.content.Context
import androidx.room.Room
import com.example.campusflow.data.local.AppDatabase
import com.example.campusflow.data.local.TimetableDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "campus_flow_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTimetableDao(database: AppDatabase): TimetableDao {
        return database.timetableDao()
    }
}