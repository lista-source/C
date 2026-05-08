package com.example.campusflow.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.STUDENT,
    val profileImageUrl: String = "",
    val department: String = ""
)