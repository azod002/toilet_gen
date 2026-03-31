package com.toiletgen.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.usecase.LoginUseCase
import com.toiletgen.core.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val isLoginMode: Boolean = true,
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = loginUseCase(email, password)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = registerUseCase(username, email, password)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(isLoginMode = !_uiState.value.isLoginMode, error = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
