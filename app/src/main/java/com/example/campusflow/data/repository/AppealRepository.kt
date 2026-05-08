package com.example.campusflow.data.repository

import com.example.campusflow.data.model.AppealStatus
import com.example.campusflow.data.model.GradeAppeal
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppealRepository @Inject constructor(
    private val database: FirebaseDatabase
) {

    private val appealsRef = database.getReference("appeals")

    // Student submits a new appeal
    suspend fun submitAppeal(appeal: GradeAppeal): Result<Unit> = runCatching {
        val id = appealsRef.push().key ?: throw Exception("Failed to generate appeal ID")
        appealsRef.child(id).setValue(appeal.copy(id = id)).await()
    }

    // Get all appeals for a specific student
    suspend fun getAppealsForStudent(studentId: String): List<GradeAppeal> {
        val snapshot = appealsRef.orderByChild("studentId").equalTo(studentId).get().await()
        return snapshot.children.mapNotNull { child ->
            runCatching { child.getValue(GradeAppeal::class.java) }.getOrNull()
        }.sortedByDescending { it.createdAt }
    }

    // Get all appeals for a specific course (lecturer view)
    suspend fun getAppealsForCourse(courseId: String): List<GradeAppeal> {
        val snapshot = appealsRef.orderByChild("courseId").equalTo(courseId).get().await()
        return snapshot.children.mapNotNull { child ->
            runCatching { child.getValue(GradeAppeal::class.java) }.getOrNull()
        }.sortedByDescending { it.createdAt }
    }

    // Get all pending appeals for a lecturer across all their courses
    suspend fun getPendingAppealsForLecturer(courseIds: List<String>): List<GradeAppeal> {
        val all = mutableListOf<GradeAppeal>()
        for (courseId in courseIds) {
            all += getAppealsForCourse(courseId)
        }
        return all.filter { it.status == AppealStatus.PENDING }
            .sortedByDescending { it.createdAt }
    }

    // Lecturer responds to an appeal
    suspend fun respondToAppeal(
        appealId: String,
        status: AppealStatus,
        comment: String
    ): Result<Unit> = runCatching {
        val updates = mapOf(
            "status" to status.name,
            "lecturerComment" to comment,
            "resolvedAt" to System.currentTimeMillis()
        )
        appealsRef.child(appealId).updateChildren(updates).await()
    }

    // Check if student already appealed a specific result
    suspend fun hasExistingAppeal(studentId: String, resultId: String): Boolean {
        val snapshot = appealsRef.orderByChild("studentId").equalTo(studentId).get().await()
        return snapshot.children.any { child ->
            child.child("resultId").getValue(String::class.java) == resultId
        }
    }
}