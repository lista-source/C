package com.example.campusflow.ui.screens.lecturer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.model.AcademicResult
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

    private val _atRiskStudents = MutableStateFlow<List<String>>(emptyList())
    val atRiskStudents: StateFlow<List<String>> = _atRiskStudents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadSchedule(lecturerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _schedule.value = repository.getTimetableForLecturer(lecturerId)
            _isLoading.value = false
        }
    }

    fun loadClassAnalytics(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _classResults.value = repository.getResultsByCourse(courseId)
            detectLowPerformers()
            _isLoading.value = false
        }
    }

    private fun detectLowPerformers() {
        _atRiskStudents.value = _classResults.value
            .filter { it.totalScore < 40.0 }
            .map { it.studentId }
            .distinct()
    }
}
