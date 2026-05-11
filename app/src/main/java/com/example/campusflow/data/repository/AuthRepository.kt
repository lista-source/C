package com.example.campusflow.data.repository

import com.example.campusflow.data.model.User
import com.example.campusflow.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    val currentUser get() = auth.currentUser

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("Login failed")
            val user = getUserData(uid)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        department: String = ""
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("Registration failed")

            // Auto-generate a unique student/staff ID
            val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val prefix = when (role) {
                UserRole.STUDENT  -> "STU"
                UserRole.LECTURER -> "LEC"
                UserRole.ADMIN    -> "ADM"
            }
            // Use last 5 digits of uid hash for uniqueness
            val suffix = uid.hashCode().let { Math.abs(it) % 100000 }.toString().padStart(5, '0')
            val generatedId = "$prefix-$year-$suffix"

            val user = User(
                uid        = uid,
                name       = name,
                email      = email,
                role       = role,
                department = department,
                studentId  = generatedId
            )
            database.getReference("users").child(uid).setValue(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(uid: String): User {
        val snapshot = database.getReference("users").child(uid).get().await()
        return snapshot.getValue(User::class.java) ?: throw Exception("User not found")
    }

    suspend fun saveFcmToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            database.getReference("users").child(uid).child("fcmToken").setValue(token).await()
        } catch (e: Exception) { /* Non-critical */ }
    }

    fun logout() { auth.signOut() }
}
