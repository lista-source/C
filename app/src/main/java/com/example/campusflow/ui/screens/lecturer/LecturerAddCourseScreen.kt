package com.example.campusflow.ui.screens.lecturer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.campusflow.data.model.Course
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerAddCourseScreen(
    navController: NavController,
    lecturerId: String,
    department: String,
    repository: FirebaseRepository
) {
    var courses     by remember { mutableStateOf<List<Course>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var isSaving    by remember { mutableStateOf(false) }
    var showForm    by remember { mutableStateOf(false) }

    // Form fields
    var code        by remember { mutableStateOf("") }
    var name        by remember { mutableStateOf("") }
    var credits     by remember { mutableStateOf("3") }
    var dept        by remember { mutableStateOf(department) }

    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lecturerId) {
        isLoading = true
        courses   = repository.getCoursesByDepartment(department).ifEmpty { repository.getAllCourses() }
        isLoading = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Courses", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showForm = !showForm }) {
                        Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, contentDescription = "Toggle form", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGrey
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Hero banner
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(NavyPrimary, CornflowerBlue)))
                    .padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Course Registry", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${department.ifBlank { "All Departments" }} · ${courses.size} courses", color = Color.White.copy(0.7f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White.copy(0.6f), modifier = Modifier.size(32.dp))
                }
            }

            // Add course form (collapsible)
            if (showForm) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Register New Course", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                        AddTextField("Course Code (e.g. CS101)", code, { code = it }, Icons.Default.Tag)
                        AddTextField("Course Name", name, { name = it }, Icons.Default.MenuBook)
                        AddTextField("Department", dept, { dept = it }, Icons.Default.Business)
                        OutlinedTextField(
                            value = credits,
                            onValueChange = { credits = it },
                            label = { Text("Credits") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (code.isBlank() || name.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Please fill Code and Name") }
                                    return@Button
                                }
                                isSaving = true
                                scope.launch {
                                    val course = Course(
                                        code       = code.trim().uppercase(),
                                        name       = name.trim(),
                                        department = dept.trim().ifBlank { department },
                                        lecturerId = lecturerId,
                                        credits    = credits.toIntOrNull() ?: 3
                                    )
                                    repository.addCourse(course)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar("✓ Course registered: ${course.code}")
                                            courses = courses + course
                                            code = ""; name = ""; credits = "3"
                                            showForm = false
                                        }
                                        .onFailure { e -> snackbarHostState.showSnackbar("Error: ${e.message}") }
                                    isSaving = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            enabled = !isSaving
                        ) {
                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            else {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Register Course", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
                courses.isEmpty() -> Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = TextHint, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No courses yet", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        TextButton(onClick = { showForm = true }) { Text("+ Register a course") }
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
                ) {
                    items(courses, key = { it.id.ifBlank { it.code } }) { course ->
                        CourseCard(
                            course = course,
                            onDelete = {
                                scope.launch {
                                    repository.deleteCourse(course.id)
                                    courses = courses.filter { it.id != course.id }
                                    snackbarHostState.showSnackbar("Course removed")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(course: Course, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(NavyPrimary.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(course.code.take(2), fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 14.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(course.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                Text(course.code, fontSize = 12.sp, color = NavyPrimary)
                Text("${course.department} · ${course.credits} credits", fontSize = 11.sp, color = TextSecondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = ErrorRed)
            }
        }
    }
}
