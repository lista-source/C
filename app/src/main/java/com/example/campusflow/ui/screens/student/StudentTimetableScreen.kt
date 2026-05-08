package com.example.campusflow.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.TimetableEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTimetableScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val timetable  by viewModel.timetable.collectAsState()
    val isLoading  by viewModel.isLoading.collectAsState()

    val grouped = timetable.groupBy { it.dayOfWeek }
    val days    = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Timetable") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            timetable.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No classes scheduled.", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Your timetable will appear here once assigned.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    days.forEach { day ->
                        val entries = grouped[day]
                        if (!entries.isNullOrEmpty()) {
                            item(key = "header_$day") {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                                HorizontalDivider()
                            }
                            items(items = entries, key = { it.id }) { entry ->
                                TimetableItem(entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableItem(entry: TimetableEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = entry.courseName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${entry.startTime} – ${entry.endTime}", style = MaterialTheme.typography.bodyMedium)
                Text("Room: ${entry.room}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Course: ${entry.courseId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
