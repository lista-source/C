package com.example.campusflow.ui.screens.lecturer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.screens.student.CourseMaterial
import com.example.campusflow.ui.screens.student.InteractiveMaterialCard
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerMaterialsScreen(
    navController: NavController,
    courseId: String,
    repository: FirebaseRepository
) {
    var materials     by remember { mutableStateOf<List<String>>(emptyList()) }
    var urlInput      by remember { mutableStateOf("") }
    var materialTitle by remember { mutableStateOf("") }
    var isLoading     by remember { mutableStateOf(true) }
    var isUploading   by remember { mutableStateOf(false) }
    val context       = LocalContext.current
    val scope         = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(courseId) {
        isLoading = true
        materials = repository.getMaterials(courseId)
        isLoading = false
    }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            isUploading = true
            scope.launch {
                repository.uploadMaterialFile(courseId, it, context)
                    .onSuccess { url ->
                        materials = materials + url
                        snackbarHostState.showSnackbar("✓ File uploaded successfully!")
                    }
                    .onFailure { e -> snackbarHostState.showSnackbar("Upload failed: ${e.message}") }
                isUploading = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Course Materials", fontWeight = FontWeight.Bold, color = NavyPrimary)
                        Text(courseId, fontSize = 11.sp, color = TextSecondary)
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

            // Hero
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
                        Text("Upload Materials", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Course: $courseId · ${materials.size} files", color = Color.White.copy(0.7f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White.copy(0.7f), modifier = Modifier.size(32.dp))
                }
            }

            // Upload section
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add New Material", fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = materialTitle, onValueChange = { materialTitle = it },
                        label = { Text("Material Title / Description") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = NavyPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = urlInput, onValueChange = { urlInput = it },
                        label = { Text("Paste link (Drive, YouTube, etc.)") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = NavyPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Add URL
                        Button(
                            onClick = {
                                if (urlInput.isBlank()) return@Button
                                isUploading = true
                                scope.launch {
                                    repository.addMaterialLink(courseId, urlInput.trim(), materialTitle.trim())
                                        .onSuccess {
                                            materials = materials + urlInput.trim()
                                            snackbarHostState.showSnackbar("✓ Link added!")
                                            urlInput = ""; materialTitle = ""
                                        }
                                        .onFailure { e -> snackbarHostState.showSnackbar("Error: ${e.message}") }
                                    isUploading = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            enabled = urlInput.isNotBlank() && !isUploading
                        ) {
                            Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Link")
                        }

                        // Upload file
                        OutlinedButton(
                            onClick = { fileLauncher.launch("*/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isUploading,
                            border = BorderStroke(1.dp, NavyPrimary)
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = NavyPrimary)
                            } else {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                                Spacer(Modifier.width(4.dp))
                                Text("Upload File", color = NavyPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "${materials.size} file${if (materials.size != 1) "s" else ""} uploaded",
                modifier = Modifier.padding(horizontal = 20.dp),
                fontSize = 12.sp, color = TextSecondary
            )

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
                materials.isEmpty() -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = TextHint, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No materials yet", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Text("Add links or upload files above", fontSize = 13.sp, color = TextHint)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
                ) {
                    items(materials) { url ->
                        InteractiveMaterialCard(
                            material = CourseMaterial(url),
                            onOpen = { /* Opens automatically in student view */ }
                        )
                    }
                }
            }
        }
    }
}
