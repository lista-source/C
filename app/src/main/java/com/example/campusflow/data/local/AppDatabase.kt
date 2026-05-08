package com.example.campusflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Database(entities = [TimetableEntryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
}

@Entity(tableName = "timetable")
data class TimetableEntryEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val courseName: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val lecturerId: String,
    val department: String
)

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable")
    suspend fun getAll(): List<TimetableEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TimetableEntryEntity>)
}