package com.toiletgen.toilet.domain.repository

import com.toiletgen.toilet.domain.model.Book
import java.util.UUID

interface BookRepository {
    suspend fun create(book: Book): Book
    suspend fun findAll(): List<Book>
    suspend fun findById(id: UUID): Book?
    suspend fun delete(id: UUID)
}
