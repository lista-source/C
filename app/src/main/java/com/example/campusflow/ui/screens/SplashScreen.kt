package com.example.campusflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusflow.data.model.UserRole
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.ui.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    authRepository: AuthRepository
) {
    LaunchedEffect(Unit) {
        delay(1500)
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            onNavigate(Screen.Login.route)
        } else {
            try {
                val user = authRepository.getUserData(currentUser.uid)
                val destination = when (user.role) {
                    UserRole.STUDENT -> Screen.StudentDashboard.route
                    UserRole.LECTURER -> Screen.LecturerDashboard.route
                    UserRole.ADMIN -> Screen.AdminDashboard.route
                }
                onNavigate(destination)
            } catch (e: Exception) {
                onNavigate(Screen.Login.route)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Campus Flow",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
