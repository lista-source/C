package com.example.campusflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campusflow.data.model.User
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.auth.AuthState
import com.example.campusflow.ui.auth.LoginScreen
import com.example.campusflow.ui.auth.RegisterScreen
import com.example.campusflow.ui.navigation.Screen
import com.example.campusflow.ui.screens.ChatScreen
import com.example.campusflow.ui.screens.ProfileScreen
import com.example.campusflow.ui.screens.SplashScreen
import com.example.campusflow.ui.screens.admin.AdminAnnouncementsScreen
import com.example.campusflow.ui.screens.admin.AdminDashboard
import com.example.campusflow.ui.screens.admin.AdminManagementScreen
import com.example.campusflow.ui.screens.appeal.AppealViewModel
import com.example.campusflow.ui.screens.appeal.LecturerAppealScreen
import com.example.campusflow.ui.screens.lecturer.LecturerAttendanceScreen
import com.example.campusflow.ui.screens.lecturer.LecturerDashboard
import com.example.campusflow.ui.screens.lecturer.LecturerResultsScreen
import com.example.campusflow.ui.screens.lecturer.LecturerScheduleScreen
import com.example.campusflow.ui.screens.lecturer.LecturerViewModel
import com.example.campusflow.ui.screens.student.AnnouncementScreen
import com.example.campusflow.ui.screens.student.MaterialsScreen
import com.example.campusflow.ui.screens.student.StudentAnalyticsScreen
import com.example.campusflow.ui.screens.student.StudentAttendanceScreen
import com.example.campusflow.ui.screens.student.StudentDashboard
import com.example.campusflow.ui.screens.student.StudentResultsScreen
import com.example.campusflow.ui.screens.student.StudentTimetableScreen
import com.example.campusflow.ui.screens.student.StudentViewModel
import com.example.campusflow.ui.theme.CampusFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.campusflow.ui.screens.appeal.StudentAppealScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var repository: FirebaseRepository
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusFlowTheme {
                AppNavigation(repository, authRepository)
            }
        }
    }
}

@Composable
fun AppNavigation(repository: FirebaseRepository, authRepository: AuthRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // ── Splash ────────────────────────────────────────────────────────

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                authRepository = authRepository
            )
        }

        // ── Auth ──────────────────────────────────────────────────────────

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        "STUDENT" -> Screen.StudentDashboard.route
                        "LECTURER" -> Screen.LecturerDashboard.route
                        "ADMIN" -> Screen.AdminDashboard.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = { role ->
                    val destination = when (role) {
                        "STUDENT" -> Screen.StudentDashboard.route
                        "LECTURER" -> Screen.LecturerDashboard.route
                        "ADMIN" -> Screen.AdminDashboard.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                authRepository = authRepository
            )
        }

        // ── Student ───────────────────────────────────────────────────────

        composable(Screen.StudentDashboard.route) {
            StudentDashboard(
                navController = navController,
                authRepository = authRepository
            )
        }

        composable(Screen.StudentAttendance.route) {
            StudentAttendanceScreen(
                navController = navController
            )
        }

        composable(Screen.StudentTimetable.route) {
            StudentTimetableScreen(
                navController = navController
            )
        }

        composable(Screen.StudentResults.route) {
            StudentResultsScreen(
                navController = navController
            )
        }

        composable(Screen.StudentAnalytics.route) {
            StudentAnalyticsScreen(
                navController = navController
            )
        }

        composable(Screen.StudentAnnouncements.route) {
            AnnouncementScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.StudentMaterials.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStack ->
            val courseId = backStack.arguments?.getString("courseId") ?: ""
            MaterialsScreen(
                navController = navController,
                repository = repository,
                courseId = courseId
            )
        }

        composable(
            route = Screen.StudentChat.route,
            arguments = listOf(
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("receiverName") { type = NavType.StringType }
            )
        ) { backStack ->
            val receiverId = backStack.arguments?.getString("receiverId") ?: ""
            val receiverName = backStack.arguments?.getString("receiverName") ?: ""
            ChatScreen(
                navController = navController,
                authRepository = authRepository,
                receiverId = receiverId,
                receiverName = receiverName
            )
        }

        // ── Lecturer ──────────────────────────────────────────────────────

        composable(Screen.LecturerDashboard.route) {
            LecturerDashboard(
                navController = navController,
                authRepository = authRepository
            )
        }

        composable(Screen.LecturerSchedule.route) {
            LecturerScheduleScreen(
                navController = navController,
                authRepository = authRepository
            )
        }

        composable(Screen.LecturerAttendance.route) {
            LecturerAttendanceScreen(
                navController = navController,
                repository = repository
            )
        }

        composable(Screen.LecturerResults.route) {
            LecturerResultsScreen(
                navController = navController,
                repository = repository
            )
        }

        composable(
            route = Screen.LecturerChat.route,
            arguments = listOf(
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("receiverName") { type = NavType.StringType }
            )
        ) { backStack ->
            val receiverId = backStack.arguments?.getString("receiverId") ?: ""
            val receiverName = backStack.arguments?.getString("receiverName") ?: ""
            ChatScreen(
                navController = navController,
                authRepository = authRepository,
                receiverId = receiverId,
                receiverName = receiverName
            )
        }

        composable(
            route = Screen.LecturerMaterials.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStack ->
            val courseId = backStack.arguments?.getString("courseId") ?: ""
            MaterialsScreen(
                navController = navController,
                repository = repository,
                courseId = courseId
            )
        }

        // ── Admin ─────────────────────────────────────────────────────────

        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                navController = navController,
                authRepository = authRepository,
                repository = repository
            )
        }

        composable(Screen.AdminManagement.route) {
            AdminManagementScreen(
                navController = navController,
                repository = repository
            )
        }

        composable(Screen.AdminAnnouncements.route) {
            AdminAnnouncementsScreen(
                navController = navController,
                repository = repository
            )
        }
        // ADD these routes inside your NavHost in MainActivity.kt
// alongside the existing student_results and lecturer_results routes

// ── Student Appeals ──────────────────────────────────────────────────────────
        // ── Student Appeals ──────────────────────────────────────────────────────────
        composable(Screen.StudentAppeals.route) {
            val studentVm: StudentViewModel = hiltViewModel()
            val appealVm: AppealViewModel = hiltViewModel()
            val results by studentVm.results.collectAsState()

            // Get user directly from authRepository instead of authViewModel
            var user by remember { mutableStateOf<User?>(null) }
            LaunchedEffect(Unit) {
                authRepository.currentUser?.uid?.let { uid ->
                    runCatching {
                        user = authRepository.getUserData(uid)
                        studentVm.initialize(uid, user!!.department)
                    }
                }
            }

            user?.let {
                StudentAppealScreen(
                    navController = navController,
                    user = it,
                    results = results,
                    viewModel = appealVm
                )
            }
        }
// ── Lecturer Appeals ─────────────────────────────────────────────────────────
        composable(Screen.LecturerAppeals.route) {
            val lecturerVm: LecturerViewModel = hiltViewModel()
            val schedule by lecturerVm.schedule.collectAsState()

            LecturerAppealScreen(
                navController = navController,
                schedule = schedule
            )
        }
    }
}