package com.example.campusflow.ui.screens.lecturer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.campusflow.data.model.AcademicResult
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerResultsScreen(
    navController: NavController,
    repository: FirebaseRepository
) {
    var studentId    by remember { mutableStateOf("") }
    var courseId     by remember { mutableStateOf("") }
    var courseName   by remember { mutableStateOf("") }
    var catScore     by remember { mutableStateOf("") }
    var examScore    by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var lastSubmitted by remember { mutableStateOf<AcademicResult?>(null) }

    val scope             = rememberCoroutineScope()
    val snackbarHostState  = remember { SnackbarHostState() }

    val cat   = catScore.toDoubleOrNull()  ?: 0.0
    val exam  = examScore.toDoubleOrNull() ?: 0.0
    val total = (cat + exam).coerceAtMost(100.0)
    val grade = gradeFromTotal(total)
    val gradeColor = when (grade) {
        "A" -> SuccessGreen; "B" -> TealAccent; "C" -> InfoBlue; "D" -> WarningAmber; else -> ErrorRed
    }

    val allFilled = studentId.isNotBlank() && courseId.isNotBlank() && courseName.isNotBlank() &&
            catScore.isNotBlank() && examScore.isNotBlank()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Upload Results", fontWeight = FontWeight.Bold, color = NavyPrimary) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live preview banner
            if (catScore.isNotBlank() || examScore.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(NavyPrimary, SkyBlue)))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Live Preview", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("Total: ${total.toInt()} / 100", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(studentId.ifBlank { "Student ID" }, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(grade, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                }
            }

            // Form card
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Student & Course", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                    ResultTextField("Student ID / Reg No", studentId, { studentId = it }, Icons.Default.Person)
                    ResultTextField("Course Code (e.g. CS101)", courseId, { courseId = it }, Icons.Default.MenuBook)
                    ResultTextField("Course Name", courseName, { courseName = it }, Icons.Default.Article)

                    HorizontalDivider(color = DividerGrey)
                    Text("Scores", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = catScore,
                            onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) catScore = it },
                            label = { Text("CAT Score") },
                            supportingText = { Text("/30") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = examScore,
                            onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) examScore = it },
                            label = { Text("Exam Score") },
                            supportingText = { Text("/70") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                            singleLine = true
                        )
                    }

                    // Grade scale reference
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(BackgroundGrey).padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("A≥70", "B≥60", "C≥50", "D≥40", "F<40").forEach {
                            Text(it, fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Last submitted card
            lastSubmitted?.let { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.08f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Last uploaded: ${r.courseName}", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                            Text("${r.studentId} · Grade: ${r.grade} · Total: ${r.totalScore.toInt()}", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            Button(
                onClick = {
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
                                snackbarHostState.showSnackbar("✓ Result uploaded for ${studentId.trim()}")
                                lastSubmitted = result
                                studentId = ""; courseId = ""; courseName = ""
                                catScore = ""; examScore = ""
                            }
                            .onFailure {
                                snackbarHostState.showSnackbar("Error: ${it.message}")
                            }
                        isSubmitting = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                enabled = allFilled && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Submit Result", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun ResultTextField(label: String, value: String, onChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
        singleLine = true
    )
}

private fun gradeFromTotal(total: Double): String = when {
    total >= 70 -> "A"
    total >= 60 -> "B"
    total >= 50 -> "C"
    total >= 40 -> "D"
    else        -> "F"
}



