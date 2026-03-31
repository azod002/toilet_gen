package com.toiletgen.toilet.infrastructure.http

import com.toiletgen.toilet.infrastructure.persistence.ForumRepliesTable
import com.toiletgen.toilet.infrastructure.persistence.ForumThreadsTable
import com.toiletgen.shared.security.role
import com.toiletgen.shared.security.userId
import com.toiletgen.shared.security.username
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

@Serializable
data class CreateThreadRequest(val title: String, val text: String)

@Serializable
data class CreateReplyRequest(val text: String)

@Serializable
data class ThreadResponse(
    val id: String,
    val userId: String,
    val username: String,
    val title: String,
    val text: String,
    val replyCount: Int,
    val createdAt: Long,
)

@Serializable
data class ReplyResponse(
    val id: String,
    val threadId: String,
    val userId: String,
    val username: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class ThreadDetailResponse(
    val thread: ThreadResponse,
    val replies: List<ReplyResponse>,
)

fun Route.forumRoutes() {
    route("/api/v1/forum") {

        // List threads (newest first)
        get("/threads") {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            val threads = newSuspendedTransaction {
                ForumThreadsTable.selectAll()
                    .orderBy(ForumThreadsTable.createdAt, SortOrder.DESC)
                    .limit(50, offset = (page * 50).toLong())
                    .map { it.toThreadResponse() }
            }
            call.respond(threads)
        }

        // Get thread with replies
        get("/threads/{id}") {
            val threadId = UUID.fromString(call.parameters["id"]!!)
            val result = newSuspendedTransaction {
                val thread = ForumThreadsTable.selectAll()
                    .where { ForumThreadsTable.id eq threadId }
                    .singleOrNull()?.toThreadResponse()
                    ?: throw IllegalArgumentException("Тред не найден")

                val replies = ForumRepliesTable.selectAll()
                    .where { ForumRepliesTable.threadId eq threadId }
                    .orderBy(ForumRepliesTable.createdAt, SortOrder.ASC)
                    .map { row ->
                        ReplyResponse(
                            id = row[ForumRepliesTable.id].toString(),
                            threadId = row[ForumRepliesTable.threadId].toString(),
                            userId = row[ForumRepliesTable.userId].toString(),
                            username = row[ForumRepliesTable.username],
                            text = row[ForumRepliesTable.text],
                            createdAt = row[ForumRepliesTable.createdAt].toEpochMilli(),
                        )
                    }

                ThreadDetailResponse(thread = thread, replies = replies)
            }
            call.respond(result)
        }

        authenticate("auth-jwt") {
            // Create thread
            post("/threads") {
                val req = call.receive<CreateThreadRequest>()
                require(req.title.isNotBlank()) { "Заголовок обязателен" }
                require(req.text.isNotBlank()) { "Текст обязателен" }
                require(req.title.length <= 300) { "Максимум 300 символов в заголовке" }
                require(req.text.length <= 10000) { "Максимум 10000 символов" }

                val thread = newSuspendedTransaction {
                    val id = UUID.randomUUID()
                    val now = Instant.now()
                    ForumThreadsTable.insert {
                        it[ForumThreadsTable.id] = id
                        it[userId] = UUID.fromString(call.userId)
                        it[username] = call.username
                        it[title] = req.title
                        it[text] = req.text
                        it[createdAt] = now
                    }
                    ThreadResponse(
                        id = id.toString(),
                        userId = call.userId,
                        username = call.username,
                        title = req.title,
                        text = req.text,
                        replyCount = 0,
                        createdAt = now.toEpochMilli(),
                    )
                }
                call.respond(HttpStatusCode.Created, thread)
            }

            // Reply to thread
            post("/threads/{id}/replies") {
                val threadId = UUID.fromString(call.parameters["id"]!!)
                val req = call.receive<CreateReplyRequest>()
                require(req.text.isNotBlank()) { "Текст обязателен" }
                require(req.text.length <= 5000) { "Максимум 5000 символов" }

                val reply = newSuspendedTransaction {
                    // Verify thread exists
                    ForumThreadsTable.selectAll()
                        .where { ForumThreadsTable.id eq threadId }
                        .singleOrNull() ?: throw IllegalArgumentException("Тред не найден")

                    val id = UUID.randomUUID()
                    val now = Instant.now()
                    ForumRepliesTable.insert {
                        it[ForumRepliesTable.id] = id
                        it[ForumRepliesTable.threadId] = threadId
                        it[userId] = UUID.fromString(call.userId)
                        it[username] = call.username
                        it[text] = req.text
                        it[createdAt] = now
                    }

                    // Increment reply count
                    ForumThreadsTable.update({ ForumThreadsTable.id eq threadId }) {
                        with(SqlExpressionBuilder) {
                            it[replyCount] = replyCount + 1
                        }
                    }

                    ReplyResponse(
                        id = id.toString(),
                        threadId = threadId.toString(),
                        userId = call.userId,
                        username = call.username,
                        text = req.text,
                        createdAt = now.toEpochMilli(),
                    )
                }
                call.respond(HttpStatusCode.Created, reply)
            }

            // Delete thread (owner or moderator)
            delete("/threads/{id}") {
                val threadId = UUID.fromString(call.parameters["id"]!!)
                newSuspendedTransaction {
                    val thread = ForumThreadsTable.selectAll()
                        .where { ForumThreadsTable.id eq threadId }
                        .singleOrNull() ?: throw IllegalArgumentException("Тред не найден")

                    if (call.role != "moderator" && thread[ForumThreadsTable.userId].toString() != call.userId) {
                        throw IllegalArgumentException("Нет прав на удаление")
                    }

                    ForumRepliesTable.deleteWhere { ForumRepliesTable.threadId eq threadId }
                    ForumThreadsTable.deleteWhere { ForumThreadsTable.id eq threadId }
                }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ResultRow.toThreadResponse() = ThreadResponse(
    id = this[ForumThreadsTable.id].toString(),
    userId = this[ForumThreadsTable.userId].toString(),
    username = this[ForumThreadsTable.username],
    title = this[ForumThreadsTable.title],
    text = this[ForumThreadsTable.text],
    replyCount = this[ForumThreadsTable.replyCount],
    createdAt = this[ForumThreadsTable.createdAt].toEpochMilli(),
)
