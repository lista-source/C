package com.example.campusflow.data.model

enum class AppealStatus { PENDING, REVIEWED, RESOLVED, REJECTED }

data class GradeAppeal(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val resultId: String = "",
    val reason: String = "",
    val status: AppealStatus = AppealStatus.PENDING,
    val lecturerComment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long = 0L
)