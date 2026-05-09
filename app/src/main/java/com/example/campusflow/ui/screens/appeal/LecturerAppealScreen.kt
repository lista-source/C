package com.example.campusflow.ui.screens.appeal

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
import com.example.campusflow.data.model.AppealStatus
import com.example.campusflow.data.model.GradeAppeal
import com.example.campusflow.data.model.TimetableEntry
import com.example.campusflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerAppealScreen(
    navController: NavController,
    schedule: List<TimetableEntry>,
    viewModel: AppealViewModel = hiltViewModel()
) {
    val appeals   by viewModel.appeals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message   by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val courseIds      = remember(schedule) { schedule.map { it.courseId }.distinct() }
    var filterStatus   by remember { mutableStateOf<AppealStatus?>(AppealStatus.PENDING) }
    var respondingTo   by remember { mutableStateOf<GradeAppeal?>(null) }

    LaunchedEffect(courseIds) {
        if (courseIds.isNotEmpty()) viewModel.loadPendingAppealsForLecturer(courseIds)
    }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val filtered = if (filterStatus == null) appeals else appeals.filter { it.status == filterStatus }

    val pending  = appeals.count { it.status == AppealStatus.PENDING }
    val resolved = appeals.count { it.status == AppealStatus.RESOLVED }
    val rejected = appeals.count { it.status == AppealStatus.REJECTED }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Grade Appeals", fontWeight = FontWeight.Bold, color = NavyPrimary)
                        if (pending > 0) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(WarningAmber).padding(horizontal = 7.dp, vertical = 2.dp)
                            ) { Text("$pending pending", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Summary hero
            if (appeals.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF00695C), TealAccent)))
                        .padding(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Appeals Inbox", color = Color.White.copy(0.7f), fontSize = 12.sp)
                            Text("${appeals.size} Total", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Courses: ${courseIds.size}", color = Color.White.copy(0.6f), fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LecturerAppealStat("Pending", "$pending", WarningAmber)
                            LecturerAppealStat("Resolved", "$resolved", SuccessGreen)
                            LecturerAppealStat("Rejected", "$rejected", ErrorRed)
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
                            selectedContainerColor = Color(0xFF00695C),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = filterStatus == status,
                            selectedBorderColor = Color(0xFF00695C), borderColor = DividerGrey
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
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = TextHint, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No appeals found", fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 16.sp)
                        Text("Check back when students submit appeals", fontSize = 13.sp, color = TextHint)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
                ) {
                    items(filtered, key = { it.id }) { appeal ->
                        LecturerAppealCard(appeal = appeal, onRespond = { respondingTo = appeal })
                    }
                }
            }
        }
    }

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
private fun LecturerAppealStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(0.65f), fontSize = 10.sp)
    }
}

@Composable
private fun LecturerAppealCard(appeal: GradeAppeal, onRespond: () -> Unit) {
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
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(statusColor.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(appeal.studentName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        Text(appeal.courseName, fontSize = 12.sp, color = TextSecondary)
                        Text("Submitted: ${dateFormat.format(Date(appeal.createdAt))}", fontSize = 11.sp, color = TextHint)
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusColor.copy(0.1f)).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) { Text(appeal.status.name, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(BackgroundGrey).padding(12.dp)
                ) {
                    Column {
                        Text("Student's reason:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(appeal.reason, fontSize = 13.sp, color = TextPrimary, lineHeight = 19.sp)
                    }
                }
                if (appeal.lecturerComment.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(NavyPrimary.copy(0.06f)).padding(12.dp)
                    ) {
                        Column {
                            Text("Your response:", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(appeal.lecturerComment, fontSize = 13.sp, color = TextPrimary, lineHeight = 19.sp)
                        }
                    }
                }
                if (appeal.status == AppealStatus.PENDING || appeal.status == AppealStatus.REVIEWED) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onRespond,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (appeal.status == AppealStatus.PENDING) "Respond to Appeal" else "Update Response")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RespondToAppealDialog(
    appeal: GradeAppeal,
    onDismiss: () -> Unit,
    onRespond: (AppealStatus, String) -> Unit
) {
    var comment        by remember { mutableStateOf(appeal.lecturerComment) }
    var selectedStatus by remember { mutableStateOf(AppealStatus.RESOLVED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Reply, contentDescription = null, tint = NavyPrimary)
                Spacer(Modifier.width(10.dp))
                Text("Respond to Appeal", fontWeight = FontWeight.Bold, color = NavyPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundGrey)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${appeal.studentName} · ${appeal.courseName}", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                        Text(appeal.reason, fontSize = 12.sp, color = TextSecondary, maxLines = 3)
                    }
                }
                Text("Your Decision:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedStatus == AppealStatus.RESOLVED,
                        onClick = { selectedStatus = AppealStatus.RESOLVED },
                        label = { Text("Resolve", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SuccessGreen, selectedLabelColor = Color.White, selectedLeadingIconColor = Color.White),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedStatus == AppealStatus.RESOLVED, selectedBorderColor = SuccessGreen, borderColor = DividerGrey)
                    )
                    FilterChip(
                        selected = selectedStatus == AppealStatus.REJECTED,
                        onClick = { selectedStatus = AppealStatus.REJECTED },
                        label = { Text("Reject", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ErrorRed, selectedLabelColor = Color.White, selectedLeadingIconColor = Color.White),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedStatus == AppealStatus.REJECTED, selectedBorderColor = ErrorRed, borderColor = DividerGrey)
                    )
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Comment to Student") },
                    placeholder = { Text("Explain your decision clearly...") },
                    minLines = 3, maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRespond(selectedStatus, comment) },
                enabled = comment.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Submit Response") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

