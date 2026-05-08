package com.example.campusflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusflow.data.model.User
import com.example.campusflow.data.model.UserRole
import com.example.campusflow.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<AuthState>(AuthState.Idle)
    val userState: StateFlow<AuthState> = _userState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _userState.value = AuthState.Loading
            val result = repository.login(email, pass)
            result.onSuccess {
                _userState.value = AuthState.Success(it)
            }.onFailure {
                _userState.value = AuthState.Error(it.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, pass: String, role: UserRole) {
        viewModelScope.launch {
            _userState.value = AuthState.Loading
            val result = repository.register(name, email, pass, role)
            result.onSuccess {
                _userState.value = AuthState.Success(it)
            }.onFailure {
                _userState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        repository.logout()
        _userState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}