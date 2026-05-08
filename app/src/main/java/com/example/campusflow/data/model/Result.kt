package com.example.campusflow.data.model

data class AcademicResult(
    val id: String = "",
    val studentId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val catScore: Double = 0.0,
    val examScore: Double = 0.0,
    val totalScore: Double = 0.0,
    val grade: String = "",
    val semester: Int = 1,
    val academicYear: String = ""
)