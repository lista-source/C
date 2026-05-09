package com.example.campusflow.ui.screens.appeal

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.AcademicResult
import com.example.campusflow.data.model.AppealStatus
import com.example.campusflow.data.model.GradeAppeal
import com.example.campusflow.data.model.User
import com.example.campusflow.ui.theme.*
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
    val appeals   by viewModel.appeals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message   by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSubmitDialog by remember { mutableStateOf(false) }
    var filterStatus     by remember { mutableStateOf<AppealStatus?>(null) }

    LaunchedEffect(user.uid) { viewModel.loadStudentAppeals(user.uid) }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val filtered = if (filterStatus == null) appeals else appeals.filter { it.status == filterStatus }
    val pending  = appeals.count { it.status == AppealStatus.PENDING }
    val resolved = appeals.count { it.status == AppealStatus.RESOLVED }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Grade Appeals", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (results.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showSubmitDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Appeal") },
                    containerColor = NavyPrimary,
                    contentColor = Color.White
                )
            }
        },
        containerColor = BackgroundGrey
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Hero summary card
            if (appeals.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(NavyPrimary, CornflowerBlue)))
                        .padding(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("My Appeals", color = Color.White.copy(0.7f), fontSize = 12.sp)
                            Text("${appeals.size} Total", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            AppealStat("Pending", "$pending", WarningAmber)
                            AppealStat("Resolved", "$resolved", SuccessGreen)
                        }
                    }
                }
            }

            // Status filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null, AppealStatus.PENDING, AppealStatus.RESOLVED, AppealStatus.REJECTED).forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = filterStatus == status,
                            selectedBorderColor = NavyPrimary, borderColor = DividerGrey
                        )
                    )
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
                filtered.isEmpty() -> Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = TextHint, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No appeals yet", fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 16.sp)
                        Text("Tap + to appeal a grade", fontSize = 13.sp, color = TextHint)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
                ) {
                    items(filtered, key = { it.id }) { appeal ->
                        StudentAppealCard(appeal)
                    }
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
                    studentId = user.uid, studentName = user.name,
                    result = result, reason = reason,
                    onSuccess = { showSubmitDialog = false }
                )
            }
        )
    }
}

@Composable
private fun AppealStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun StudentAppealCard(appeal: GradeAppeal) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val (statusColor, statusIcon) = when (appeal.status) {
        AppealStatus.PENDING  -> WarningAmber to Icons.Default.HourglassEmpty
        AppealStatus.REVIEWED -> InfoBlue     to Icons.Default.RateReview
        AppealStatus.RESOLVED -> SuccessGreen to Icons.Default.CheckCircle
        AppealStatus.REJECTED -> ErrorRed     to Icons.Default.Cancel
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Brush.horizontalGradient(listOf(statusColor, statusColor.copy(0.3f)))))
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(statusColor.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(appeal.courseName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        Text("Submitted: ${dateFormat.format(Date(appeal.createdAt))}", fontSize = 11.sp, color = TextHint)
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusColor.copy(0.1f)).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) { Text(appeal.status.name, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(12.dp))
                Text("Your Reason", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(appeal.reason, fontSize = 13.sp, color = TextPrimary, lineHeight = 19.sp)
                if (appeal.lecturerComment.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = DividerGrey)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lecturer Response", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(appeal.lecturerComment, fontSize = 13.sp, color = TextPrimary, lineHeight = 19.sp)
                    if (appeal.resolvedAt > 0L) {
                        Text("Resolved: ${dateFormat.format(Date(appeal.resolvedAt))}", fontSize = 11.sp, color = TextHint)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitAppealDialog(
    results: List<AcademicResult>,
    onDismiss: () -> Unit,
    onSubmit: (AcademicResult, String) -> Unit
) {
    var selectedResult by remember { mutableStateOf<AcademicResult?>(null) }
    var reason         by remember { mutableStateOf("") }
    var expanded       by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = NavyPrimary)
                Spacer(Modifier.width(10.dp))
                Text("Submit Grade Appeal", fontWeight = FontWeight.Bold, color = NavyPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedResult?.let { "${it.courseName} — Grade: ${it.grade}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        results.forEach { result ->
                            val gradeColor = when (result.grade) { "A" -> SuccessGreen; "B" -> TealAccent; "C" -> InfoBlue; "D" -> WarningAmber; else -> ErrorRed }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(gradeColor.copy(0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) { Text(result.grade, color = gradeColor, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(result.courseName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                            Text("Total: ${result.totalScore.toInt()} · CAT: ${result.catScore.toInt()} · Exam: ${result.examScore.toInt()}", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                },
                                onClick = { selectedResult = result; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Appeal") },
                    placeholder = { Text("Clearly explain why your grade should be reviewed...") },
                    minLines = 4, maxLines = 7,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary),
                    supportingText = {
                        Text("${reason.length} chars (min 20)", color = if (reason.length >= 20) SuccessGreen else TextHint)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedResult?.let { onSubmit(it, reason) } },
                enabled = selectedResult != null && reason.length >= 20,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Submit Appeal") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AppealStatusChip(status: AppealStatus) {
    val (label, color, icon) = when (status) {
        AppealStatus.PENDING  -> Triple("Pending",  WarningAmber, Icons.Default.HourglassEmpty)
        AppealStatus.REVIEWED -> Triple("Reviewed", InfoBlue,     Icons.Default.RateReview)
        AppealStatus.RESOLVED -> Triple("Resolved", SuccessGreen, Icons.Default.CheckCircle)
        AppealStatus.REJECTED -> Triple("Rejected", ErrorRed,     Icons.Default.Cancel)
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color, iconContentColor = color
        )
    )
}



