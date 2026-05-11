package com.example.campusflow.ui.screens.student

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.User
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.ui.components.ActionItem
import com.example.campusflow.ui.components.DashboardCard
import com.example.campusflow.ui.components.SectionHeader
import com.example.campusflow.ui.navigation.Screen
import com.example.campusflow.ui.theme.*
import java.util.Calendar

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
                viewModel.initialize(uid, fetchedUser.department, fetchedUser.studentId)
            } catch (e: Exception) { /* ignore */ }
        }
    }

    val attendanceRate = if (attendance.isEmpty()) 85
    else (attendance.count { it.isPresent } * 100 / attendance.size)

    val gpa = if (results.isEmpty()) 3.7
    else results.map { gpaFromScore(it.totalScore) }.average()

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user?.name?.take(1) ?: "S", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("CampusFlow", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                            Text("Student Portal", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.StudentAnnouncements.route) }) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = NavyPrimary)
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGrey
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Greeting hero banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(NavyPrimary, CornflowerBlue)))
                        .padding(24.dp)
                ) {
                    Column {
                        Text("$greeting,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(user?.name ?: "Student", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Semester 1 · ${user?.department ?: "Computer Science"}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))
                        Row {
                            QuickStat("Attendance", "$attendanceRate%", SuccessGreen)
                            Spacer(Modifier.width(24.dp))
                            QuickStat("GPA", String.format("%.1f", gpa), TealAccent)
                            Spacer(Modifier.width(24.dp))
                            QuickStat("Courses", "${results.size.coerceAtLeast(4)}", WarningAmber)
                        }
                    }
                    // Decorative circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                    )
                }

                // Attendance alert if low
                if (attendanceRate < 75) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Low Attendance Warning", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 13.sp)
                                Text("Your attendance is below 75%. Please attend classes.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Stat cards row
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardCard(
                        title = "Attendance",
                        value = "$attendanceRate%",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f),
                        containerColor = if (attendanceRate >= 75) NavyPrimary else ErrorRed,
                        onClick = { navController.navigate(Screen.StudentAttendance.route) }
                    )
                    DashboardCard(
                        title = "GPA",
                        value = String.format("%.2f", gpa),
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f),
                        containerColor = SkyBlue,
                        onClick = { navController.navigate(Screen.StudentResults.route) }
                    )
                    DashboardCard(
                        title = "Courses",
                        value = "${results.size.coerceAtLeast(4)}",
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF00897B),
                        onClick = { navController.navigate(Screen.StudentResults.route) }
                    )
                }

                Spacer(Modifier.height(20.dp))
                SectionHeader("Quick Access", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionItem(icon = Icons.Default.CalendarMonth, label = "My Timetable", subtitle = "View class schedule") {
                        navController.navigate(Screen.StudentTimetable.route)
                    }
                    ActionItem(icon = Icons.Default.QrCodeScanner, label = "Mark Attendance", subtitle = "Scan QR or use GPS") {
                        navController.navigate(Screen.StudentAttendance.route)
                    }
                    ActionItem(icon = Icons.Default.TrendingUp, label = "My Results", subtitle = "View grades & transcripts") {
                        navController.navigate(Screen.StudentResults.route)
                    }
                    ActionItem(icon = Icons.Default.Analytics, label = "Performance Analytics", subtitle = "Trends & insights") {
                        navController.navigate(Screen.StudentAnalytics.route)
                    }
                    ActionItem(icon = Icons.Default.Campaign, label = "Announcements", subtitle = "Latest campus news", badge = "3") {
                        navController.navigate(Screen.StudentAnnouncements.route)
                    }
                    ActionItem(icon = Icons.Default.Forum, label = "Chat with Lecturers", subtitle = "Send messages") {
                        navController.navigate(Screen.StudentChat.createRoute("lecturers", "Support"))
                    }
                    ActionItem(icon = Icons.Default.Gavel, label = "Grade Appeals", subtitle = "Dispute a result") {
                        navController.navigate(Screen.StudentAppeals.route)
                    }
                    ActionItem(icon = Icons.Default.LibraryBooks, label = "Course Materials", subtitle = "Slides, notes & resources") {
                        navController.navigate(Screen.StudentMaterials.createRoute(user?.department ?: "general"))
                    }
                }

                Spacer(Modifier.height(16.dp))
                // Logout
                TextButton(
                    onClick = {
                        authRepository.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sign Out", color = ErrorRed)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun QuickStat(label: String, value: String, color: Color) {
    Column {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

fun gpaFromScore(score: Double): Double = when {
    score >= 70 -> 4.0
    score >= 60 -> 3.7
    score >= 50 -> 3.0
    score >= 40 -> 2.0
    else -> 0.0
}