package com.example.campusflow.ui.screens.lecturer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.model.AcademicResult
import com.example.campusflow.data.model.Attendance
import com.example.campusflow.data.model.TimetableEntry
import com.example.campusflow.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LecturerViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _schedule = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val schedule: StateFlow<List<TimetableEntry>> = _schedule

    private val _classResults = MutableStateFlow<List<AcademicResult>>(emptyList())
    val classResults: StateFlow<List<AcademicResult>> = _classResults

    private val _classAttendance = MutableStateFlow<List<Attendance>>(emptyList())
    val classAttendance: StateFlow<List<Attendance>> = _classAttendance

    private val _atRiskStudents = MutableStateFlow<List<String>>(emptyList())
    val atRiskStudents: StateFlow<List<String>> = _atRiskStudents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadSchedule(lecturerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _schedule.value = repository.getTimetableForLecturer(lecturerId)
            // After loading schedule, refresh at-risk by loading results for all taught courses
            val courseIds = _schedule.value.map { it.courseId }.distinct()
            val allResults = courseIds.flatMap { repository.getResultsByCourse(it) }
            _classResults.value = allResults
            detectLowPerformers(allResults)
            _isLoading.value = false
        }
    }

    /** Load all results for a specific course (when lecturer taps Students on a class card) */
    fun loadClassStudents(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _classResults.value = repository.getResultsByCourse(courseId)
            _classAttendance.value = repository.getAttendanceByCourse(courseId)
            detectLowPerformers(_classResults.value)
            _isLoading.value = false
        }
    }

    fun loadClassAnalytics(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _classResults.value = repository.getResultsByCourse(courseId)
            detectLowPerformers(_classResults.value)
            _isLoading.value = false
        }
    }

    /** Lecturer manually marks a student as present */
    fun markStudentAttendance(studentId: String, courseId: String, lecturerId: String) {
        viewModelScope.launch {
            val result = repository.markAttendanceManual(studentId, courseId, lecturerId)
            result.onSuccess {
                _message.value = "✓ Attendance marked for $studentId"
                // Refresh course attendance list
                _classAttendance.value = repository.getAttendanceByCourse(courseId)
            }.onFailure {
                _message.value = "Failed: ${it.message}"
            }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun detectLowPerformers(results: List<AcademicResult>) {
        _atRiskStudents.value = results
            .filter { it.totalScore < 40.0 }
            .map { it.studentId }
            .distinct()
    }
}

