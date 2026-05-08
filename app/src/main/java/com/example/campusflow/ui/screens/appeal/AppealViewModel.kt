package com.example.campusflow.ui.screens.appeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.model.AcademicResult
import com.example.campusflow.data.model.AppealStatus
import com.example.campusflow.data.model.GradeAppeal
import com.example.campusflow.data.repository.AppealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppealViewModel @Inject constructor(
    private val appealRepository: AppealRepository
) : ViewModel() {

    private val _appeals = MutableStateFlow<List<GradeAppeal>>(emptyList())
    val appeals: StateFlow<List<GradeAppeal>> = _appeals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // ── Student ──────────────────────────────────────────────

    fun loadStudentAppeals(studentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { appealRepository.getAppealsForStudent(studentId) }
                .onSuccess { _appeals.value = it }
                .onFailure { _message.value = "Failed to load appeals: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun submitAppeal(
        studentId: String,
        studentName: String,
        result: AcademicResult,
        reason: String,
        onSuccess: () -> Unit
    ) {
        if (reason.isBlank()) {
            _message.value = "Please enter a reason for your appeal"
            return
        }
        if (reason.length < 20) {
            _message.value = "Please provide more detail (at least 20 characters)"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            // Guard: prevent duplicate appeals
            val alreadyAppealed = runCatching {
                appealRepository.hasExistingAppeal(studentId, result.id)
            }.getOrDefault(false)

            if (alreadyAppealed) {
                _message.value = "You have already submitted an appeal for this result"
                _isLoading.value = false
                return@launch
            }

            val appeal = GradeAppeal(
                studentId = studentId,
                studentName = studentName,
                courseId = result.courseId,
                courseName = result.courseName,
                resultId = result.id,
                reason = reason
            )

            appealRepository.submitAppeal(appeal)
                .onSuccess {
                    _message.value = "Appeal submitted successfully"
                    loadStudentAppeals(studentId)
                    onSuccess()
                }
                .onFailure { _message.value = "Failed to submit appeal: ${it.message}" }

            _isLoading.value = false
        }
    }

    // ── Lecturer ─────────────────────────────────────────────

    fun loadAppealsForCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { appealRepository.getAppealsForCourse(courseId) }
                .onSuccess { _appeals.value = it }
                .onFailure { _message.value = "Failed to load appeals: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun loadPendingAppealsForLecturer(courseIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { appealRepository.getPendingAppealsForLecturer(courseIds) }
                .onSuccess { _appeals.value = it }
                .onFailure { _message.value = "Failed to load appeals: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun respondToAppeal(
        appealId: String,
        status: AppealStatus,
        comment: String,
        courseId: String
    ) {
        if (comment.isBlank()) {
            _message.value = "Please enter a comment for the student"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            appealRepository.respondToAppeal(appealId, status, comment)
                .onSuccess {
                    _message.value = if (status == AppealStatus.RESOLVED) "Appeal resolved" else "Appeal rejected"
                    loadAppealsForCourse(courseId)
                }
                .onFailure { _message.value = "Failed to respond: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
}