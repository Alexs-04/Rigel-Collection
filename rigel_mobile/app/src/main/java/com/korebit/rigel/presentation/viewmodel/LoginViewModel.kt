package com.korebit.rigel.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korebit.rigel.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun clearForm() {
        _uiState.value = LoginUiState()
    }

    fun login() {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "Por favor completa todos los campos")
            return
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = authRepository.login(currentState.email, currentState.password)

            result.onSuccess { response ->
                if (response.success) {
                    _uiState.value = LoginUiState(isLoggedIn = true)
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }

            result.onFailure { exception ->
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = exception.message ?: "Error al iniciar sesión"
                )
            }
        }
    }
}

