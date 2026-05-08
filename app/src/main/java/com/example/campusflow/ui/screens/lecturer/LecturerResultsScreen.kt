package com.example.campusflow.ui.screens.lecturer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusflow.data.model.AcademicResult
import com.example.campusflow.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerResultsScreen(
    navController: NavController,
    repository: FirebaseRepository
) {
    var studentId  by remember { mutableStateOf("") }
    var courseId   by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var catScore   by remember { mutableStateOf("") }
    var examScore  by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope            = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Upload Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Enter Result Details", style = MaterialTheme.typography.titleLarge)
            }

            item {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID / Reg No") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = courseId,
                    onValueChange = { courseId = it },
                    label = { Text("Course Code (e.g. CS101)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = catScore,
                        onValueChange = { catScore = it },
                        label = { Text("CAT Score") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = examScore,
                        onValueChange = { examScore = it },
                        label = { Text("Exam Score") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            // Live preview of computed total + grade
            item {
                val cat   = catScore.toDoubleOrNull()  ?: 0.0
                val exam  = examScore.toDoubleOrNull() ?: 0.0
                val total = cat + exam
                val grade = gradeFromTotal(total)
                if (catScore.isNotBlank() || examScore.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total: $total / 100", style = MaterialTheme.typography.bodyLarge)
                            Text("Grade: $grade", style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val cat   = catScore.toDoubleOrNull()  ?: 0.0
                        val exam  = examScore.toDoubleOrNull() ?: 0.0
                        val total = cat + exam
                        if (studentId.isBlank() || courseId.isBlank() || courseName.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please fill all fields") }
                            return@Button
                        }
                        val result = AcademicResult(
                            studentId  = studentId.trim(),
                            courseId   = courseId.trim(),
                            courseName = courseName.trim(),
                            catScore   = cat,
                            examScore  = exam,
                            totalScore = total,
                            grade      = gradeFromTotal(total)
                        )
                        isSubmitting = true
                        scope.launch {
                            repository.uploadResult(result)
                                .onSuccess {
                                    snackbarHostState.showSnackbar("Result uploaded successfully!")
                                    studentId = ""; courseId = ""; courseName = ""
                                    catScore = ""; examScore = ""
                                }
                                .onFailure {
                                    snackbarHostState.showSnackbar("Error: ${it.message}")
                                }
                            isSubmitting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Submit Result")
                }
            }
        }
    }
}

private fun gradeFromTotal(total: Double): String = when {
    total >= 70 -> "A"
    total >= 60 -> "B"
    total >= 50 -> "C"
    total >= 40 -> "D"
    else        -> "F"
}
