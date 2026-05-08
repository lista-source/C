package com.example.campusflow.ui.screens.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.model.AcademicResult
import com.example.campusflow.data.model.Announcement
import com.example.campusflow.data.model.Attendance
import com.example.campusflow.data.model.TimetableEntry
import com.example.campusflow.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable

    private val _attendance = MutableStateFlow<List<Attendance>>(emptyList())
    val attendance: StateFlow<List<Attendance>> = _attendance

    private val _results = MutableStateFlow<List<AcademicResult>>(emptyList())
    val results: StateFlow<List<AcademicResult>> = _results

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _attendanceMessage = MutableStateFlow<String?>(null)
    val attendanceMessage: StateFlow<String?> = _attendanceMessage

    private var currentStudentId: String = ""

    fun initialize(studentId: String, department: String) {
        if (currentStudentId == studentId) return // Already loaded
        currentStudentId = studentId
        viewModelScope.launch {
            _isLoading.value = true
            val t = async { repository.getTimetableForStudent(department) }
            val a = async { repository.getStudentAttendance(studentId) }
            val r = async { repository.getResultsForStudent(studentId) }
            val n = async { repository.getAnnouncements() }
            _timetable.value = t.await()
            _attendance.value = a.await()
            _results.value = r.await()
            _announcements.value = n.await()
            _isLoading.value = false
        }
    }

    fun markAttendance(courseId: String, lat: Double, lon: Double) {
        if (currentStudentId.isBlank()) return
        viewModelScope.launch {
            val result = repository.verifyAndMarkAttendance(currentStudentId, courseId, lat, lon)
            _attendanceMessage.value = result.fold(
                onSuccess = {
                    repository.getStudentAttendance(currentStudentId).also {
                        _attendance.value = it
                    }
                    "Attendance marked successfully!"
                },
                onFailure = { it.message ?: "Failed to mark attendance" }
            )
        }
    }

    fun clearAttendanceMessage() {
        _attendanceMessage.value = null
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _announcements.value = repository.getAnnouncements()
        }
    }
}
