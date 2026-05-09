package com.example.campusflow.ui.screens.student

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.campusflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnalyticsScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val results    by viewModel.results.collectAsState()
    val attendance by viewModel.attendance.collectAsState()

    val averageGpa     = if (results.isNotEmpty()) results.map { gpaFromScore(it.totalScore) }.average() else 3.7
    val attendanceRate = if (attendance.isNotEmpty()) attendance.count { it.isPresent }.toDouble() / attendance.size else 0.85
    val isAtRisk       = averageGpa < 2.0 || attendanceRate < 0.75

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Analytics", fontWeight = FontWeight.Bold, color = NavyPrimary) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (isAtRisk) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Academic Risk Alert", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 15.sp)
                                Text(
                                    if (averageGpa < 2.0) "Your GPA is below 2.0 — academic probation risk."
                                    else "Your attendance is below 75% — exam eligibility at risk.",
                                    fontSize = 13.sp, color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Overview hero
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = if (isAtRisk) 4.dp else 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(NavyPrimary, CornflowerBlue)))
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnalyticsHeroStat("GPA", "%.2f".format(averageGpa), when {
                            averageGpa >= 3.5 -> "Excellent"
                            averageGpa >= 2.0 -> "Good"
                            else -> "At Risk"
                        })
                        AnalyticsHeroStat("Attendance", "${(attendanceRate * 100).toInt()}%", when {
                            attendanceRate >= 0.9 -> "Excellent"
                            attendanceRate >= 0.75 -> "Good"
                            else -> "Low"
                        })
                        AnalyticsHeroStat("Courses", "${results.size.coerceAtLeast(4)}", "Enrolled")
                    }
                }
            }

            // GPA breakdown card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).shadow(3.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Academic Standing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        }
                        Spacer(Modifier.height(16.dp))
                        GpaScaleBar(averageGpa)
                        Spacer(Modifier.height(12.dp))
                        val standing = when {
                            averageGpa >= 3.5 -> "First Class Honours 🎓"
                            averageGpa >= 3.0 -> "Second Class Upper"
                            averageGpa >= 2.0 -> "Second Class Lower"
                            else -> "Academic Probation ⚠"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyPrimary.copy(alpha = 0.06f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Standing", fontSize = 13.sp, color = TextSecondary)
                            Text(standing, fontWeight = FontWeight.SemiBold, color = NavyPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Attendance breakdown card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).shadow(3.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventAvailable, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Attendance Overview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        }
                        Spacer(Modifier.height(16.dp))

                        val present = attendance.count { it.isPresent }
                        val total = attendance.size.coerceAtLeast(20)
                        val rate = (attendanceRate * 100).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AttendanceStat("Present", "$present", SuccessGreen)
                            AttendanceStat("Absent", "${total - present}", ErrorRed)
                            AttendanceStat("Total", "$total", NavyPrimary)
                            AttendanceStat("Rate", "$rate%", if (rate >= 75) TealAccent else WarningAmber)
                        }

                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { attendanceRate.toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (attendanceRate >= 0.75) TealAccent else ErrorRed,
                            trackColor = DividerGrey
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (attendanceRate >= 0.75) "✓ Meeting minimum attendance threshold (75%)"
                            else "⚠ Below minimum 75% — risk of exam ban",
                            fontSize = 12.sp,
                            color = if (attendanceRate >= 0.75) SuccessGreen else ErrorRed
                        )
                    }
                }
            }

            // Insights card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).shadow(3.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Personalized Insights", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        }
                        Spacer(Modifier.height(12.dp))
                        val insights = buildInsights(averageGpa, attendanceRate)
                        insights.forEach { insight ->
                            Row(modifier = Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                                Text("•", color = NavyPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 1.dp, end = 8.dp))
                                Text(insight, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeroStat(label: String, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        Text(sub, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
private fun AttendanceStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun GpaScaleBar(gpa: Double) {
    val fraction = (gpa / 4.0).toFloat().coerceIn(0f, 1f)
    val color = when {
        gpa >= 3.5 -> SuccessGreen
        gpa >= 2.0 -> TealAccent
        else -> ErrorRed
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0.0", fontSize = 10.sp, color = TextHint)
            Text("GPA: %.2f / 4.0".format(gpa), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
            Text("4.0", fontSize = 10.sp, color = TextHint)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(DividerGrey)
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction).clip(RoundedCornerShape(5.dp)).background(color)
            )
        }
    }
}

private fun buildInsights(gpa: Double, attendance: Double): List<String> {
    val list = mutableListOf<String>()
    if (gpa >= 3.5) list.add("Excellent academic performance — you're on track for First Class Honours.")
    else if (gpa >= 3.0) list.add("Strong GPA. Aim for 3.5+ to achieve First Class Honours.")
    else if (gpa >= 2.0) list.add("Your GPA is in Good Standing, but there's room to improve.")
    else list.add("Your GPA is at risk. Meet your academic advisor immediately.")

    if (attendance >= 0.9) list.add("Outstanding attendance rate — keep it up!")
    else if (attendance >= 0.75) list.add("Attendance is within the required threshold. Try to exceed 90%.")
    else list.add("Critical: Attendance is below 75%. You may be barred from exams.")

    list.add("Focus on improving your weakest subjects first for maximum GPA impact.")
    return list
}

@Composable
fun RiskAlertCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Academic Risk Alert", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Text("Low GPA or attendance detected.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@Composable
fun AnalyticsSummaryCard(title: String, primaryValue: String, secondaryValue: String, color: Color) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = primaryValue, style = MaterialTheme.typography.headlineLarge, color = color)
                Spacer(Modifier.width(8.dp))
                Text(text = secondaryValue, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}
