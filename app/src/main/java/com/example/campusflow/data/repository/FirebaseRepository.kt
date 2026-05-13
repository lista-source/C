package com.example.campusflow.data.repository

import android.content.Context
import android.net.Uri
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
    private val CAMPUS_LAT = -1.2921
    private val CAMPUS_LON = 36.8219
    private val MAX_DISTANCE_METERS = 500.0

    // ── Attendance ────────────────────────────────────────────────────────

    suspend fun verifyAndMarkAttendance(studentId: String, courseId: String, studentLat: Double, studentLon: Double): Result<Unit> {
        val dist = haversineDistance(studentLat, studentLon, CAMPUS_LAT, CAMPUS_LON)
        if (dist > MAX_DISTANCE_METERS) return Result.failure(Exception("You are ${dist.toInt()}m from campus. Must be within ${MAX_DISTANCE_METERS.toInt()}m."))
        return markAttendanceRecord(studentId, courseId, VerificationMethod.GPS, "$studentLat,$studentLon")
    }

    suspend fun markAttendanceManual(studentId: String, courseId: String, lecturerId: String): Result<Unit> =
        markAttendanceRecord(studentId, courseId, VerificationMethod.MANUAL, "", lecturerId)

    private suspend fun markAttendanceRecord(studentId: String, courseId: String, method: VerificationMethod, location: String, lectureId: String = ""): Result<Unit> {
        val rec = Attendance(studentId = studentId, courseId = courseId, date = System.currentTimeMillis(), isPresent = true, location = location, verified = true, verificationMethod = method, lectureId = lectureId)
        return try {
            val key = database.getReference("attendance").push().key ?: throw Exception("Key generation failed")
            database.getReference("attendance/$key").setValue(rec.copy(id = key)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getStudentAttendance(studentId: String): List<Attendance> = try {
        database.getReference("attendance").orderByChild("studentId").equalTo(studentId).get().await()
            .children.mapNotNull { it.getValue(Attendance::class.java) }.sortedByDescending { it.date }
    } catch (e: Exception) { emptyList() }

    suspend fun getAttendanceByCourse(courseId: String): List<Attendance> = try {
        database.getReference("attendance").orderByChild("courseId").equalTo(courseId).get().await()
            .children.mapNotNull { it.getValue(Attendance::class.java) }.sortedByDescending { it.date }
    } catch (e: Exception) { emptyList() }

    // ── Timetable Read ────────────────────────────────────────────────────

    suspend fun getTimetableForStudent(department: String): List<TimetableEntry> = try {
        val remote = database.getReference("timetable").orderByChild("department").equalTo(department).get().await()
            .children.mapNotNull { it.getValue(TimetableEntry::class.java) }
        if (remote.isNotEmpty()) localDb.timetableDao().insertAll(remote.map { it.toEntity() })
        remote
    } catch (e: Exception) { localDb.timetableDao().getAll().map { it.toDomain() } }

    suspend fun getTimetableForLecturer(lecturerId: String): List<TimetableEntry> = try {
        database.getReference("timetable").orderByChild("lecturerId").equalTo(lecturerId).get().await()
            .children.mapNotNull { it.getValue(TimetableEntry::class.java) }
    } catch (e: Exception) { emptyList() }

    // ── Timetable Write ───────────────────────────────────────────────────

    suspend fun addTimetableEntry(entry: TimetableEntry): Result<Unit> = try {
        val key = database.getReference("timetable").push().key ?: throw Exception("Key generation failed")
        val saved = entry.copy(id = key)
        database.getReference("timetable/$key").setValue(saved).await()
        localDb.timetableDao().insertAll(listOf(saved.toEntity()))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteTimetableEntry(entryId: String): Result<Unit> = try {
        database.getReference("timetable/$entryId").removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ── Personal Schedule (Student's own study slots) ─────────────────────

    suspend fun getPersonalSchedule(studentId: String): List<TimetableEntry> = try {
        database.getReference("personalSchedule").orderByChild("lecturerId").equalTo(studentId).get().await()
            .children.mapNotNull { it.getValue(TimetableEntry::class.java) }
    } catch (e: Exception) { emptyList() }

    suspend fun addPersonalScheduleEntry(studentId: String, title: String, type: String, day: String, start: String, end: String): Result<TimetableEntry> = try {
        val key   = database.getReference("personalSchedule").push().key ?: throw Exception("Key generation failed")
        val entry = TimetableEntry(id = key, courseId = type, courseName = title, dayOfWeek = day, startTime = start, endTime = end, room = "", lecturerId = studentId, department = "personal")
        database.getReference("personalSchedule/$key").setValue(entry).await()
        Result.success(entry)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deletePersonalScheduleEntry(entryId: String) = try {
        database.getReference("personalSchedule/$entryId").removeValue().await()
    } catch (e: Exception) {}

    // ── Results ───────────────────────────────────────────────────────────

    suspend fun getResultsForStudent(studentId: String): List<AcademicResult> = try {
        database.getReference("results").orderByChild("studentId").equalTo(studentId).get().await()
            .children.mapNotNull { it.getValue(AcademicResult::class.java) }
    } catch (e: Exception) { emptyList() }

    suspend fun getResultsByCourse(courseId: String): List<AcademicResult> = try {
        database.getReference("results").orderByChild("courseId").equalTo(courseId).get().await()
            .children.mapNotNull { it.getValue(AcademicResult::class.java) }
    } catch (e: Exception) { emptyList() }

    suspend fun uploadResult(result: AcademicResult): Result<Unit> = try {
        val key = database.getReference("results").push().key ?: throw Exception("Key generation failed")
        database.getReference("results/$key").setValue(result.copy(id = key)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ── Courses ───────────────────────────────────────────────────────────

    suspend fun getAllCourses(): List<Course> = try {
        database.getReference("courses").get().await()
            .children.mapNotNull { it.getValue(Course::class.java) }
    } catch (e: Exception) { emptyList() }

    suspend fun getCoursesByDepartment(department: String): List<Course> = try {
        database.getReference("courses").orderByChild("department").equalTo(department).get().await()
            .children.mapNotNull { it.getValue(Course::class.java) }
    } catch (e: Exception) { emptyList() }

    suspend fun addCourse(course: Course): Result<Unit> = try {
        val key = database.getReference("courses").push().key ?: throw Exception("Key generation failed")
        database.getReference("courses/$key").setValue(course.copy(id = key)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteCourse(courseId: String): Result<Unit> = try {
        database.getReference("courses/$courseId").removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ── Materials ─────────────────────────────────────────────────────────

    suspend fun getMaterials(courseId: String): List<String> = try {
        val stored = try {
            storage.getReference("materials/$courseId").listAll().await().items.map { it.downloadUrl.await().toString() }
        } catch (e: Exception) { emptyList() }
        val links = try {
            database.getReference("materialLinks/$courseId").get().await()
                .children.mapNotNull { it.child("url").getValue(String::class.java) }
        } catch (e: Exception) { emptyList() }
        (stored + links).distinct()
    } catch (e: Exception) { emptyList() }

    suspend fun addMaterialLink(courseId: String, url: String, title: String): Result<Unit> = try {
        val key = database.getReference("materialLinks/$courseId").push().key ?: throw Exception("Key generation failed")
        database.getReference("materialLinks/$courseId/$key").setValue(mapOf("url" to url, "title" to title, "addedAt" to System.currentTimeMillis())).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun uploadMaterialFile(courseId: String, fileUri: Uri, context: Context): Result<String> = try {
        val fileName = "${System.currentTimeMillis()}_${fileUri.lastPathSegment ?: "file"}"
        val ref = storage.getReference("materials/$courseId/$fileName")
        ref.putFile(fileUri).await()
        Result.success(ref.downloadUrl.await().toString())
    } catch (e: Exception) { Result.failure(e) }

    // ── Announcements ─────────────────────────────────────────────────────

    suspend fun postAnnouncement(announcement: Announcement): Result<Unit> = try {
        val key = database.getReference("announcements").push().key ?: throw Exception("Key generation failed")
        database.getReference("announcements/$key").setValue(announcement.copy(id = key)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getAnnouncements(): List<Announcement> = try {
        database.getReference("announcements").get().await()
            .children.mapNotNull { it.getValue(Announcement::class.java) }.sortedByDescending { it.date }
    } catch (e: Exception) { emptyList() }

    // ── Users ─────────────────────────────────────────────────────────────

    suspend fun getAllUsers(): List<User> = try {
        database.getReference("users").get().await().children.mapNotNull { it.getValue(User::class.java) }
    } catch (e: Exception) { emptyList() }

    suspend fun getUsersByRole(role: UserRole): List<User> = try {
        database.getReference("users").orderByChild("role").equalTo(role.name).get().await()
            .children.mapNotNull { it.getValue(User::class.java) }
    } catch (e: Exception) { emptyList() }

    // ── Chat — Private conversations ──────────────────────────────────────

    private fun conversationId(uid1: String, uid2: String): String = listOf(uid1, uid2).sorted().joinToString("_")

    suspend fun sendMessage(message: ChatMessage): Result<Unit> = try {
        val convId = conversationId(message.senderId, message.receiverId)
        val key    = database.getReference("chats/$convId").push().key ?: throw Exception("Key generation failed")
        database.getReference("chats/$convId/$key").setValue(message.copy(id = key)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    fun getConversationPath(uid1: String, uid2: String): String = "chats/${conversationId(uid1, uid2)}"

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3
        val phi1 = lat1 * PI / 180; val phi2 = lat2 * PI / 180
        val dPhi = (lat2 - lat1) * PI / 180; val dLambda = (lon2 - lon1) * PI / 180
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun TimetableEntry.toEntity() = TimetableEntryEntity(id, courseId, courseName, dayOfWeek, startTime, endTime, room, lecturerId, department)
    private fun TimetableEntryEntity.toDomain() = TimetableEntry(id, courseId, courseName, dayOfWeek, startTime, endTime, room, lecturerId, department)
}
