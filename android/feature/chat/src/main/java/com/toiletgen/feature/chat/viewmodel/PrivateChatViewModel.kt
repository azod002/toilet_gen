package com.toiletgen.feature.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.network.api.ChatApi
import com.toiletgen.core.network.model.PrivateMessageResponse
import com.toiletgen.core.network.model.SendMessageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PrivateChatUiState(
    val messages: List<PrivateMessageResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSending: Boolean = false,
)

class PrivateChatViewModel(
    private val chatApi: ChatApi,
    private val partnerId: String,
    private val partnerUsername: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrivateChatUiState())
    val uiState: StateFlow<PrivateChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
        startPolling()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val messages = chatApi.getPrivateMessages(partnerId)
                _uiState.value = _uiState.value.copy(isLoading = false, messages = messages)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(3000)
                try {
                    val lastTimestamp = _uiState.value.messages.lastOrNull()?.createdAt
                    val newMessages = chatApi.getPrivateMessages(partnerId, since = lastTimestamp)
                    if (newMessages.isNotEmpty()) {
                        val existing = _uiState.value.messages.map { it.id }.toSet()
                        val fresh = newMessages.filter { it.id !in existing }
                        if (fresh.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                messages = _uiState.value.messages + fresh,
                            )
                        }
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            try {
                val msg = chatApi.sendPrivateMessage(partnerId, partnerUsername, SendMessageRequest(text))
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    messages = _uiState.value.messages + msg,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false, error = e.message)
            }
        }
    }
}
