package com.example.campusflow.ui.screens.student

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
import com.example.campusflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentResultsScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val results   by viewModel.results.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val averageGpa = if (results.isNotEmpty())
        results.map { gpaFromScore(it.totalScore) }.average() else 0.0

    val standing = when {
        averageGpa >= 3.5 -> "First Class Honours"
        averageGpa >= 3.0 -> "Second Class Upper"
        averageGpa >= 2.0 -> "Second Class Lower"
        averageGpa > 0.0  -> "Pass"
        else              -> "—"
    }
    val standingColor = when {
        averageGpa >= 3.5 -> SuccessGreen
        averageGpa >= 2.0 -> TealAccent
        averageGpa > 0.0  -> WarningAmber
        else              -> TextHint
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Results", fontWeight = FontWeight.Bold, color = NavyPrimary) },
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
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // GPA Hero card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.horizontalGradient(listOf(NavyPrimary, SkyBlue)))
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Semester GPA", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                                Text("%.2f".format(averageGpa), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(standing, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                GpaStat("Courses", "${results.size.coerceAtLeast(4)}")
                                Spacer(Modifier.height(12.dp))
                                GpaStat("Credits", "${results.size.coerceAtLeast(4) * 3}")
                            }
                        }
                    }
                }

                // Grade distribution bar
                if (results.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).shadow(3.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Grade Distribution", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))
                                val gradeGroups = results.groupBy { it.grade }
                                listOf("A", "B", "C", "D", "F").forEach { grade ->
                                    val count = gradeGroups[grade]?.size ?: 0
                                    if (count > 0 || results.isEmpty()) {
                                        GradeBarRow(grade = grade, count = count, total = results.size)
                                        Spacer(Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Course Results", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("${results.size} courses", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                if (results.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = TextHint, modifier = Modifier.size(52.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("No results published yet.", color = TextHint, fontSize = 15.sp)
                            }
                        }
                    }
                } else {
                    items(results, key = { it.id }) { result ->
                        EnhancedResultItem(result, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GpaStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun GradeBarRow(grade: String, count: Int, total: Int) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    val color = when (grade) {
        "A" -> SuccessGreen
        "B" -> TealAccent
        "C" -> InfoBlue
        "D" -> WarningAmber
        else -> ErrorRed
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(grade, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.width(20.dp), fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(DividerGrey)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("$count", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.width(20.dp))
    }
}

@Composable
fun EnhancedResultItem(result: AcademicResult, modifier: Modifier = Modifier) {
    val gradeColor = when (result.grade) {
        "A" -> SuccessGreen
        "B" -> TealAccent
        "C" -> InfoBlue
        "D" -> WarningAmber
        else -> ErrorRed
    }
    Card(
        modifier = modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(result.courseName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                    Text(result.courseId, fontSize = 12.sp, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(result.grade, color = gradeColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            // Score breakdown bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreChip("CAT", result.catScore, 30.0)
                ScoreChip("Exam", result.examScore, 70.0)
                ScoreChip("Total", result.totalScore, 100.0)
            }
            Spacer(Modifier.height(8.dp))
            // Progress bar
            LinearProgressIndicator(
                progress = { (result.totalScore / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = gradeColor,
                trackColor = DividerGrey
            )
        }
    }
}

@Composable
private fun RowScope.ScoreChip(label: String, score: Double, max: Double) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundGrey)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("${score.toInt()}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
fun ResultItem(result: AcademicResult) {
    EnhancedResultItem(result)
}

@Composable
private fun gradeColor(grade: String) = when (grade) {
    "A" -> SuccessGreen
    "B" -> TealAccent
    "F" -> ErrorRed
    else -> TextSecondary
}
