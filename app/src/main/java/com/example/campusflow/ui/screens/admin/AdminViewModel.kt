package com.example.campusflow.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.model.Announcement
import com.example.campusflow.data.model.User
import com.example.campusflow.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _users     = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message   = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _users.value     = repository.getAllUsers()
            _isLoading.value = false
        }
    }

    fun postAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.postAnnouncement(announcement)
                .onSuccess { _message.value = "Announcement posted successfully!" }
                .onFailure { _message.value = "Error: ${it.message}" }
        }
    }

    fun clearMessage() { _message.value = null }
}
