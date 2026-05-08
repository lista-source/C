package com.example.campusflow.ui.screens.lecturer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusflow.data.model.Attendance
import com.example.campusflow.data.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerAttendanceScreen(
    navController: NavController,
    repository: FirebaseRepository? = null   // optional — used for lookup
) {
    var courseIdQuery by remember { mutableStateOf("") }
    var studentIdQuery by remember { mutableStateOf("") }
    var attendanceList by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Look Up Attendance", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = studentIdQuery,
                onValueChange = { studentIdQuery = it },
                label = { Text("Student ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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
                    isLoading = true
                    searched  = true
                    scope.launch {
                        attendanceList = repository.getStudentAttendance(studentIdQuery.trim())
                        isLoading      = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Search Attendance")
            }

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (searched) {
                val present = attendanceList.count { it.isPresent }
                val total   = attendanceList.size

                if (total > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (present * 100 / total >= 75)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Attendance Rate", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${present * 100 / total}%  ($present/$total)",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                if (attendanceList.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No attendance records found for this student.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(attendanceList, key = { it.id }) { record ->
                            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Course: ${record.courseId}", style = MaterialTheme.typography.bodyLarge)
                                        Text(dateFormat.format(Date(record.date)), style = MaterialTheme.typography.bodySmall)
                                        if (record.verified) {
                                            Text("GPS Verified ✓", style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Badge(
                                        containerColor = if (record.isPresent) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error
                                    ) {
                                        Text(if (record.isPresent) "Present" else "Absent")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
