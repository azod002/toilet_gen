package com.toiletgen.feature.entertainment.ui

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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.toiletgen.core.network.api.ReportsApi
import com.toiletgen.core.network.model.CreateReportRequest
import com.toiletgen.core.network.model.ReplyResponse
import com.toiletgen.core.ui.components.ReportDialog
import com.toiletgen.feature.entertainment.viewmodel.ThreadDetailViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadDetailScreen(
    onBack: () -> Unit,
    viewModel: ThreadDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    var replyText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var reportContentType by remember { mutableStateOf("") }
    var reportContentId by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }
    var isReporting by remember { mutableStateOf(false) }
    val reportsApi: ReportsApi = koinInject()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason ->
                isReporting = true
                scope.launch {
                    try {
                        reportsApi.createReport(CreateReportRequest(reportContentType, reportContentId, reason))
                        showReportDialog = false
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

    LaunchedEffect(uiState.replies.size) {
        if (uiState.replies.isNotEmpty()) {
            listState.animateScrollToItem(uiState.replies.size)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.thread?.title ?: "Тред", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    uiState.thread?.let { thread ->
                        IconButton(onClick = {
                            reportContentType = "forum_thread"
                            reportContentId = thread.id
                            showReportDialog = true
                        }) {
                            Icon(Icons.Default.Flag, "Пожаловаться",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = replyText, onValueChange = { replyText = it },
                        placeholder = { Text("Ответить...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.sendReply(replyText); replyText = "" },
                        enabled = replyText.isNotBlank() && !uiState.isSending,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Отправить",
                            tint = if (replyText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // OP post
                uiState.thread?.let { thread ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                    ) {
                                        Text("OP", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold)
                                    }
                                    Text(thread.username, style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(Date(thread.createdAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(thread.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Divider
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Ответы (${uiState.replies.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold)
                    }
                }

                // Replies
                items(uiState.replies) { reply ->
                    ReplyCard(
                        reply = reply,
                        onLongPress = {
                            reportContentType = "forum_reply"
                            reportContentId = reply.id
                            showReportDialog = true
                        },
                    )
                }

                if (uiState.replies.isEmpty() && !uiState.isLoading) {
                    item {
                        Text("Пока нет ответов. Будь первым!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReplyCard(reply: ReplyResponse, onLongPress: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = {},
            onLongClick = onLongPress,
        ),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(reply.username, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(reply.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            Text(reply.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
