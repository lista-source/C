package com.example.campusflow.ui.screens.lecturer

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

    val today = Calendar.getInstance().getDisplayName(
        Calendar.DAY_OF_WEEK, Calendar.LONG, java.util.Locale.getDefault()
    ) ?: ""
    val todaySchedule = schedule.filter { it.dayOfWeek.equals(today, ignoreCase = true) }

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
                                .background(Brush.radialGradient(listOf(TealAccent, NavyPrimary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user?.name?.take(1) ?: "L", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("CampusFlow", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                            Text("Lecturer Portal", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = NavyPrimary)
                    }
                    IconButton(onClick = {
                        authRepository.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = ErrorRed)
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
                // Hero banner — teal accent for lecturer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF00695C), TealAccent)))
                        .padding(24.dp)
                ) {
                    Column {
                        Text("$greeting,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text("Prof. ${user?.name ?: "Lecturer"}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(user?.department ?: "Department of Computing", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))
                        Row {
                            LecturerStat("Today's Classes", "${todaySchedule.size}")
                            Spacer(Modifier.width(28.dp))
                            LecturerStat("At-Risk Students", "${atRiskStudents.size}")
                            Spacer(Modifier.width(28.dp))
                            LecturerStat("Total Courses", "${schedule.map { it.courseId }.distinct().size.coerceAtLeast(3)}")
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                    )
                }

                // At-risk alert
                if (atRiskStudents.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        onClick = { navController.navigate(Screen.LecturerResults.route) }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${atRiskStudents.size} At-Risk Student${if (atRiskStudents.size != 1) "s" else ""}", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 13.sp)
                                Text("Attendance below 75% — tap to review results", fontSize = 12.sp, color = TextSecondary)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = WarningAmber)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Stat cards
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardCard(
                        title = "Today's Classes",
                        value = "${todaySchedule.size}",
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF00695C),
                        onClick = { navController.navigate(Screen.LecturerSchedule.route) }
                    )
                    DashboardCard(
                        title = "At-Risk",
                        value = "${atRiskStudents.size}",
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f),
                        containerColor = if (atRiskStudents.isEmpty()) NavyPrimary else WarningAmber,
                        onClick = { navController.navigate(Screen.LecturerResults.route) }
                    )
                    DashboardCard(
                        title = "Courses",
                        value = "${schedule.map { it.courseId }.distinct().size.coerceAtLeast(3)}",
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        containerColor = SkyBlue,
                        onClick = { navController.navigate(Screen.LecturerSchedule.route) }
                    )
                }

                Spacer(Modifier.height(20.dp))
                SectionHeader("Management", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionItem(icon = Icons.Default.CalendarMonth, label = "Teaching Schedule", subtitle = "View weekly timetable") {
                        navController.navigate(Screen.LecturerSchedule.route)
                    }
                    ActionItem(icon = Icons.Default.QrCodeScanner, label = "Mark Attendance (QR)", subtitle = "Generate QR or search student") {
                        navController.navigate(Screen.LecturerAttendance.route)
                    }
                    ActionItem(icon = Icons.Default.UploadFile, label = "Upload Results", subtitle = "Enter CAT & exam scores") {
                        navController.navigate(Screen.LecturerResults.route)
                    }
                    ActionItem(icon = Icons.Default.LibraryBooks, label = "Course Materials", subtitle = "Upload slides, notes & PDFs") {
                        navController.navigate(Screen.LecturerMaterials.createRoute(user?.department ?: "general"))
                    }
                    ActionItem(icon = Icons.Default.Forum, label = "Chat with Students", subtitle = "Send messages & replies") {
                        navController.navigate(Screen.LecturerChat.createRoute("students", "Student Support"))
                    }
                    ActionItem(icon = Icons.Default.Gavel, label = "Review Grade Appeals", subtitle = "Pending appeals from students", badge = if (atRiskStudents.isNotEmpty()) "${atRiskStudents.size}" else "") {
                        navController.navigate(Screen.LecturerAppeals.route)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LecturerStat(label: String, value: String) {
    Column {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}