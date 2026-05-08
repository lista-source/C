package com.example.campusflow.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnalyticsScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val results    by viewModel.results.collectAsState()
    val attendance by viewModel.attendance.collectAsState()

    val averageGpa     = if (results.isNotEmpty()) results.map { gpaFromScore(it.totalScore) }.average() else 0.0
    val attendanceRate = if (attendance.isNotEmpty()) attendance.count { it.isPresent }.toDouble() / attendance.size else 0.0
    val isAtRisk       = averageGpa < 2.0 || attendanceRate < 0.75

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Analytics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isAtRisk) {
                item { RiskAlertCard() }
            }

            item {
                AnalyticsSummaryCard(
                    title        = "Academic Standing",
                    primaryValue = "GPA: %.2f".format(averageGpa),
                    secondaryValue = when {
                        averageGpa >= 3.5 -> "Excellent"
                        averageGpa >= 2.0 -> "Good Standing"
                        else              -> "Academic Probation"
                    },
                    color = if (averageGpa >= 2.0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                )
            }

            item {
                AnalyticsSummaryCard(
                    title          = "Attendance Overview",
                    primaryValue   = "${(attendanceRate * 100).toInt()}%",
                    secondaryValue = "${attendance.count { it.isPresent }} / ${attendance.size} Classes",
                    color = if (attendanceRate >= 0.75) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.error
                )
            }

            item { Text("Performance Insights", style = MaterialTheme.typography.titleLarge) }

            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isAtRisk)
                                "Action Required: Your performance is below the academic threshold. " +
                                "Please schedule a meeting with your academic advisor."
                            else
                                "Keep it up! You are maintaining solid academic progress this semester.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiskAlertCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
