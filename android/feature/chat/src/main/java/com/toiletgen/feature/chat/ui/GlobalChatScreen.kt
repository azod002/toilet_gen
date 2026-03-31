package com.toiletgen.feature.chat.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.toiletgen.core.network.api.ReportsApi
import com.toiletgen.core.network.model.ChatMessageResponse
import com.toiletgen.core.network.model.CreateReportRequest
import com.toiletgen.core.ui.components.ReportDialog
import com.toiletgen.feature.chat.viewmodel.GlobalChatViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalChatScreen(
    onBack: () -> Unit,
    onUserClick: (userId: String, username: String) -> Unit,
    viewModel: GlobalChatViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var reportTarget by remember { mutableStateOf<ChatMessageResponse?>(null) }
    var isReporting by remember { mutableStateOf(false) }
    val reportsApi: ReportsApi = koinInject()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (reportTarget != null) {
        ReportDialog(
            onDismiss = { reportTarget = null },
            onSubmit = { reason ->
                isReporting = true
                scope.launch {
                    try {
                        reportsApi.createReport(CreateReportRequest("chat_message", reportTarget!!.id, reason))
                        reportTarget = null
                        snackbarHostState.showSnackbar("Жалоба отправлена")
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("Ошибка отправки жалобы")
                    } finally {
                        isReporting = false
                    }
                }
            },
            isLoading = isReporting,
        )
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Общий чат") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Сообщение...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        },
                        enabled = messageText.isNotBlank() && !uiState.isSending,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            "Отправить",
                            tint = if (messageText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (uiState.isLoading && uiState.messages.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(uiState.messages) { msg ->
                    GlobalChatBubble(
                        message = msg,
                        onUserClick = { onUserClick(msg.senderId, msg.senderUsername) },
                        onLongPress = { reportTarget = msg },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GlobalChatBubble(
    message: ChatMessageResponse,
    onUserClick: () -> Unit,
    onLongPress: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth().combinedClickable(
        onClick = {},
        onLongClick = onLongPress,
    )) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = onUserClick,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
            ) {
                Text(
                    message.senderUsername,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                message.text,
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
