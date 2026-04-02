package com.toiletgen.feature.stamps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.StampTrade
import com.toiletgen.core.domain.model.UserStamp
import com.toiletgen.core.domain.repository.StampRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StampsUiState(
    val stamps: List<UserStamp> = emptyList(),
    val trades: List<StampTrade> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

class StampsViewModel(private val repository: StampRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(StampsUiState())
    val uiState: StateFlow<StampsUiState> = _uiState.asStateFlow()

    init {
        loadStamps()
        loadTrades()
    }

    fun loadStamps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getMyStamps()) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, stamps = result.data, error = null) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadTrades() {
        viewModelScope.launch {
            when (val result = repository.getMyTrades()) {
                is Resource.Success -> _uiState.update { it.copy(trades = result.data) }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun acceptTrade(tradeId: String) {
        viewModelScope.launch {
            when (val result = repository.acceptTrade(tradeId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(message = "Обмен принят!") }
                    loadStamps()
                    loadTrades()
                }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun declineTrade(tradeId: String) {
        viewModelScope.launch {
            when (repository.declineTrade(tradeId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(message = "Обмен отклонён") }
                    loadTrades()
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
