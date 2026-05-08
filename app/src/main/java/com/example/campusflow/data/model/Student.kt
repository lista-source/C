package com.example.campusflow.data.model

data class Student(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val registrationNumber: String = "",
    val department: String = "",
    val course: String = "",
    val semester: Int = 1,
    val gpa: Double = 0.0,
    val attendancePercentage: Double = 0.0
)