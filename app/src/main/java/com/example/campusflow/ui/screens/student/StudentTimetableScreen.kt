package com.example.campusflow.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.campusflow.data.model.TimetableEntry
import com.example.campusflow.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTimetableScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val timetable by viewModel.timetable.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val todayName = remember {
        Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: "Monday"
    }
    var selectedDay by remember { mutableStateOf(todayName) }
    var selectedEntry by remember { mutableStateOf<TimetableEntry?>(null) }

    val grouped = timetable.groupBy { it.dayOfWeek }
    val todayEntries = grouped[selectedDay] ?: emptyList()
    val totalClasses = timetable.size
    val totalCourses = timetable.map { it.courseId }.distinct().size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Timetable", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
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

                // Hero stats banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(NavyPrimary, CornflowerBlue)))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weekly Schedule", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("$selectedDay", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${todayEntries.size} class${if (todayEntries.size != 1) "es" else ""} today",
                                color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            TimetableStat("Total\nClasses", "$totalClasses")
                            TimetableStat("Courses\nEnrolled", "$totalCourses")
                        }
                    }
                }

                // Day selector tabs (scrollable)
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
                        val hasClasses = !grouped[day].isNullOrEmpty()
                        Tab(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        day.take(3),
                                        fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                    if (hasClasses) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (selectedDay == day) TealAccent else TealAccent.copy(alpha = 0.5f))
                                        )
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
                        items(todayEntries.sortedBy { it.startTime }, key = { it.id }) { entry ->
                            StudentTimetableCard(
                                entry = entry,
                                isNow = isCurrentlyOngoing(entry),
                                onClick = { selectedEntry = entry }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail bottom sheet
    selectedEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedEntry = null },
            title = {
                Text(entry.courseName, fontWeight = FontWeight.Bold, color = NavyPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(Icons.Default.MenuBook, "Course Code", entry.courseId)
                    DetailRow(Icons.Default.Schedule, "Time", "${entry.startTime} – ${entry.endTime}")
                    DetailRow(Icons.Default.Room, "Room / Venue", entry.room.ifBlank { "TBA" })
                    DetailRow(Icons.Default.CalendarMonth, "Day", entry.dayOfWeek)
                    if (entry.department.isNotBlank()) DetailRow(Icons.Default.Business, "Department", entry.department)
                    if (isCurrentlyOngoing(entry)) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f))
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
                                Spacer(Modifier.width(8.dp))
                                Text("This class is happening right now!", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
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
            }
        )
    }
}

@Composable
private fun StudentTimetableCard(
    entry: TimetableEntry,
    isNow: Boolean,
    onClick: () -> Unit
) {
    val cardColor = if (isNow) NavyPrimary else Color.White
    val textColor = if (isNow) Color.White else TextPrimary
    val subColor  = if (isNow) Color.White.copy(alpha = 0.75f) else TextSecondary

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().shadow(if (isNow) 6.dp else 2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Time column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(entry.startTime, fontWeight = FontWeight.Bold, color = if (isNow) TealAccent else NavyPrimary, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(if (isNow) TealAccent.copy(0.5f) else DividerGrey)
                )
                Text(entry.endTime, fontSize = 11.sp, color = subColor)
            }

            Spacer(Modifier.width(14.dp))

            // Content
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.courseName, fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 15.sp)
                    if (isNow) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TealAccent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(entry.courseId, fontSize = 12.sp, color = subColor)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Room, contentDescription = null, tint = subColor, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(entry.room.ifBlank { "Room TBA" }, fontSize = 12.sp, color = subColor)
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isNow) Color.White.copy(0.6f) else TextHint)
        }
    }
}

@Composable
private fun TimetableStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp, lineHeight = 13.sp)
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 13.sp)
        }
    }
}

private fun isCurrentlyOngoing(entry: TimetableEntry): Boolean {
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
fun TimetableItem(entry: TimetableEntry) {
    StudentTimetableCard(entry = entry, isNow = isCurrentlyOngoing(entry), onClick = {})
}




