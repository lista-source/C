package com.example.campusflow.ui.screens.student

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.campusflow.data.repository.FirebaseRepository
import com.example.campusflow.ui.theme.*

// Shared data class used by both student (view) and lecturer (upload) versions
data class CourseMaterial(
    val url: String,
    val name: String = url.substringAfterLast("/").substringBefore("?").replace("%2F", "/").substringAfterLast("/").ifBlank { "Material" },
    val type: MaterialType = detectType(url),
    val uploadedAt: Long = System.currentTimeMillis()
)

enum class MaterialType { PDF, SLIDE, VIDEO, LINK, DOCUMENT, OTHER }

fun detectType(url: String): MaterialType {
    val lower = url.lowercase()
    return when {
        lower.contains(".pdf")  -> MaterialType.PDF
        lower.contains(".ppt") || lower.contains(".pptx") -> MaterialType.SLIDE
        lower.contains(".mp4") || lower.contains("youtube") || lower.contains("youtu.be") -> MaterialType.VIDEO
        lower.contains(".doc") || lower.contains(".docx") -> MaterialType.DOCUMENT
        else -> MaterialType.LINK
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    navController: NavController,
    repository: FirebaseRepository,
    courseId: String
) {
    var rawMaterials by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var searchQuery  by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(courseId) {
        isLoading = true
        rawMaterials = repository.getMaterials(courseId)
        isLoading = false
    }

    val materials = rawMaterials.map { CourseMaterial(it) }
    val typeTabs = listOf("All", "PDF", "Slides", "Videos", "Documents", "Links")

    val filtered = materials.filter { mat ->
        val matchSearch = searchQuery.isBlank() || mat.name.contains(searchQuery, true)
        val matchType = when (selectedType) {
            "PDF"       -> mat.type == MaterialType.PDF
            "Slides"    -> mat.type == MaterialType.SLIDE
            "Videos"    -> mat.type == MaterialType.VIDEO
            "Documents" -> mat.type == MaterialType.DOCUMENT
            "Links"     -> mat.type == MaterialType.LINK
            else        -> true
        }
        matchSearch && matchType
    }

    // Group by type for display
    val typeCounts = mapOf(
        "PDF"       to materials.count { it.type == MaterialType.PDF },
        "Slides"    to materials.count { it.type == MaterialType.SLIDE },
        "Videos"    to materials.count { it.type == MaterialType.VIDEO },
        "Documents" to materials.count { it.type == MaterialType.DOCUMENT }
    )

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
                actions = {
                    IconButton(onClick = {
                        isLoading = true
                        // Trigger refresh
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGrey
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Hero banner
            if (!isLoading && materials.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF00695C), TealAccent)))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Course: $courseId", color = Color.White.copy(0.7f), fontSize = 12.sp)
                            Text("${materials.size} Resources", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Tap any file to open or download", color = Color.White.copy(0.65f), fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            typeCounts.filter { it.value > 0 }.entries.take(2).forEach { (type, count) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$count", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(type, color = Color.White.copy(0.65f), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search materials...", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary, unfocusedBorderColor = DividerGrey,
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(8.dp))

            // Type filter tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(typeTabs) { tab ->
                    FilterChip(
                        selected = selectedType == tab,
                        onClick = { selectedType = tab },
                        label = { Text(tab, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00695C),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selectedType == tab,
                            selectedBorderColor = Color(0xFF00695C), borderColor = DividerGrey
                        )
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
                filtered.isEmpty() && materials.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = TextHint, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No materials uploaded yet", fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 16.sp)
                            Text("Course: $courseId", fontSize = 13.sp, color = TextHint)
                        }
                    }
                }
                filtered.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextHint, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No results for \"$searchQuery\"", color = TextSecondary)
                        }
                    }
                }
                else -> {
                    Text(
                        "${filtered.size} file${if (filtered.size != 1) "s" else ""}",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        fontSize = 12.sp, color = TextSecondary
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
                    ) {
                        items(filtered, key = { it.url }) { material ->
                            InteractiveMaterialCard(material = material) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(material.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // handled by snackbar
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveMaterialCard(material: CourseMaterial, onOpen: () -> Unit) {
    val (icon, color, label) = when (material.type) {
        MaterialType.PDF      -> Triple(Icons.Default.PictureAsPdf, ErrorRed, "PDF")
        MaterialType.SLIDE    -> Triple(Icons.Default.Slideshow, WarningAmber, "Slides")
        MaterialType.VIDEO    -> Triple(Icons.Default.PlayCircle, Color(0xFF7B1FA2), "Video")
        MaterialType.DOCUMENT -> Triple(Icons.Default.Description, InfoBlue, "Doc")
        MaterialType.LINK     -> Triple(Icons.Default.Link, TealAccent, "Link")
        MaterialType.OTHER    -> Triple(Icons.Default.AttachFile, TextSecondary, "File")
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    material.name,
                    fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp,
                    maxLines = 2
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(color.copy(alpha = 0.1f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
                    }
                    Text("Tap to open", fontSize = 11.sp, color = TextHint)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onOpen, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = NavyPrimary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onOpen, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Download, contentDescription = "Download", tint = TealAccent, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun MaterialItem(url: String, onOpen: () -> Unit) {
    InteractiveMaterialCard(material = CourseMaterial(url), onOpen = onOpen)
}

