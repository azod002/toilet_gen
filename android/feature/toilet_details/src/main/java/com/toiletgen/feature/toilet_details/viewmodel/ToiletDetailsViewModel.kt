package com.toiletgen.feature.toilet_details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Review
import com.toiletgen.core.domain.model.Toilet
import com.toiletgen.core.domain.repository.AuthRepository
import com.toiletgen.core.domain.usecase.AddReviewUseCase
import com.toiletgen.core.domain.usecase.GetReviewsUseCase
import com.toiletgen.core.domain.usecase.GetToiletDetailsUseCase
import com.toiletgen.core.network.api.StampApi
import com.toiletgen.core.network.api.ToiletApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ToiletDetailsUiState(
    val toilet: Toilet? = null,
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val reviewSubmitted: Boolean = false,
    val currentUserId: String? = null,
    val currentUserRole: String = "user",
    val deleted: Boolean = false,
    val stampMessage: String? = null,
    val stampCollecting: Boolean = false,
)

class ToiletDetailsViewModel(
    private val getToiletDetailsUseCase: GetToiletDetailsUseCase,
    private val getReviewsUseCase: GetReviewsUseCase,
    private val addReviewUseCase: AddReviewUseCase,
    private val authRepository: AuthRepository,
    private val toiletApi: ToiletApi,
    private val stampApi: StampApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToiletDetailsUiState())
    val uiState: StateFlow<ToiletDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(currentUserId = resource.data.id, currentUserRole = resource.data.role) }
                }
            }
        }
    }

    fun loadToilet(toiletId: String) {
        viewModelScope.launch {
            getToiletDetailsUseCase(toiletId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, toilet = resource.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
        viewModelScope.launch {
            getReviewsUseCase(toiletId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(reviews = resource.data) }
                }
            }
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            when (val result = addReviewUseCase(review)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(reviewSubmitted = true) }
                    loadToilet(review.toiletId)
                }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun collectStamp() {
        val toiletId = _uiState.value.toilet?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(stampCollecting = true) }
            try {
                stampApi.collectStamp(toiletId)
                _uiState.update { it.copy(stampCollecting = false, stampMessage = "Марка получена!") }
            } catch (e: Exception) {
                val msg = e.message?.let {
                    if (it.contains("марку можно получить")) it
                    else "Ошибка: $it"
                } ?: "Ошибка получения марки"
                _uiState.update { it.copy(stampCollecting = false, stampMessage = msg) }
            }
        }
    }

    fun clearStampMessage() {
        _uiState.update { it.copy(stampMessage = null) }
    }

    fun deleteToilet() {
        val toiletId = _uiState.value.toilet?.id ?: return
        viewModelScope.launch {
            try {
                toiletApi.deleteToilet(toiletId)
                _uiState.update { it.copy(deleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Ошибка удаления") }
            }
        }
    }
}
