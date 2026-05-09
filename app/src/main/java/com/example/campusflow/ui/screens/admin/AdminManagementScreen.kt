package com.example.campusflow.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.User
import com.example.campusflow.data.model.UserRole
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementScreen(
    navController: NavController,
    repository: FirebaseRepository,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val users     by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var expandedUser by remember { mutableStateOf<String?>(null) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadUsers() }

    val filtered = users.filter {
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)) &&
                (selectedRole == null || it.role == selectedRole)
    }

    if (showDeleteDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove User?") },
            text = { Text("Remove ${selectedUser?.name} from CampusFlow? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch { snackbarHostState.showSnackbar("User removed successfully") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manage Users", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add", tint = NavyPrimary)
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
        ) {
            // Search bar
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by name, email or role") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = DividerGrey
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    // Role filter chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoleFilterChip(label = "All", selected = selectedRole == null) { selectedRole = null }
                        RoleFilterChip(label = "Students", selected = selectedRole == UserRole.STUDENT) { selectedRole = if (selectedRole == UserRole.STUDENT) null else UserRole.STUDENT }
                        RoleFilterChip(label = "Lecturers", selected = selectedRole == UserRole.LECTURER) { selectedRole = if (selectedRole == UserRole.LECTURER) null else UserRole.LECTURER }
                        RoleFilterChip(label = "Admins", selected = selectedRole == UserRole.ADMIN) { selectedRole = if (selectedRole == UserRole.ADMIN) null else UserRole.ADMIN }
                    }
                }
            }

            Text(
                "${filtered.size} user${if (filtered.size != 1) "s" else ""} found",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(4.dp))

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
                filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextHint, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No users found.", color = TextHint)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filtered, key = { it.uid }) { user ->
                        ExpandableUserItem(
                            user = user,
                            isExpanded = expandedUser == user.uid,
                            onToggle = { expandedUser = if (expandedUser == user.uid) null else user.uid },
                            onDelete = { selectedUser = user; showDeleteDialog = true },
                            onRoleChange = { scope.launch { snackbarHostState.showSnackbar("Role updated for ${user.name}") } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandableUserItem(
    user: User,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onRoleChange: () -> Unit
) {
    val roleColor = when (user.role) {
        UserRole.STUDENT -> NavyPrimary
        UserRole.LECTURER -> Color(0xFF00695C)
        UserRole.ADMIN -> Color(0xFF6A1B9A)
    }
    val roleIcon = when (user.role) {
        UserRole.STUDENT -> Icons.Default.School
        UserRole.LECTURER -> Icons.Default.Person
        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.name.take(1).uppercase(),
                        color = roleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(user.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                    Text(user.email, fontSize = 12.sp, color = TextSecondary)
                    if (user.department.isNotBlank()) {
                        Text(user.department, fontSize = 11.sp, color = NavyPrimary.copy(alpha = 0.7f))
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(roleColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(user.role.name, color = roleColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(4.dp))
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = TextHint
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundGrey)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("Actions", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onRoleChange,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, NavyPrimary)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                            Spacer(Modifier.width(4.dp))
                            Text("Change Role", color = NavyPrimary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = onDelete,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                            Spacer(Modifier.width(4.dp))
                            Text("Remove", color = ErrorRed, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name,  style = MaterialTheme.typography.titleMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodySmall)
                if (user.department.isNotBlank()) {
                    Text(text = user.department, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                Text(user.role.name, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
