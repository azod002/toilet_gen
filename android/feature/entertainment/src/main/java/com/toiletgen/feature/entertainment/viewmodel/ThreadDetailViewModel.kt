package com.toiletgen.feature.entertainment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.network.api.ForumApi
import com.toiletgen.core.network.model.CreateReplyRequest
import com.toiletgen.core.network.model.ReplyResponse
import com.toiletgen.core.network.model.ThreadResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ThreadDetailUiState(
    val thread: ThreadResponse? = null,
    val replies: List<ReplyResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSending: Boolean = false,
)

class ThreadDetailViewModel(
    private val forumApi: ForumApi,
    private val threadId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ThreadDetailUiState())
    val uiState: StateFlow<ThreadDetailUiState> = _uiState.asStateFlow()

    init { loadThread() }

    fun loadThread() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val detail = forumApi.getThread(threadId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    thread = detail.thread,
                    replies = detail.replies,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun sendReply(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            try {
                val reply = forumApi.createReply(threadId, CreateReplyRequest(text))
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    replies = _uiState.value.replies + reply,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false, error = e.message)
            }
        }
    }
}
