package com.toiletgen.feature.toilet_details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Review
import com.toiletgen.core.domain.usecase.AddReviewUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AddReviewUiState(
    val rating: Int = 3,
    val cleanlinessSmell: Int = 3,
    val cleanlinessDirt: Int = 3,
    val hasToiletPaper: Boolean = true,
    val comment: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

class AddReviewViewModel(
    private val addReviewUseCase: AddReviewUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReviewUiState())
    val uiState: StateFlow<AddReviewUiState> = _uiState.asStateFlow()

    fun onRatingChange(rating: Int) { _uiState.update { it.copy(rating = rating) } }
    fun onSmellChange(value: Int) { _uiState.update { it.copy(cleanlinessSmell = value) } }
    fun onDirtChange(value: Int) { _uiState.update { it.copy(cleanlinessDirt = value) } }
    fun onToiletPaperChange(has: Boolean) { _uiState.update { it.copy(hasToiletPaper = has) } }
    fun onCommentChange(comment: String) { _uiState.update { it.copy(comment = comment) } }

    fun submit(toiletId: String) {
        val state = _uiState.value
        if (state.comment.isBlank()) {
            _uiState.update { it.copy(error = "Напишите комментарий") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val review = Review(
                id = "",
                toiletId = toiletId,
                userId = "",
                username = "",
                rating = state.rating,
                cleanlinessSmell = state.cleanlinessSmell,
                cleanlinessDirt = state.cleanlinessDirt,
                hasToiletPaper = state.hasToiletPaper,
                comment = state.comment,
                createdAt = System.currentTimeMillis(),
            )
            when (val result = addReviewUseCase(review)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}
