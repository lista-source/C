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
import com.example.campusflow.data.model.AcademicResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentResultsScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val results   by viewModel.results.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val averageGpa = if (results.isNotEmpty())
        results.map { gpaFromScore(it.totalScore) }.average() else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current Semester GPA", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "%.2f".format(averageGpa),
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }

                Text("Course Results", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                if (results.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No results published yet.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(results, key = { it.id }) { result ->
                            ResultItem(result)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultItem(result: AcademicResult) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = result.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Grade: ${result.grade}",
                    style = MaterialTheme.typography.titleMedium,
                    color = gradeColor(result.grade)
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CAT: ${result.catScore}",   style = MaterialTheme.typography.bodySmall)
                Text("Exam: ${result.examScore}",  style = MaterialTheme.typography.bodySmall)
                Text("Total: ${result.totalScore}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun gradeColor(grade: String) = when (grade) {
    "A"  -> MaterialTheme.colorScheme.primary
    "B"  -> MaterialTheme.colorScheme.secondary
    "F"  -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

/** Single source-of-truth GPA calculator used across student screens */
fun gpaFrom(score: Double): Double = when {
    score >= 70 -> 4.0
    score >= 60 -> 3.7
    score >= 50 -> 3.0
    score >= 40 -> 2.0
    else        -> 0.0
}
