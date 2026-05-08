package com.example.campusflow.data.model

data class Course(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val department: String = "",
    val lecturerId: String = "",
    val credits: Int = 0
)