package com.example.campusflow.data.model

data class TimetableEntry(
    val id: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val room: String = "",
    val lecturerId: String = "",
    val department: String = ""
)