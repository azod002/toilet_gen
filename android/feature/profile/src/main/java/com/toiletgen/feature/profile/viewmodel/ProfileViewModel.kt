package com.toiletgen.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.User
import com.toiletgen.core.domain.repository.AuthRepository
import com.toiletgen.core.network.api.ReportApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Year

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val reviewsCount: Int = 0,
    val visitsCount: Int = 0,
    val toiletsCreated: Int = 0,
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val reportApi: ReportApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadStats()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, user = resource.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val report = reportApi.getYearlyReport(Year.now().value)
                _uiState.update {
                    it.copy(
                        reviewsCount = report.reviewsWritten,
                        visitsCount = report.toiletsVisited,
                        toiletsCreated = report.toiletsCreated,
                    )
                }
            } catch (_: Exception) {
                // Stats loading failed silently
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
