package com.example.campusflow.ui.screens.admin

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.Announcement
import com.example.campusflow.data.model.UserRole
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnnouncementsScreen(
    navController: NavController,
    repository: FirebaseRepository,
    viewModel: AdminViewModel = hiltViewModel()
) {
    var title        by remember { mutableStateOf("") }
    var content      by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var expanded     by remember { mutableStateOf(false) }
    var isPriority   by remember { mutableStateOf(false) }

    val message          by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
            if (it.startsWith("Announcement")) {
                title = ""; content = ""
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Broadcast Announcement", fontWeight = FontWeight.Bold, color = NavyPrimary) },
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
            // Info card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Announcements will be delivered to all selected users via push notifications and in-app feed.",
                        fontSize = 13.sp, color = TextSecondary
                    )
                }
            }

            // Form card
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("New Announcement", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title") },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = NavyPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = DividerGrey
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Message Content") },
                        leadingIcon = { Icon(Icons.Default.Message, contentDescription = null, tint = NavyPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = DividerGrey
                        ),
                        minLines = 4,
                        maxLines = 8
                    )

                    // Audience selector
                    Text("Target Audience", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AudienceChip("Everyone", selectedRole == null) { selectedRole = null }
                        AudienceChip("Students", selectedRole == UserRole.STUDENT) { selectedRole = UserRole.STUDENT }
                        AudienceChip("Lecturers", selectedRole == UserRole.LECTURER) { selectedRole = UserRole.LECTURER }
                    }

                    // Priority toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = if (isPriority) ErrorRed else TextHint, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Mark as Priority", fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 14.sp)
                            Text("Sends with urgent notification", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = isPriority,
                            onCheckedChange = { isPriority = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = ErrorRed)
                        )
                    }

                    // Char count
                    if (content.isNotBlank()) {
                        Text("${content.length} characters", fontSize = 11.sp, color = TextHint)
                    }

                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) return@Button
                            viewModel.postAnnouncement(
                                Announcement(
                                    title      = title.trim(),
                                    content    = content.trim(),
                                    targetRole = selectedRole
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Broadcast to ${selectedRole?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Everyone"}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudienceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NavyPrimary,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = NavyPrimary,
            borderColor = DividerGrey
        )
    )
}



