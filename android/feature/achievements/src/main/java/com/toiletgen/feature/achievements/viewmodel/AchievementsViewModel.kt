package com.toiletgen.feature.achievements.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Achievement
import com.toiletgen.core.domain.usecase.GetAchievementsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AchievementsUiState(val achievements: List<Achievement> = emptyList(), val isLoading: Boolean = false, val error: String? = null)

class AchievementsViewModel(private val getAchievementsUseCase: GetAchievementsUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()
    init { loadAchievements() }
    private fun loadAchievements() {
        viewModelScope.launch {
            getAchievementsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, achievements = resource.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }
}
