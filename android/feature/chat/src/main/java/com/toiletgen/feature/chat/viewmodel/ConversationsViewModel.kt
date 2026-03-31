package com.toiletgen.feature.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.network.api.ChatApi
import com.toiletgen.core.network.model.ConversationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConversationsUiState(
    val conversations: List<ConversationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ConversationsViewModel(
    private val chatApi: ChatApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val convs = chatApi.getConversations()
                _uiState.value = _uiState.value.copy(isLoading = false, conversations = convs)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
