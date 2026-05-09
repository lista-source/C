package com.example.campusflow.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.Announcement
import com.example.campusflow.data.model.UserRole
import com.example.campusflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val announcements by viewModel.announcements.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()

    var searchQuery    by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String>("All") }
    var expandedId     by remember { mutableStateOf<String?>(null) }
    var bookmarked     by remember { mutableStateOf(setOf<String>()) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) { viewModel.loadAnnouncements() }

    val filterTabs = listOf("All", "General", "Students", "Lecturers", "Urgent")

    val filtered = announcements.filter { ann ->
        val matchesSearch = searchQuery.isBlank() ||
            ann.title.contains(searchQuery, true) ||
            ann.content.contains(searchQuery, true)
        val matchesFilter = when (selectedFilter) {
            "Students"  -> ann.targetRole == UserRole.STUDENT
            "Lecturers" -> ann.targetRole == UserRole.LECTURER
            "General"   -> ann.targetRole == null
            "Urgent"    -> ann.title.contains("urgent", true) || ann.title.contains("important", true)
            else        -> true
        }
        matchesSearch && matchesFilter
    }.sortedByDescending { it.date }

    val unread = announcements.size
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Announcements", fontWeight = FontWeight.Bold, color = NavyPrimary)
                        if (unread > 0) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(TealAccent)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text("$unread", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAnnouncements() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGrey
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search announcements...", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = DividerGrey,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterTabs) { tab ->
                    FilterChip(
                        selected = selectedFilter == tab,
                        onClick = { selectedFilter = tab },
                        label = { Text(tab, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selectedFilter == tab,
                            selectedBorderColor = NavyPrimary, borderColor = DividerGrey
                        )
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Results count
            Text(
                "${filtered.size} announcement${if (filtered.size != 1) "s" else ""}",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                fontSize = 12.sp, color = TextSecondary
            )

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                }
                filtered.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = TextHint, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "No results for \"$searchQuery\"" else "No announcements yet",
                                fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 16.sp
                            )
                            Text("Check back later for updates", fontSize = 13.sp, color = TextHint)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
                    ) {
                        items(filtered, key = { it.id }) { ann ->
                            InteractiveAnnouncementCard(
                                announcement = ann,
                                dateFormat = dateFormat,
                                isExpanded = expandedId == ann.id,
                                isBookmarked = bookmarked.contains(ann.id),
                                onToggleExpand = {
                                    expandedId = if (expandedId == ann.id) null else ann.id
                                },
                                onToggleBookmark = {
                                    bookmarked = if (bookmarked.contains(ann.id))
                                        bookmarked - ann.id else bookmarked + ann.id
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveAnnouncementCard(
    announcement: Announcement,
    dateFormat: SimpleDateFormat,
    isExpanded: Boolean,
    isBookmarked: Boolean,
    onToggleExpand: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val isUrgent = announcement.title.contains("urgent", true) || announcement.title.contains("important", true)
    val accentColor = when {
        isUrgent                                   -> ErrorRed
        announcement.targetRole == UserRole.STUDENT -> NavyPrimary
        announcement.targetRole == UserRole.LECTURER -> TealAccent
        else                                        -> SkyBlue
    }
    val roleLabel = when (announcement.targetRole) {
        UserRole.STUDENT  -> "Students"
        UserRole.LECTURER -> "Lecturers"
        UserRole.ADMIN    -> "Admins"
        null              -> "Everyone"
    }
    val roleIcon = when (announcement.targetRole) {
        UserRole.STUDENT  -> Icons.Default.School
        UserRole.LECTURER -> Icons.Default.Person
        else              -> Icons.Default.Groups
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Accent top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.4f)))
                    )
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Icon badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isUrgent) Icons.Default.PriorityHigh else Icons.Default.Campaign,
                            contentDescription = null, tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            announcement.title,
                            fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            dateFormat.format(Date(announcement.date)),
                            fontSize = 11.sp, color = TextHint
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) NavyPrimary else TextHint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Preview / full content
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "content"
                ) { expanded ->
                    Text(
                        announcement.content,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        lineHeight = 20.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Target role chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(roleIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(roleLabel, fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (isUrgent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ErrorRed.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("URGENT", fontSize = 10.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (isBookmarked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyPrimary.copy(alpha = 0.08f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Saved", fontSize = 11.sp, color = NavyPrimary)
                            }
                        }
                    }
                    // Expand toggle
                    TextButton(
                        onClick = onToggleExpand,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            if (isExpanded) "Show less" else "Read more",
                            fontSize = 12.sp, color = NavyPrimary
                        )
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null, tint = NavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementItem(announcement: Announcement, dateFormat: SimpleDateFormat) {
    InteractiveAnnouncementCard(
        announcement = announcement,
        dateFormat = dateFormat,
        isExpanded = false,
        isBookmarked = false,
        onToggleExpand = {},
        onToggleBookmark = {}
    )
}




