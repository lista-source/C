package com.example.campusflow.ui.screens.appeal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.AcademicResult
import com.example.campusflow.data.model.AppealStatus
import com.example.campusflow.data.model.GradeAppeal
import com.example.campusflow.data.model.User
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAppealScreen(
    navController: NavController,
    user: User,
    results: List<AcademicResult>,
    viewModel: AppealViewModel = hiltViewModel()
) {
    val appeals by viewModel.appeals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user.uid) {
        viewModel.loadStudentAppeals(user.uid)
    }

    message?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
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
        floatingActionButton = {
            if (results.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showSubmitDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Appeal") }
                )
            }
        },
        snackbarHost = {
            message?.let {
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(it) }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (appeals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.RateReview, contentDescription = null,
                        modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("No appeals yet", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline)
                    Text("Tap + to appeal a grade", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appeals) { appeal ->
                    AppealCard(appeal = appeal)
                }
            }
        }
    }

    if (showSubmitDialog) {
        SubmitAppealDialog(
            results = results,
            onDismiss = { showSubmitDialog = false },
            onSubmit = { result, reason ->
                viewModel.submitAppeal(
                    studentId = user.uid,
                    studentName = user.name,
                    result = result,
                    reason = reason,
                    onSuccess = { showSubmitDialog = false }
                )
            }
        )
    }
}

@Composable
private fun AppealCard(appeal: GradeAppeal) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(appeal.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AppealStatusChip(appeal.status)
            }

            Text(
                text = "Submitted: ${dateFormat.format(Date(appeal.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            HorizontalDivider()

            Text("Your reason:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(appeal.reason, style = MaterialTheme.typography.bodyMedium)

            if (appeal.lecturerComment.isNotBlank()) {
                HorizontalDivider()
                Text("Lecturer's response:", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(appeal.lecturerComment, style = MaterialTheme.typography.bodyMedium)
                if (appeal.resolvedAt > 0L) {
                    Text(
                        text = "Resolved: ${dateFormat.format(Date(appeal.resolvedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun AppealStatusChip(status: AppealStatus) {
    val (label, color, icon) = when (status) {
        AppealStatus.PENDING  -> Triple("Pending",  MaterialTheme.colorScheme.tertiary,   Icons.Default.HourglassEmpty)
        AppealStatus.REVIEWED -> Triple("Reviewed", MaterialTheme.colorScheme.secondary,  Icons.Default.RateReview)
        AppealStatus.RESOLVED -> Triple("Resolved", MaterialTheme.colorScheme.primary,    Icons.Default.CheckCircle)
        AppealStatus.REJECTED -> Triple("Rejected", MaterialTheme.colorScheme.error,      Icons.Default.Cancel)
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color,
            iconContentColor = color
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitAppealDialog(
    results: List<AcademicResult>,
    onDismiss: () -> Unit,
    onSubmit: (AcademicResult, String) -> Unit
) {
    var selectedResult by remember { mutableStateOf<AcademicResult?>(null) }
    var reason by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Grade Appeal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Course selector
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedResult?.courseName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        results.forEach { result ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(result.courseName, fontWeight = FontWeight.Medium)
                                        Text(
                                            "Total: ${result.totalScore} | Grade: ${result.grade}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                },
                                onClick = {
                                    selectedResult = result
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Reason input
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Appeal") },
                    placeholder = { Text("Explain why you believe your grade should be reviewed...") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${reason.length} chars (min 20)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedResult?.let { onSubmit(it, reason) } },
                enabled = selectedResult != null && reason.length >= 20
            ) {
                Text("Submit Appeal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}