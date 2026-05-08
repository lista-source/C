package com.example.campusflow.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.UserRole
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.components.ActionItem
import com.example.campusflow.ui.components.DashboardCard
import com.example.campusflow.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    navController: NavController,
    authRepository: AuthRepository,
    repository: FirebaseRepository,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val users     by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadUsers() }

    val totalStudents  = users.count { it.role == UserRole.STUDENT }
    val totalLecturers = users.count { it.role == UserRole.LECTURER }
    val totalUsers     = users.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    Text("System Administration", style = MaterialTheme.typography.headlineMedium)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardCard(
                            title    = "Students",
                            value    = "$totalStudents",
                            icon     = Icons.Default.School,
                            modifier = Modifier.weight(1f),
                            onClick  = { navController.navigate(Screen.AdminManagement.route) }
                        )
                        DashboardCard(
                            title    = "Lecturers",
                            value    = "$totalLecturers",
                            icon     = Icons.Default.Person,
                            modifier = Modifier.weight(1f),
                            onClick  = { navController.navigate(Screen.AdminManagement.route) }
                        )
                        DashboardCard(
                            title    = "Total",
                            value    = "$totalUsers",
                            icon     = Icons.Default.Group,
                            modifier = Modifier.weight(1f),
                            onClick  = { navController.navigate(Screen.AdminManagement.route) }
                        )
                    }
                }

                item { Text("Management Tasks", style = MaterialTheme.typography.titleLarge) }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionItem(icon = Icons.Default.ManageAccounts, label = "Manage Users") {
                            navController.navigate(Screen.AdminManagement.route)
                        }
                        ActionItem(icon = Icons.Default.Campaign, label = "Broadcast Announcement") {
                            navController.navigate(Screen.AdminAnnouncements.route)
                        }
                    }
                }
            }
        }
    }
}
