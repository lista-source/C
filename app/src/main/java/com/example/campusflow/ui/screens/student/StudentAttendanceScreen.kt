package com.example.campusflow.ui.screens.student

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campusflow.data.model.Attendance
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val attendanceList by viewModel.attendance.collectAsState()
    val attendanceMessage by viewModel.attendanceMessage.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var courseIdInput by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    viewModel.markAttendance(courseIdInput, location.latitude, location.longitude)
                }
            }
        }
    }

    LaunchedEffect(attendanceMessage) {
        attendanceMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearAttendanceMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Attendance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Mark Attendance Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Mark Today's Attendance", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = courseIdInput,
                        onValueChange = { courseIdInput = it },
                        label = { Text("Course Code (e.g. CS101)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (courseIdInput.isBlank()) return@Button
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                                fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                                    if (location != null) {
                                        viewModel.markAttendance(courseIdInput, location.latitude, location.longitude)
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = courseIdInput.isNotBlank()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark My Attendance (GPS)")
                    }
                }
            }

            // Summary
            val present = attendanceList.count { it.isPresent }
            val total = attendanceList.size
            val rate = if (total == 0) 0 else present * 100 / total

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (rate >= 75) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Overall Attendance", style = MaterialTheme.typography.titleMedium)
                        Text("$present of $total classes attended", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("$rate%", style = MaterialTheme.typography.headlineLarge,
                        color = if (rate >= 75) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
            }

            Text("Attendance History", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(attendanceList) { record ->
                    AttendanceItem(record, dateFormat)
                }
            }

            if (attendanceList.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No attendance records yet.")
                }
            }
        }
    }
}

@Composable
fun AttendanceItem(attendance: Attendance, dateFormat: SimpleDateFormat) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Course: ${attendance.courseId}", style = MaterialTheme.typography.bodyLarge)
                Text(text = dateFormat.format(Date(attendance.date)), style = MaterialTheme.typography.bodySmall)
                if (attendance.verified) {
                    Text(text = "GPS Verified ✓", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Badge(
                containerColor = if (attendance.isPresent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            ) {
                Text(if (attendance.isPresent) "Present" else "Absent")
            }
        }
    }
}
