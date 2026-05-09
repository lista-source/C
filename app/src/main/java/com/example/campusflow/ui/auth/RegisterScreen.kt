package com.example.campusflow.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.campusflow.data.model.UserRole
import com.example.campusflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name         by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var department   by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var showPw       by remember { mutableStateOf(false) }

    val authState by viewModel.userState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess((authState as AuthState.Success).user.role.name)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyPrimary, NavyDark, Color(0xFF0A0E3D))))
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(TealAccent.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Back button row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(4.dp))
                Text("Back to Login", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Create Account",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Join CampusFlow today",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("University Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { showPw = !showPw }) {
                                Icon(if (showPw) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextHint)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                        visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department / Faculty") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = NavyPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                        singleLine = true
                    )

                    HorizontalDivider(color = DividerGrey)

                    Text("Account Role", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextSecondary)

                    // Role chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UserRole.entries.filter { it != UserRole.ADMIN }.forEach { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { selectedRole = role },
                                label = {
                                    Text(
                                        role.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = if (selectedRole == role) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedRole == role,
                                    selectedBorderColor = NavyPrimary,
                                    borderColor = DividerGrey
                                )
                            )
                        }
                    }

                    AnimatedVisibility(visible = authState is AuthState.Error) {
                        if (authState is AuthState.Error) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text((authState as AuthState.Error).message, color = ErrorRed, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    if (authState is AuthState.Loading) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NavyPrimary)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.register(name.trim(), email.trim(), password, selectedRole) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                        ) {
                            Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}


