package com.toiletgen.toilet.infrastructure.persistence

import com.toiletgen.toilet.domain.model.Book
import com.toiletgen.toilet.domain.repository.BookRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class BookRepositoryImpl : BookRepository {

    override suspend fun create(book: Book): Book = newSuspendedTransaction {
        BooksTable.insert {
            it[id] = book.id
            it[userId] = book.userId
            it[username] = book.username
            it[title] = book.title
            it[author] = book.author
            it[fileSize] = book.fileSize
            it[createdAt] = book.createdAt
        }
        book
    }

    override suspend fun findAll(): List<Book> = newSuspendedTransaction {
        BooksTable.selectAll()
            .orderBy(BooksTable.createdAt, SortOrder.DESC)
            .map { it.toBook() }
    }

    override suspend fun findById(id: UUID): Book? = newSuspendedTransaction {
        BooksTable.selectAll().where { BooksTable.id eq id }.singleOrNull()?.toBook()
    }

    override suspend fun delete(id: UUID) = newSuspendedTransaction {
        BooksTable.deleteWhere { Op.build { BooksTable.id eq id } }
        Unit
    }

    private fun ResultRow.toBook() = Book(
        id = this[BooksTable.id],
        userId = this[BooksTable.userId],
        username = this[BooksTable.username],
        title = this[BooksTable.title],
        author = this[BooksTable.author],
        fileSize = this[BooksTable.fileSize],
        createdAt = this[BooksTable.createdAt],
    )
}
