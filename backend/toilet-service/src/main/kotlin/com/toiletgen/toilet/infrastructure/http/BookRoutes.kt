package com.toiletgen.toilet.infrastructure.http

import com.toiletgen.toilet.domain.model.Book
import com.toiletgen.toilet.domain.repository.BookRepository
import com.toiletgen.shared.security.role
import com.toiletgen.shared.security.userId
import com.toiletgen.shared.security.username
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.io.File
import java.util.UUID

private val booksDir = File("/data/books").also { it.mkdirs() }

@Serializable
data class BookResponse(
    val id: String,
    val userId: String,
    val username: String,
    val title: String,
    val author: String,
    val fileSize: Long,
    val createdAt: Long,
)

fun Route.bookRoutes() {
    val bookRepository by application.inject<BookRepository>()

    route("/api/v1/books") {
        get {
            val books = bookRepository.findAll()
            call.respond(books.map { it.toResponse() })
        }

        // Download PDF
        get("/{id}/download") {
            val bookId = UUID.fromString(call.parameters["id"]!!)
            val book = bookRepository.findById(bookId)
                ?: throw IllegalArgumentException("Книга не найдена")
            val file = File(booksDir, "${book.id}.pdf")
            if (!file.exists()) throw IllegalArgumentException("Файл не найден")
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName, "${book.title}.pdf"
                ).toString()
            )
            call.respondFile(file)
        }

        authenticate("auth-jwt") {
            post {
                val multipart = call.receiveMultipart()
                var title = ""
                var author = ""
                var fileBytes: ByteArray? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "title" -> title = part.value
                                "author" -> author = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            fileBytes = part.streamProvider().readBytes()
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                require(title.isNotBlank()) { "Название обязательно" }
                requireNotNull(fileBytes) { "PDF файл обязателен" }
                require(fileBytes!!.size <= 50 * 1024 * 1024) { "Максимальный размер файла: 50 МБ" }

                val book = Book(
                    userId = UUID.fromString(call.userId),
                    username = call.username,
                    title = title,
                    author = author.ifBlank { call.username },
                    fileSize = fileBytes!!.size.toLong(),
                )

                // Save file
                File(booksDir, "${book.id}.pdf").writeBytes(fileBytes!!)

                val saved = bookRepository.create(book)
                call.respond(HttpStatusCode.Created, saved.toResponse())
            }

            delete("/{id}") {
                val bookId = UUID.fromString(call.parameters["id"]!!)
                val book = bookRepository.findById(bookId)
                    ?: throw IllegalArgumentException("Книга не найдена")
                if (call.role != "moderator" && book.userId.toString() != call.userId) {
                    throw IllegalArgumentException("Нет прав на удаление")
                }
                // Delete file
                File(booksDir, "${book.id}.pdf").delete()
                bookRepository.delete(bookId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun Book.toResponse() = BookResponse(
    id = id.toString(),
    userId = userId.toString(),
    username = username,
    title = title,
    author = author,
    fileSize = fileSize,
    createdAt = createdAt.toEpochMilli(),
)
