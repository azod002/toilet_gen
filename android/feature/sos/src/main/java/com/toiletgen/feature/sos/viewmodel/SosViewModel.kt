package com.toiletgen.feature.sos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.SosRequest
import com.toiletgen.core.domain.model.SosStatus
import com.toiletgen.core.domain.usecase.RequestSosUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SosUiState(
    val isSearching: Boolean = false,
    val sosRequest: SosRequest? = null,
    val error: String? = null,
    val statusText: String = "",
)

class SosViewModel(
    private val requestSosUseCase: RequestSosUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SosUiState())
    val uiState: StateFlow<SosUiState> = _uiState.asStateFlow()

    fun requestSos(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = SosUiState(isSearching = true, statusText = "Ищем ближайший туалет...")
            when (val result = requestSosUseCase(lat, lon)) {
                is Resource.Success -> {
                    val request = result.data
                    val text = when (request.status) {
                        SosStatus.PENDING -> "Ожидаем ответ владельца..."
                        SosStatus.ACCEPTED -> "Найден! Строим маршрут..."
                        SosStatus.DECLINED -> "К сожалению, владелец отказал"
                        SosStatus.EXPIRED -> "Время ожидания истекло"
                    }
                    _uiState.value = SosUiState(isSearching = false, sosRequest = request, statusText = text)
                }
                is Resource.Error -> _uiState.value = SosUiState(isSearching = false, error = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun reset() {
        _uiState.value = SosUiState()
    }
}
