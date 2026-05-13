package com.example.campusflow.ui.screens.admin

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
fun AdminCoursesScreen(
    navController: NavController,
    repository: FirebaseRepository
) {
    var courses     by remember { mutableStateOf<List<Course>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var isSaving    by remember { mutableStateOf(false) }
    var showForm    by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var code    by remember { mutableStateOf("") }
    var name    by remember { mutableStateOf("") }
    var dept    by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") }

    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        isLoading = true
        courses = repository.getAllCourses()
        isLoading = false
    }

    val filtered = courses.filter {
        searchQuery.isBlank() ||
        it.name.contains(searchQuery, true) ||
        it.code.contains(searchQuery, true) ||
        it.department.contains(searchQuery, true)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Course Management", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showForm = !showForm }) {
                        Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null, tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGrey
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(NavyDark, NavyPrimary)))
                    .padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("All Courses", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${courses.size} courses in system", color = Color.White.copy(0.7f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.School, contentDescription = null, tint = Color.White.copy(0.6f), modifier = Modifier.size(32.dp))
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Search courses...", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                trailingIcon = { if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) } },
                shape = RoundedCornerShape(14.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            // Add course form
            if (showForm) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Add New Course", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey), singleLine = true)
                            OutlinedTextField(value = credits, onValueChange = { credits = it }, label = { Text("Credits") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                        }
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey), singleLine = true)
                        OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey), singleLine = true)
                        Button(
                            onClick = {
                                if (code.isBlank() || name.isBlank()) { scope.launch { snackbarHostState.showSnackbar("Fill Code and Name") }; return@Button }
                                isSaving = true
                                scope.launch {
                                    repository.addCourse(Course(code = code.trim().uppercase(), name = name.trim(), department = dept.trim(), credits = credits.toIntOrNull() ?: 3))
                                        .onSuccess { courses = repository.getAllCourses(); code = ""; name = ""; dept = ""; credits = "3"; showForm = false; snackbarHostState.showSnackbar("✓ Course added") }
                                        .onFailure { e -> snackbarHostState.showSnackbar("Error: ${e.message}") }
                                    isSaving = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary), enabled = !isSaving
                        ) { Text("Add Course", fontWeight = FontWeight.Bold) }
                    }
                }
            }

            Text("${filtered.size} courses", modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp), fontSize = 12.sp, color = TextSecondary)

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavyPrimary) }
                filtered.isEmpty() -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextHint, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(if (searchQuery.isNotBlank()) "No results for \"$searchQuery\"" else "No courses yet", color = TextSecondary)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
                ) {
                    items(filtered, key = { it.id.ifBlank { it.code } }) { course ->
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(NavyPrimary.copy(0.1f)), contentAlignment = Alignment.Center) {
                                    Text(course.code.take(2), fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 13.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(course.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                    Text("${course.code} · ${course.department} · ${course.credits} cr", fontSize = 11.sp, color = TextSecondary)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        repository.deleteCourse(course.id)
                                        courses = courses.filter { it.id != course.id }
                                        snackbarHostState.showSnackbar("Course removed")
                                    }
                                }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
