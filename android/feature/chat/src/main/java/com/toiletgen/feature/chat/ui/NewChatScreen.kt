package com.toiletgen.feature.chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.toiletgen.core.network.api.ChatApi
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onBack: () -> Unit,
    onStartChat: (userId: String, username: String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val chatApi: ChatApi = koinInject()
    val scope = rememberCoroutineScope()

    fun searchAndStart() {
        if (username.isBlank()) return
        isSearching = true
        errorMessage = null
        scope.launch {
            try {
                val user = chatApi.searchUser(username.trim())
                onStartChat(user.id, user.username)
            } catch (e: Exception) {
                errorMessage = if (e.message?.contains("404") == true) {
                    "Пользователь \"${username.trim()}\" не найден"
                } else {
                    "Пользователь не найден"
                }
            } finally {
                isSearching = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новое сообщение") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Введите имя пользователя, чтобы начать переписку",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = null
                },
                label = { Text("Имя пользователя") },
                singleLine = true,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { msg ->
                    { Text(msg, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(
                            onClick = { searchAndStart() },
                            enabled = username.isNotBlank(),
                        ) {
                            Icon(Icons.Default.Search, "Найти")
                        }
                    }
                },
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { searchAndStart() },
                enabled = username.isNotBlank() && !isSearching,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Найти и написать", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Вы также можете нажать на имя пользователя в общем чате, чтобы написать ему лично",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
