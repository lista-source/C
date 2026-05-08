package com.example.campusflow.ui.screens.lecturer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerDashboard(
    navController: NavController,
    authRepository: AuthRepository,
    viewModel: LecturerViewModel = hiltViewModel()
) {
    var user by remember { mutableStateOf<User?>(null) }
    val schedule by viewModel.schedule.collectAsState()
    val atRiskStudents by viewModel.atRiskStudents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        authRepository.currentUser?.uid?.let { uid ->
            try {
                val fetchedUser = authRepository.getUserData(uid)
                user = fetchedUser
                viewModel.loadSchedule(uid)
            } catch (e: Exception) { /* ignore */ }
        }
    }

    val todaySchedule = schedule.filter { entry ->
        val today = java.util.Calendar.getInstance()
            .getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.LONG, java.util.Locale.getDefault())
        entry.dayOfWeek.equals(today, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lecturer Dashboard") },
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
                        text = "Hello, ${user?.name ?: "Professor"}!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardCard(
                            title = "Classes Today",
                            value = "${todaySchedule.size}",
                            icon = Icons.Default.School,          // ✅ was Icons.Default.Class (reserved keyword)
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.LecturerSchedule.route) }
                        )
                        DashboardCard(
                            title = "At-Risk Students",
                            value = "${atRiskStudents.size}",
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.LecturerResults.route) }
                        )
                    }
                }

                item { Text("Management", style = MaterialTheme.typography.titleLarge) }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionItem(icon = Icons.Default.CalendarMonth, label = "My Teaching Schedule") {
                            navController.navigate(Screen.LecturerSchedule.route)
                        }
                        ActionItem(icon = Icons.Default.QrCodeScanner, label = "Mark Attendance (QR)") {
                            navController.navigate(Screen.LecturerAttendance.route)
                        }
                        ActionItem(icon = Icons.Default.UploadFile, label = "Upload Results") {
                            navController.navigate(Screen.LecturerResults.route)
                        }
                        ActionItem(icon = Icons.Default.Book, label = "Upload Course Materials") {
                            val courseId = user?.department ?: "general"
                            navController.navigate(Screen.LecturerMaterials.createRoute(courseId))
                        }
                        ActionItem(icon = Icons.Default.Message, label = "Chat with Students") {
                            navController.navigate(Screen.LecturerChat.createRoute("students", "Student Support"))
                        }
                        ActionItem(icon = Icons.Default.RateReview, label = "Review Grade Appeals") {
                            navController.navigate(Screen.LecturerAppeals.route)
                        }
                    }
                }
            }
        }
    }
}