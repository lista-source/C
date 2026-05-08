package com.example.campusflow.data.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: Long = System.currentTimeMillis(),
    val authorId: String = "",
    val targetRole: UserRole? = null, // null for everyone
    val department: String? = null
)