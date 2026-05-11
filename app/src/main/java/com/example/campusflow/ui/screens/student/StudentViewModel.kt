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

    /**
     * @param studentId     Firebase UID (used for attendance lookup)
     * @param department    Used for timetable lookup
     * @param generatedId   The STU-YYYY-XXXXX id stored in results by lecturers (optional)
     */
    fun initialize(studentId: String, department: String, generatedId: String = "") {
        if (currentStudentId == studentId) return
        currentStudentId = studentId
        viewModelScope.launch {
            _isLoading.value = true
            val t = async { repository.getTimetableForStudent(department) }
            val a = async { repository.getStudentAttendance(studentId) }
            // Fetch results by generatedId first; fall back to uid if empty
            val r = async {
                val byGenId = if (generatedId.isNotBlank())
                    repository.getResultsForStudent(generatedId) else emptyList()
                if (byGenId.isNotEmpty()) byGenId
                else repository.getResultsForStudent(studentId)
            }
            val n = async { repository.getAnnouncements() }
            _timetable.value      = t.await()
            _attendance.value     = a.await()
            _results.value        = r.await()
            _announcements.value  = n.await()
            _isLoading.value      = false
        }
    }

    fun markAttendance(courseId: String, lat: Double, lon: Double) {
        if (currentStudentId.isBlank()) return
        viewModelScope.launch {
            val result = repository.verifyAndMarkAttendance(currentStudentId, courseId, lat, lon)
            _attendanceMessage.value = result.fold(
                onSuccess = {
                    _attendance.value = repository.getStudentAttendance(currentStudentId)
                    "✓ Attendance marked successfully!"
                },
                onFailure = { it.message ?: "Failed to mark attendance" }
            )
        }
    }

    fun clearAttendanceMessage() { _attendanceMessage.value = null }

    fun loadAnnouncements() {
        viewModelScope.launch { _announcements.value = repository.getAnnouncements() }
    }
}


