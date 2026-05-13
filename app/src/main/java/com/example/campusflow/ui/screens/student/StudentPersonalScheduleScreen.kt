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
import androidx.navigation.NavController
import com.example.campusflow.data.model.TimetableEntry
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPersonalScheduleScreen(
    navController: NavController,
    studentId: String,
    repository: FirebaseRepository
) {
    var entries        by remember { mutableStateOf<List<TimetableEntry>>(emptyList()) }
    var isLoading      by remember { mutableStateOf(true) }
    var showAddDialog  by remember { mutableStateOf(false) }
    var selectedDay    by remember { mutableStateOf("Monday") }
    val scope          = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val days = listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")

    LaunchedEffect(studentId) {
        isLoading = true
        entries = repository.getPersonalSchedule(studentId)
        isLoading = false
    }

    val grouped = entries.groupBy { it.dayOfWeek }
    val todayEntries = grouped[selectedDay] ?: emptyList()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Study Schedule", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NavyPrimary
            ) { Icon(Icons.Default.Add, contentDescription = "Add slot", tint = Color.White) }
        },
        containerColor = BackgroundGrey
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Hero
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(NavyPrimary, CornflowerBlue)))
                    .padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Personal Schedule", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("$selectedDay · ${todayEntries.size} slots", color = Color.White.copy(0.7f), fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${entries.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Total Slots", color = Color.White.copy(0.65f), fontSize = 10.sp)
                    }
                }
            }

            // Day tabs
            ScrollableTabRow(
                selectedTabIndex = days.indexOf(selectedDay).coerceAtLeast(0),
                containerColor = Color.White, contentColor = NavyPrimary, edgePadding = 12.dp,
                indicator = { tabPositions ->
                    val idx = days.indexOf(selectedDay).coerceAtLeast(0)
                    if (idx < tabPositions.size) TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[idx]), color = TealAccent)
                }
            ) {
                days.forEach { day ->
                    Tab(
                        selected = selectedDay == day, onClick = { selectedDay = day },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day.take(3), fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                                if (!grouped[day].isNullOrEmpty()) Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(TealAccent))
                            }
                        },
                        selectedContentColor = NavyPrimary, unselectedContentColor = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavyPrimary) }
            } else if (todayEntries.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventNote, contentDescription = null, tint = TextHint, modifier = Modifier.size(52.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No slots on $selectedDay", fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 16.sp)
                        TextButton(onClick = { showAddDialog = true }) { Text("+ Add a study slot") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(todayEntries.sortedBy { it.startTime }, key = { it.id }) { entry ->
                        PersonalSlotCard(entry) {
                            // Delete
                            scope.launch {
                                repository.deletePersonalScheduleEntry(entry.id)
                                entries = entries.filter { it.id != entry.id }
                                snackbarHostState.showSnackbar("Slot removed")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPersonalSlotDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, type, day, start, end ->
                scope.launch {
                    repository.addPersonalScheduleEntry(studentId, title, type, day, start, end)
                        .onSuccess { newEntry ->
                            entries = entries + newEntry
                            snackbarHostState.showSnackbar("✓ Slot added!")
                            showAddDialog = false
                        }
                        .onFailure { snackbarHostState.showSnackbar("Error: ${it.message}"); showAddDialog = false }
                }
            }
        )
    }
}

@Composable
private fun PersonalSlotCard(entry: TimetableEntry, onDelete: () -> Unit) {
    val typeColor = when {
        entry.courseName.contains("Study", true) || entry.courseName.contains("Revision", true) -> TealAccent
        entry.courseName.contains("Lab", true) -> WarningAmber
        entry.courseName.contains("Meeting", true) -> InfoBlue
        else -> NavyPrimary
    }
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Text(entry.startTime, fontWeight = FontWeight.Bold, color = typeColor, fontSize = 13.sp)
                Box(modifier = Modifier.width(2.dp).height(16.dp).background(DividerGrey))
                Text(entry.endTime, fontSize = 11.sp, color = TextSecondary)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.courseName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                if (entry.room.isNotBlank()) {
                    Text(entry.room, fontSize = 12.sp, color = TextSecondary)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPersonalSlotDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, type: String, day: String, start: String, end: String) -> Unit
) {
    var title    by remember { mutableStateOf("") }
    var slotType by remember { mutableStateOf("Study Session") }
    var day      by remember { mutableStateOf("Monday") }
    var start    by remember { mutableStateOf("08:00") }
    var end      by remember { mutableStateOf("10:00") }

    val days  = listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
    val types = listOf("Study Session","Revision","Lab Work","Group Meeting","Assignment","Personal")
    val times = listOf("07:00","08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00")
    var dayExp by remember { mutableStateOf(false) }
    var typeExp by remember { mutableStateOf(false) }
    var startExp by remember { mutableStateOf(false) }
    var endExp by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = NavyPrimary)
                Spacer(Modifier.width(10.dp))
                Text("Add Study Slot", fontWeight = FontWeight.Bold, color = NavyPrimary)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title (e.g. Math Revision)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary), singleLine = true
                )
                // Type picker
                ExposedDropdownMenuBox(expanded = typeExp, onExpandedChange = { typeExp = it }) {
                    OutlinedTextField(
                        value = slotType, onValueChange = {}, readOnly = true,
                        label = { Text("Type") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExp) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary)
                    )
                    ExposedDropdownMenu(expanded = typeExp, onDismissRequest = { typeExp = false }) {
                        types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { slotType = t; typeExp = false }) }
                    }
                }
                // Day
                ExposedDropdownMenuBox(expanded = dayExp, onExpandedChange = { dayExp = it }) {
                    OutlinedTextField(
                        value = day, onValueChange = {}, readOnly = true,
                        label = { Text("Day") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dayExp) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary)
                    )
                    ExposedDropdownMenu(expanded = dayExp, onDismissRequest = { dayExp = false }) {
                        days.forEach { d -> DropdownMenuItem(text = { Text(d) }, onClick = { day = d; dayExp = false }) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(expanded = startExp, onExpandedChange = { startExp = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = start, onValueChange = {}, readOnly = true, label = { Text("Start") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(startExp) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary))
                        ExposedDropdownMenu(expanded = startExp, onDismissRequest = { startExp = false }) { times.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { start = t; startExp = false }) } }
                    }
                    ExposedDropdownMenuBox(expanded = endExp, onExpandedChange = { endExp = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = end, onValueChange = {}, readOnly = true, label = { Text("End") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(endExp) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary))
                        ExposedDropdownMenu(expanded = endExp, onDismissRequest = { endExp = false }) { times.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { end = t; endExp = false }) } }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title.trim(), slotType, day, start, end) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary), shape = RoundedCornerShape(12.dp)
            ) { Text("Add Slot") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
