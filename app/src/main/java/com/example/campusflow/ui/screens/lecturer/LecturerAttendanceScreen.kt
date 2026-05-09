package com.example.campusflow.ui.screens.lecturer

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.campusflow.data.model.Attendance
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerAttendanceScreen(
    navController: NavController,
    repository: FirebaseRepository? = null
) {
    var studentIdQuery by remember { mutableStateOf("") }
    var attendanceList by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var isLoading      by remember { mutableStateOf(false) }
    var searched       by remember { mutableStateOf(false) }
    val dateFormat     = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val scope          = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records", fontWeight = FontWeight.Bold, color = NavyPrimary) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search card
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Look Up Student Attendance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = studentIdQuery,
                        onValueChange = { studentIdQuery = it },
                        label = { Text("Student ID / Registration Number") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                        trailingIcon = {
                            if (studentIdQuery.isNotBlank()) {
                                IconButton(onClick = { studentIdQuery = ""; searched = false; attendanceList = emptyList() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = DividerGrey
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (studentIdQuery.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Enter a Student ID") }
                                return@Button
                            }
                            if (repository == null) {
                                scope.launch { snackbarHostState.showSnackbar("Repository not available") }
                                return@Button
                            }
                            isLoading = true; searched = true
                            scope.launch {
                                attendanceList = repository.getStudentAttendance(studentIdQuery.trim())
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Search Attendance", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (searched && !isLoading) {
                val present = attendanceList.count { it.isPresent }
                val total   = attendanceList.size

                if (total > 0) {
                    val rate = present * 100 / total
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (rate >= 75) SuccessGreen.copy(alpha = 0.08f)
                            else ErrorRed.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(studentIdQuery, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Text("$present of $total classes attended", fontSize = 12.sp, color = TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(if (rate >= 75) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$rate%", fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                        color = if (rate >= 75) SuccessGreen else ErrorRed)
                                }
                            }
                        }
                    }
                }

                if (attendanceList.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextHint, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("No records found for '${studentIdQuery}'", color = TextHint, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(attendanceList, key = { it.id }) { record ->
                            AttendanceRecordItem(record, dateFormat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRecordItem(record: Attendance, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (record.isPresent) SuccessGreen.copy(0.1f) else ErrorRed.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (record.isPresent) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (record.isPresent) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Course: ${record.courseId}", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                Text(dateFormat.format(Date(record.date)), fontSize = 11.sp, color = TextSecondary)
                if (record.verified) Text("GPS Verified ✓", fontSize = 11.sp, color = TealAccent)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (record.isPresent) SuccessGreen.copy(0.1f) else ErrorRed.copy(0.1f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    if (record.isPresent) "Present" else "Absent",
                    color = if (record.isPresent) SuccessGreen else ErrorRed,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



