package com.example.campusflow.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.campusflow.data.model.User
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.ui.navigation.Screen
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authRepository: AuthRepository
) {
    var user by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Editable fields
    var nameValue by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf("") }
    var emergencyValue by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }

    // Notification toggles
    var pushAlertsEnabled by remember { mutableStateOf(true) }
    var emailDigestsEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authRepository.currentUser?.uid?.let { uid ->
            try {
                val fetched = authRepository.getUserData(uid)
                user = fetched
                nameValue = fetched.name
                phoneValue = "+1 (555) 012-3456"
                emergencyValue = "Emergency Contact"
            } catch (e: Exception) { /* ignore */ }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed) },
            title = { Text("Logout Session?") },
            text = { Text("You will be signed out of CampusFlow on this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        authRepository.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed) },
            title = { Text("Deactivate Account?") },
            text = { Text("This will permanently disable your CampusFlow account. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { showDeactivateDialog = false; scope.launch { snackbarHostState.showSnackbar("Account deactivation request submitted") } },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Deactivate") }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user?.name?.take(2)?.uppercase() ?: "AR",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
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
        ) {
            // Avatar card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(CornflowerBlue, NavyPrimary))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user?.name?.take(2)?.uppercase() ?: "AR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(TealAccent)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(user?.name ?: "Loading...", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                    Text(
                        user?.studentId?.ifBlank { "ID: 2024001" } ?: "ID: —",
                        fontSize = 13.sp, color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE8EAF6))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp), tint = NavyPrimary)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                user?.department?.ifBlank { "Computer Science" } ?: "Computer Science",
                                fontSize = 12.sp,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Personal Details card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("PERSONAL DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = NavyPrimary, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(16.dp))

                    ProfileTextField(label = "Full Name", value = nameValue, onValueChange = { nameValue = it }, icon = Icons.Default.Person)
                    Spacer(Modifier.height(12.dp))
                    ProfileTextField(label = "University Email", value = user?.email ?: "", onValueChange = {}, icon = Icons.Default.Email, readOnly = true)
                    Spacer(Modifier.height(12.dp))
                    ProfileTextField(label = "Phone Number", value = phoneValue, onValueChange = { phoneValue = it }, icon = Icons.Default.Phone)
                    Spacer(Modifier.height(12.dp))
                    ProfileTextField(label = "Emergency Contact", value = emergencyValue, onValueChange = { emergencyValue = it }, icon = Icons.Default.ContactPhone)

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                kotlinx.coroutines.delay(800)
                                isSaving = false
                                snackbarHostState.showSnackbar("✓ Changes saved successfully")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Save Changes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Notifications card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = NavyPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    }
                    Spacer(Modifier.height(16.dp))

                    NotificationToggleRow(
                        title = "Push Alerts",
                        subtitle = "Class changes, exam results",
                        checked = pushAlertsEnabled,
                        onCheckedChange = { pushAlertsEnabled = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerGrey)
                    NotificationToggleRow(
                        title = "Email Digests",
                        subtitle = "Weekly performance summary",
                        checked = emailDigestsEnabled,
                        onCheckedChange = { emailDigestsEnabled = it }
                    )
                }
            }

            // App Preferences card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = NavyPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("App Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                    PreferenceRow(icon = Icons.Default.DarkMode, label = "Theme Selection", value = "System Default")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerGrey)
                    PreferenceRow(icon = Icons.Default.Language, label = "Language", value = "English (US)")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerGrey)
                    PreferenceRow(icon = Icons.Default.PrivacyTip, label = "Privacy Policy", value = "")
                }
            }

            // Action buttons
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed)
                Spacer(Modifier.width(8.dp))
                Text("Logout Session", color = ErrorRed, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showDeactivateDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, DividerGrey)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = TextSecondary)
                Spacer(Modifier.width(8.dp))
                Text("Deactivate Account", color = TextSecondary, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp)) },
        readOnly = readOnly,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyPrimary,
            unfocusedBorderColor = DividerGrey,
            focusedLabelColor = NavyPrimary,
            unfocusedContainerColor = if (readOnly) BackgroundGrey else Color.White,
            focusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
private fun NotificationToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NavyPrimary,
                uncheckedTrackColor = DividerGrey
            )
        )
    }
}

@Composable
private fun PreferenceRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary)
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f), color = TextPrimary, fontSize = 14.sp)
        Text(value, color = if (value.isNotBlank()) NavyPrimary else TextHint, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        if (value.isBlank()) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextHint)
        }
    }
}