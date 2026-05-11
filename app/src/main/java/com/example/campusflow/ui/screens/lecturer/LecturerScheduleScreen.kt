package com.example.campusflow.ui.screens.lecturer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.campusflow.data.model.AcademicResult
import com.example.campusflow.data.model.TimetableEntry
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerScheduleScreen(
    navController: NavController,
    authRepository: AuthRepository,
    repository: FirebaseRepository? = null,
    viewModel: LecturerViewModel = hiltViewModel()
) {
    val schedule  by viewModel.schedule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val todayName = remember {
        Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: "Monday"
    }
    var selectedDay       by remember { mutableStateOf(todayName) }
    var selectedEntry     by remember { mutableStateOf<TimetableEntry?>(null) }
    val classResults    by viewModel.classResults.collectAsState()
    val classAttendance by viewModel.classAttendance.collectAsState()
    val vmMessage       by viewModel.message.collectAsState()
    val scope           = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var markStudentId   by remember { mutableStateOf("") }
    var showMarkDialog  by remember { mutableStateOf(false) }
    var markForCourse   by remember { mutableStateOf("") }
    var lecturerId      by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authRepository.currentUser?.uid?.let { uid ->
            lecturerId = uid
            viewModel.loadSchedule(uid)
        }
    }

    LaunchedEffect(vmMessage) {
        vmMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }

    val grouped     = schedule.groupBy { it.dayOfWeek }
    val todayEntries = (grouped[selectedDay] ?: emptyList()).sortedBy { it.startTime }
    val totalClasses = schedule.size
    val totalCourses = schedule.map { it.courseId }.distinct().size

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Teaching Schedule", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authRepository.currentUser?.uid?.let { scope.launch { viewModel.loadSchedule(it) } }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGrey
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                // Hero banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF00695C), TealAccent)))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Teaching Schedule", color = Color.White.copy(0.7f), fontSize = 12.sp)
                            Text("$selectedDay", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${todayEntries.size} class${if (todayEntries.size != 1) "es" else ""} scheduled",
                                color = Color.White.copy(0.7f), fontSize = 12.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            ScheduleStat("Total\nSlots", "$totalClasses")
                            ScheduleStat("Courses\nTaught", "$totalCourses")
                        }
                    }
                }

                // Day tabs
                ScrollableTabRow(
                    selectedTabIndex = days.indexOf(selectedDay).coerceAtLeast(0),
                    containerColor = Color.White,
                    contentColor = NavyPrimary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        val idx = days.indexOf(selectedDay).coerceAtLeast(0)
                        if (idx < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[idx]),
                                color = TealAccent
                            )
                        }
                    }
                ) {
                    days.forEach { day ->
                        val count = grouped[day]?.size ?: 0
                        Tab(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(day.take(3), fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                                    if (count > 0) {
                                        Text("$count", fontSize = 10.sp, color = if (selectedDay == day) TealAccent else TextHint)
                                    }
                                }
                            },
                            selectedContentColor = NavyPrimary,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (todayEntries.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventAvailable, contentDescription = null, tint = TextHint, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No classes on $selectedDay", fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 16.sp)
                            Text("Enjoy your free day!", fontSize = 13.sp, color = TextHint)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(todayEntries, key = { it.id }) { entry ->
                            LecturerScheduleCard(
                                entry = entry,
                                onViewStudents = {
                                    selectedEntry = entry
                                    viewModel.loadClassStudents(entry.courseId)
                                },
                                onTakeAttendance = {
                                    markForCourse  = entry.courseId
                                    showMarkDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Mark attendance dialog
    if (showMarkDialog) {
        AlertDialog(
            onDismissRequest = { showMarkDialog = false },
            title = { Text("Mark Student Attendance", fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter the student ID to mark as present for: $markForCourse", fontSize = 13.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = markStudentId,
                        onValueChange = { markStudentId = it },
                        label = { Text("Student ID / Reg No") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (markStudentId.isNotBlank()) {
                            viewModel.markStudentAttendance(markStudentId.trim(), markForCourse, lecturerId)
                            markStudentId  = ""
                            showMarkDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = markStudentId.isNotBlank()
                ) { Text("Mark Present") }
            },
            dismissButton = {
                TextButton(onClick = { showMarkDialog = false; markStudentId = "" }) { Text("Cancel") }
            }
        )
    }

    // Class detail / student roster dialog
    selectedEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedEntry = null },
            title = {
                Column {
                    Text(entry.courseName, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 16.sp)
                    Text("${entry.startTime} – ${entry.endTime} · Room ${entry.room}", fontSize = 12.sp, color = TextSecondary)
                }
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 420.dp)) {
                    ClassDetailRow(Icons.Default.MenuBook, "Course ID", entry.courseId)
                    ClassDetailRow(Icons.Default.Room, "Venue", entry.room.ifBlank { "TBA" })
                    ClassDetailRow(Icons.Default.Business, "Department", entry.department.ifBlank { "N/A" })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerGrey)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Enrolled Students (${classResults.size})",
                            fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NavyPrimary)
                        }
                    } else if (classResults.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            Text("No results recorded yet for this course.", fontSize = 13.sp, color = TextHint)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(classResults) { result ->
                                val gradeColor = when (result.grade) {
                                    "A" -> SuccessGreen; "B" -> TealAccent; "C" -> InfoBlue
                                    "D" -> WarningAmber; else -> ErrorRed
                                }
                                // Check if this student has been marked present today
                                val markedToday = classAttendance.any { att ->
                                    att.studentId == result.studentId &&
                                    System.currentTimeMillis() - att.date < 86400000L
                                }
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = BackgroundGrey)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(NavyPrimary.copy(0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(result.studentId.take(2).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(result.studentId, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 13.sp)
                                            Text("Total: ${result.totalScore.toInt()}/100", fontSize = 11.sp, color = TextSecondary)
                                        }
                                        if (markedToday) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Present", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    viewModel.markStudentAttendance(result.studentId, entry.courseId, lecturerId)
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Mark", tint = NavyPrimary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(gradeColor.copy(0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(result.grade, color = gradeColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedEntry = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) { Text("Close") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    markForCourse  = entry.courseId
                    showMarkDialog = true
                    selectedEntry  = null
                }) { Text("Mark Attendance") }
            }
        )
    }
}

@Composable
private fun LecturerScheduleCard(
    entry: TimetableEntry,
    onViewStudents: () -> Unit,
    onTakeAttendance: () -> Unit
) {
    val isNow = isLecturerClassNow(entry)
    val cardColor = if (isNow) Color(0xFF00695C) else Color.White
    val textColor = if (isNow) Color.White else TextPrimary
    val subColor  = if (isNow) Color.White.copy(0.75f) else TextSecondary

    Card(
        modifier = Modifier.fillMaxWidth().shadow(if (isNow) 6.dp else 2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                    Text(entry.startTime, fontWeight = FontWeight.Bold, color = if (isNow) TealAccent else NavyPrimary, fontSize = 13.sp)
                    Box(modifier = Modifier.width(2.dp).height(18.dp).background(if (isNow) TealAccent.copy(0.4f) else DividerGrey))
                    Text(entry.endTime, fontSize = 11.sp, color = subColor)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.courseName, fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)
                        if (isNow) {
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(TealAccent).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(entry.courseId, fontSize = 12.sp, color = subColor)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Room, contentDescription = null, tint = subColor, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(entry.room.ifBlank { "Room TBA" }, fontSize = 12.sp, color = subColor)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = if (isNow) Color.White.copy(0.2f) else DividerGrey)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onViewStudents,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isNow) Color.White else NavyPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isNow) Color.White.copy(0.4f) else NavyPrimary.copy(0.3f))
                ) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Students", fontSize = 12.sp)
                }
                Button(
                    onClick = onTakeAttendance,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isNow) TealAccent else NavyPrimary)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Attendance", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ScheduleStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(label, color = Color.White.copy(0.65f), fontSize = 10.sp, lineHeight = 13.sp)
    }
}

@Composable
private fun ClassDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("$label: ", fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

private fun isLecturerClassNow(entry: TimetableEntry): Boolean {
    val now = Calendar.getInstance()
    val today = now.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: ""
    if (!entry.dayOfWeek.equals(today, ignoreCase = true)) return false
    return try {
        val (sh, sm) = entry.startTime.split(":").map { it.trim().toInt() }
        val (eh, em) = entry.endTime.split(":").map { it.trim().toInt() }
        val nowMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        nowMins in (sh * 60 + sm)..(eh * 60 + em)
    } catch (e: Exception) { false }
}

@Composable
fun ScheduleCard(entry: TimetableEntry) {
    LecturerScheduleCard(entry = entry, onViewStudents = {}, onTakeAttendance = {})
}
