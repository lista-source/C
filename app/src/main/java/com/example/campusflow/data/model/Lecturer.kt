package com.example.campusflow.data.model

data class Lecturer(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val employeeId: String = "",
    val department: String = "",
    val courses: List<String> = emptyList()
)