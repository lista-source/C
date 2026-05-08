package com.example.campusflow.ui.screens.appeal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.AppealStatus
import com.example.campusflow.data.model.GradeAppeal
import com.example.campusflow.data.model.TimetableEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerAppealScreen(
    navController: NavController,
    schedule: List<TimetableEntry>,          // pass from LecturerViewModel
    viewModel: AppealViewModel = hiltViewModel()
) {
    val appeals by viewModel.appeals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    // Use the first course in schedule as default; lecturer can filter later
    val courseIds = remember(schedule) { schedule.map { it.courseId }.distinct() }
    val selectedCourseId = remember(courseIds) { courseIds.firstOrNull() ?: "" }

    var filterStatus by remember { mutableStateOf<AppealStatus?>(AppealStatus.PENDING) }
    var respondingTo by remember { mutableStateOf<GradeAppeal?>(null) }

    LaunchedEffect(courseIds) {
        if (courseIds.isNotEmpty()) {
            viewModel.loadPendingAppealsForLecturer(courseIds)
        }
    }

    message?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    val filtered = when (filterStatus) {
        null -> appeals
        else -> appeals.filter { it.status == filterStatus }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grade Appeals") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            message?.let {
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(it) }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Status filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null, AppealStatus.PENDING, AppealStatus.RESOLVED, AppealStatus.REJECTED).forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All") }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Inbox, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Text("No appeals found", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filtered) { appeal ->
                        LecturerAppealCard(
                            appeal = appeal,
                            onRespond = { respondingTo = appeal }
                        )
                    }
                }
            }
        }
    }

    // Response dialog
    respondingTo?.let { appeal ->
        RespondToAppealDialog(
            appeal = appeal,
            onDismiss = { respondingTo = null },
            onRespond = { status, comment ->
                viewModel.respondToAppeal(
                    appealId = appeal.id,
                    status = status,
                    comment = comment,
                    courseId = appeal.courseId
                )
                respondingTo = null
            }
        )
    }
}

@Composable
private fun LecturerAppealCard(
    appeal: GradeAppeal,
    onRespond: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(appeal.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(appeal.courseName, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
                AppealStatusChip(appeal.status)
            }

            Text(
                text = "Submitted: ${dateFormat.format(Date(appeal.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            HorizontalDivider()

            Text("Student's reason:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(appeal.reason, style = MaterialTheme.typography.bodyMedium)

            if (appeal.lecturerComment.isNotBlank()) {
                HorizontalDivider()
                Text("Your response:", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(appeal.lecturerComment, style = MaterialTheme.typography.bodyMedium)
            }

            if (appeal.status == AppealStatus.PENDING || appeal.status == AppealStatus.REVIEWED) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onRespond) {
                        Text(if (appeal.status == AppealStatus.PENDING) "Respond" else "Update Response")
                    }
                }
            }
        }
    }
}

@Composable
private fun RespondToAppealDialog(
    appeal: GradeAppeal,
    onDismiss: () -> Unit,
    onRespond: (AppealStatus, String) -> Unit
) {
    var comment by remember { mutableStateOf(appeal.lecturerComment) }
    var selectedStatus by remember { mutableStateOf(AppealStatus.RESOLVED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Respond to Appeal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text(
                    text = "${appeal.studentName} — ${appeal.courseName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                // Outcome selector
                Text("Decision:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { selectedStatus = AppealStatus.RESOLVED },
                        colors = if (selectedStatus == AppealStatus.RESOLVED)
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Resolve")
                    }
                    OutlinedButton(
                        onClick = { selectedStatus = AppealStatus.REJECTED },
                        colors = if (selectedStatus == AppealStatus.REJECTED)
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reject")
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Comment") },
                    placeholder = { Text("Explain your decision to the student...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRespond(selectedStatus, comment) },
                enabled = comment.isNotBlank()
            ) {
                Text("Submit Response")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}