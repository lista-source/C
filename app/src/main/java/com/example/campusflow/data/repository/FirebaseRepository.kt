package com.example.campusflow.data.repository

import com.example.campusflow.data.local.AppDatabase
import com.example.campusflow.data.local.TimetableEntryEntity
import com.example.campusflow.data.model.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class FirebaseRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val localDb: AppDatabase
) {
    private val CAMPUS_LAT        = -1.2921
    private val CAMPUS_LON        = 36.8219
    private val MAX_DISTANCE_METERS = 100.0

    // ── Attendance ────────────────────────────────────────────────────────

    suspend fun verifyAndMarkAttendance(
        studentId: String,
        courseId: String,
        studentLat: Double,
        studentLon: Double
    ): Result<Unit> {
        val distance = haversineDistance(studentLat, studentLon, CAMPUS_LAT, CAMPUS_LON)
        if (distance > MAX_DISTANCE_METERS) {
            return Result.failure(
                Exception("You are ${distance.toInt()}m from campus. Must be within ${MAX_DISTANCE_METERS.toInt()}m.")
            )
        }
        val attendance = Attendance(
            studentId          = studentId,
            courseId           = courseId,
            date               = System.currentTimeMillis(),
            isPresent          = true,
            location           = "$studentLat,$studentLon",
            verified           = true,
            verificationMethod = VerificationMethod.GPS
        )
        return try {
            val key = database.getReference("attendance").push().key
                ?: throw Exception("Database key generation failed")
            database.getReference("attendance").child(key)
                .setValue(attendance.copy(id = key)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentAttendance(studentId: String): List<Attendance> {
        return try {
            val snapshot = database.getReference("attendance")
                .orderByChild("studentId").equalTo(studentId).get().await()
            snapshot.children
                .mapNotNull { it.getValue(Attendance::class.java) }
                .sortedByDescending { it.date }
        } catch (e: Exception) { emptyList() }
    }

    // ── Timetable ─────────────────────────────────────────────────────────

    suspend fun getTimetableForStudent(department: String): List<TimetableEntry> {
        return try {
            val snapshot = database.getReference("timetable")
                .orderByChild("department").equalTo(department).get().await()
            val remote = snapshot.children.mapNotNull { it.getValue(TimetableEntry::class.java) }
            localDb.timetableDao().insertAll(remote.map { it.toEntity() })
            remote
        } catch (e: Exception) {
            localDb.timetableDao().getAll().map { it.toDomain() }
        }
    }

    suspend fun getTimetableForLecturer(lecturerId: String): List<TimetableEntry> {
        return try {
            val snapshot = database.getReference("timetable")
                .orderByChild("lecturerId").equalTo(lecturerId).get().await()
            snapshot.children.mapNotNull { it.getValue(TimetableEntry::class.java) }
        } catch (e: Exception) { emptyList() }
    }

    // ── Results ───────────────────────────────────────────────────────────

    suspend fun getResultsForStudent(studentId: String): List<AcademicResult> {
        return try {
            val snapshot = database.getReference("results")
                .orderByChild("studentId").equalTo(studentId).get().await()
            snapshot.children.mapNotNull { it.getValue(AcademicResult::class.java) }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getResultsByCourse(courseId: String): List<AcademicResult> {
        return try {
            val snapshot = database.getReference("results")
                .orderByChild("courseId").equalTo(courseId).get().await()
            snapshot.children.mapNotNull { it.getValue(AcademicResult::class.java) }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun uploadResult(result: AcademicResult): Result<Unit> {
        return try {
            val key = database.getReference("results").push().key
                ?: throw Exception("Database key generation failed")
            database.getReference("results").child(key)
                .setValue(result.copy(id = key)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Announcements ─────────────────────────────────────────────────────

    suspend fun postAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            val key = database.getReference("announcements").push().key
                ?: throw Exception("Database key generation failed")
            database.getReference("announcements").child(key)
                .setValue(announcement.copy(id = key)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getAnnouncements(): List<Announcement> {
        return try {
            val snapshot = database.getReference("announcements").get().await()
            snapshot.children
                .mapNotNull { it.getValue(Announcement::class.java) }
                .sortedByDescending { it.date }
        } catch (e: Exception) { emptyList() }
    }

    // ── Materials ─────────────────────────────────────────────────────────

    suspend fun getMaterials(courseId: String): List<String> {
        return try {
            val listResult = storage.getReference("materials/$courseId").listAll().await()
            listResult.items.map { it.downloadUrl.await().toString() }
        } catch (e: Exception) { emptyList() }
    }

    // ── Users ─────────────────────────────────────────────────────────────

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = database.getReference("users").get().await()
            snapshot.children.mapNotNull { it.getValue(User::class.java) }
        } catch (e: Exception) { emptyList() }
    }

    // ── Chat ──────────────────────────────────────────────────────────────

    suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            val key = database.getReference("chats").push().key
                ?: throw Exception("Database key generation failed")
            database.getReference("chats").child(key)
                .setValue(message.copy(id = key)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Distance Helper ───────────────────────────────────────────────────

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r      = 6371e3
        val phi1   = lat1 * PI / 180
        val phi2   = lat2 * PI / 180
        val dPhi   = (lat2 - lat1) * PI / 180
        val dLambda = (lon2 - lon1) * PI / 180
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // ── Room Mappers ──────────────────────────────────────────────────────

    private fun TimetableEntry.toEntity() =
        TimetableEntryEntity(id, courseId, courseName, dayOfWeek, startTime, endTime, room, lecturerId, department)

    private fun TimetableEntryEntity.toDomain() =
        TimetableEntry(id, courseId, courseName, dayOfWeek, startTime, endTime, room, lecturerId, department)
}
