package com.toiletgen.feature.entertainment.ui

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.toiletgen.core.network.model.BookResponse
import com.toiletgen.feature.entertainment.viewmodel.BooksViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onBack: () -> Unit,
    viewModel: BooksViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.showAddDialog) {
        AddBookDialog(
            viewModel = viewModel,
            isSubmitting = uiState.isSubmitting,
            selectedFileName = uiState.selectedFileName,
            onDismiss = { viewModel.hideAddDialog() },
            onSubmit = { title, author -> viewModel.uploadBook(title, author) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Книги") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(Icons.Default.Add, "Загрузить книгу")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.padding(32.dp))
                    }
                }
            }

            if (uiState.books.isEmpty() && !uiState.isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Пока нет книг",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Загрузите первую книгу в формате PDF",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            items(uiState.books) { book ->
                BookCard(
                    book = book,
                    onOpen = {
                        viewModel.downloadAndOpenBook(book) { file ->
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file,
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                context.startActivity(Intent.createChooser(intent, "Открыть PDF"))
                            }
                        }
                    },
                )
            }

            if (uiState.error != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            uiState.error!!,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCard(
    book: BookResponse,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onOpen,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    book.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        book.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${formatFileSize(book.fileSize)} · ${book.username}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.Download,
                "Открыть",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes Б"
    bytes < 1024 * 1024 -> "${bytes / 1024} КБ"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} МБ"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBookDialog(
    viewModel: BooksViewModel,
    isSubmitting: Boolean,
    selectedFileName: String?,
    onDismiss: () -> Unit,
    onSubmit: (title: String, author: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val name = cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else "book.pdf"
            } else "book.pdf"
        } ?: "book.pdf"
        viewModel.setSelectedFile(uri, name)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Загрузить книгу") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Автор") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedButton(
                    onClick = { filePicker.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.UploadFile, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedFileName ?: "Выбрать PDF файл")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(title, author) },
                enabled = title.isNotBlank() && selectedFileName != null && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Загрузить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}
