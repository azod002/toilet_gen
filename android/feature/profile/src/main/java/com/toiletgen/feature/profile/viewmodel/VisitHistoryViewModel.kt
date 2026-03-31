package com.toiletgen.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.network.api.ToiletApi
import com.toiletgen.core.network.model.VisitResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VisitHistoryUiState(
    val visits: List<VisitResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class VisitHistoryViewModel(
    private val toiletApi: ToiletApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitHistoryUiState())
    val uiState: StateFlow<VisitHistoryUiState> = _uiState.asStateFlow()

    init { loadVisits() }

    private fun loadVisits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val visits = toiletApi.getMyVisits()
                _uiState.update { it.copy(isLoading = false, visits = visits) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки") }
            }
        }
    }
}
