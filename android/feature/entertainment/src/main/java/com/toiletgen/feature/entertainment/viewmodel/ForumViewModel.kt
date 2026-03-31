package com.toiletgen.feature.entertainment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.network.api.ForumApi
import com.toiletgen.core.network.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForumUiState(
    val threads: List<ThreadResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val isSubmitting: Boolean = false,
)

class ForumViewModel(
    private val forumApi: ForumApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForumUiState())
    val uiState: StateFlow<ForumUiState> = _uiState.asStateFlow()

    init { loadThreads() }

    fun loadThreads() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val threads = forumApi.getThreads()
                _uiState.value = _uiState.value.copy(isLoading = false, threads = threads)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun showCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = true) }
    fun hideCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = false) }

    fun createThread(title: String, text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            try {
                forumApi.createThread(CreateThreadRequest(title, text))
                _uiState.value = _uiState.value.copy(isSubmitting = false, showCreateDialog = false)
                loadThreads()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = e.message)
            }
        }
    }
}
