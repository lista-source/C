package com.example.campusflow.ui.screens.lecturer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.campusflow.data.model.TimetableEntry
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerAddTimetableScreen(
    navController: NavController,
    lecturerId: String,
    department: String,
    repository: FirebaseRepository
) {
    var courseName  by remember { mutableStateOf("") }
    var courseCode  by remember { mutableStateOf("") }
    var dayOfWeek   by remember { mutableStateOf("Monday") }
    var startTime   by remember { mutableStateOf("08:00") }
    var endTime     by remember { mutableStateOf("10:00") }
    var room        by remember { mutableStateOf("") }
    var isSaving    by remember { mutableStateOf(false) }

    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val days  = listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")
    val times = listOf("07:00","08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00")

    var dayExpanded   by remember { mutableStateOf(false) }
    var startExpanded by remember { mutableStateOf(false) }
    var endExpanded   by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Class Slot", fontWeight = FontWeight.Bold, color = NavyPrimary) },
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
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF00695C), TealAccent)))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Create Class Slot", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Add to your weekly teaching schedule", color = Color.White.copy(0.7f), fontSize = 12.sp)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Course Details", fontWeight = FontWeight.Bold, color = TextPrimary)

                    AddTextField("Course Name", courseName, { courseName = it }, Icons.Default.MenuBook)
                    AddTextField("Course Code (e.g. CS101)", courseCode, { courseCode = it }, Icons.Default.Tag)
                    AddTextField("Room / Venue", room, { room = it }, Icons.Default.Room)

                    // Day dropdown
                    ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = it }) {
                        OutlinedTextField(
                            value = dayOfWeek,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Day of Week") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NavyPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey)
                        )
                        ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                            days.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = { dayOfWeek = day; dayExpanded = false }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Start time
                        ExposedDropdownMenuBox(expanded = startExpanded, onExpandedChange = { startExpanded = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = startTime, onValueChange = {}, readOnly = true,
                                label = { Text("Start") },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey)
                            )
                            ExposedDropdownMenu(expanded = startExpanded, onDismissRequest = { startExpanded = false }) {
                                times.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { startTime = t; startExpanded = false }) }
                            }
                        }
                        // End time
                        ExposedDropdownMenuBox(expanded = endExpanded, onExpandedChange = { endExpanded = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = endTime, onValueChange = {}, readOnly = true,
                                label = { Text("End") },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey)
                            )
                            ExposedDropdownMenu(expanded = endExpanded, onDismissRequest = { endExpanded = false }) {
                                times.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { endTime = t; endExpanded = false }) }
                            }
                        }
                    }
                }
            }

            // Preview card
            if (courseName.isNotBlank() || courseCode.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(0.06f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Preview, contentDescription = null, tint = NavyPrimary)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(courseName.ifBlank { courseCode }, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("$dayOfWeek · $startTime – $endTime · ${room.ifBlank { "Room TBA" }}", fontSize = 12.sp, color = TextSecondary)
                            Text(department, fontSize = 11.sp, color = NavyPrimary)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (courseName.isBlank() || courseCode.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please fill Course Name and Code") }
                        return@Button
                    }
                    isSaving = true
                    scope.launch {
                        val entry = TimetableEntry(
                            courseId   = courseCode.trim().uppercase(),
                            courseName = courseName.trim(),
                            dayOfWeek  = dayOfWeek,
                            startTime  = startTime,
                            endTime    = endTime,
                            room       = room.trim(),
                            lecturerId = lecturerId,
                            department = department
                        )
                        repository.addTimetableEntry(entry)
                            .onSuccess {
                                snackbarHostState.showSnackbar("✓ Class slot added to schedule!")
                                courseName = ""; courseCode = ""; room = ""
                            }
                            .onFailure { snackbarHostState.showSnackbar("Error: ${it.message}") }
                        isSaving = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C)),
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add to Schedule", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun AddTextField(label: String, value: String, onChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
        singleLine = true
    )
}
