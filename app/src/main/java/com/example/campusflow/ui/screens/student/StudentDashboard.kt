package com.example.campusflow.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.User
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.ui.components.ActionItem
import com.example.campusflow.ui.components.DashboardCard
import com.example.campusflow.ui.navigation.Screen
import kotlin.collections.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    navController: NavController,
    authRepository: AuthRepository,
    viewModel: StudentViewModel = hiltViewModel()
) {
    var user by remember { mutableStateOf<User?>(null) }
    val attendance by viewModel.attendance.collectAsState()
    val results by viewModel.results.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        authRepository.currentUser?.uid?.let { uid ->
            try {
                val fetchedUser = authRepository.getUserData(uid)
                user = fetchedUser
                viewModel.initialize(uid, fetchedUser.department)
            } catch (e: Exception) { /* ignore */ }
        }
    }

    // Compute stats
    val attendanceRate = if (attendance.isEmpty()) 0
    else (attendance.count { it.isPresent } * 100 / attendance.size)

    val gpa = if (results.isEmpty()) 0.0
    else results.map { gpaFromScore(it.totalScore) }.average()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Dashboard") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = {
                        authRepository.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Hello, ${user?.name ?: "Student"}!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                // Stats cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardCard(
                            title = "Attendance",
                            value = "$attendanceRate%",
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.StudentAttendance.route) }
                        )
                        DashboardCard(
                            title = "GPA",
                            value = String.format("%.2f", gpa),
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.StudentResults.route) }
                        )
                    }
                }

                item { Text("Quick Access", style = MaterialTheme.typography.titleLarge) }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionItem(icon = Icons.Default.CalendarMonth, label = "My Timetable") {
                            navController.navigate(Screen.StudentTimetable.route)
                        }
                        ActionItem(icon = Icons.Default.CheckCircle, label = "Mark Attendance") {
                            navController.navigate(Screen.StudentAttendance.route)
                        }
                        ActionItem(icon = Icons.Default.TrendingUp, label = "My Results") {
                            navController.navigate(Screen.StudentResults.route)
                        }
                        ActionItem(icon = Icons.Default.Analytics, label = "Analytics") {
                            navController.navigate(Screen.StudentAnalytics.route)
                        }
                        ActionItem(icon = Icons.Default.Announcement, label = "Announcements") {
                            navController.navigate(Screen.StudentAnnouncements.route)
                        }
                        ActionItem(icon = Icons.Default.Message, label = "Chat") {
                            navController.navigate(
                                Screen.StudentChat.createRoute("lecturers", "Support")
                            )
                        }
                        ActionItem(icon = Icons.Default.RateReview, label = "Grade Appeals") {
                            navController.navigate(Screen.StudentAppeals.route)
                        }
                    }
                }
            }
        }
    }
}

// GPA helper — single source of truth
fun gpaFromScore(score: Double): Double = when {
    score >= 70 -> 4.0
    score >= 60 -> 3.7
    score >= 50 -> 3.0
    score >= 40 -> 2.0
    else        -> 0.0
}