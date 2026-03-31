package com.toiletgen.feature.entertainment.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.network.api.BooksApi
import com.toiletgen.core.network.model.BookResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class BooksUiState(
    val books: List<BookResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val isSubmitting: Boolean = false,
    val selectedFileName: String? = null,
    val selectedFileUri: Uri? = null,
)

class BooksViewModel(
    application: Application,
    private val booksApi: BooksApi,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val books = booksApi.getBooks()
                _uiState.value = _uiState.value.copy(isLoading = false, books = books)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, selectedFileName = null, selectedFileUri = null)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, selectedFileName = null, selectedFileUri = null)
    }

    fun setSelectedFile(uri: Uri, fileName: String) {
        _uiState.value = _uiState.value.copy(selectedFileUri = uri, selectedFileName = fileName)
    }

    fun uploadBook(title: String, author: String) {
        val uri = _uiState.value.selectedFileUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val context = getApplication<Application>()
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.readBytes()
                } ?: throw IllegalStateException("Не удалось прочитать файл")

                val titleBody = title.toRequestBody("text/plain".toMediaType())
                val authorBody = author.toRequestBody("text/plain".toMediaType())
                val filePart = MultipartBody.Part.createFormData(
                    "file", _uiState.value.selectedFileName ?: "book.pdf",
                    bytes.toRequestBody("application/pdf".toMediaType()),
                )

                booksApi.uploadBook(titleBody, authorBody, filePart)
                _uiState.value = _uiState.value.copy(isSubmitting = false, showAddDialog = false)
                loadBooks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = e.message)
            }
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch {
            try {
                booksApi.deleteBook(id)
                loadBooks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun downloadAndOpenBook(book: BookResponse, onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val responseBody = booksApi.downloadBook(book.id)
                val file = withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "books")
                    dir.mkdirs()
                    val f = File(dir, "${book.id}.pdf")
                    f.outputStream().use { out ->
                        responseBody.byteStream().use { it.copyTo(out) }
                    }
                    f
                }
                onFileReady(file)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка загрузки: ${e.message}")
            }
        }
    }
}
