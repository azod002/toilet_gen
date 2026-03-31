package com.toiletgen.toilet.infrastructure.http

import com.toiletgen.toilet.infrastructure.persistence.ChatMessagesTable
import com.toiletgen.toilet.infrastructure.persistence.PrivateMessagesTable
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

@Serializable
data class SendMessageRequest(val text: String)

@Serializable
data class SendPrivateMessageRequest(val text: String)

@Serializable
data class ChatMessageResponse(
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class PrivateMessageResponse(
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val receiverId: String,
    val receiverUsername: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class ConversationResponse(
    val userId: String,
    val username: String,
    val lastMessage: String,
    val lastMessageAt: Long,
)

fun Route.chatRoutes() {
    route("/api/v1/chat") {

        // Global chat — list messages (latest 100, or since timestamp)
        get("/messages") {
            val since = call.request.queryParameters["since"]?.toLongOrNull()
            val messages = newSuspendedTransaction {
                val query = if (since != null) {
                    ChatMessagesTable.selectAll()
                        .where { ChatMessagesTable.createdAt greater Instant.ofEpochMilli(since) }
                } else {
                    ChatMessagesTable.selectAll()
                }
                query.orderBy(ChatMessagesTable.createdAt, SortOrder.DESC)
                    .limit(100)
                    .map { row ->
                        ChatMessageResponse(
                            id = row[ChatMessagesTable.id].toString(),
                            senderId = row[ChatMessagesTable.senderId].toString(),
                            senderUsername = row[ChatMessagesTable.senderUsername],
                            text = row[ChatMessagesTable.text],
                            createdAt = row[ChatMessagesTable.createdAt].toEpochMilli(),
                        )
                    }
            }
            call.respond(messages.reversed())
        }

        authenticate("auth-jwt") {
            // Send message to global chat
            post("/messages") {
                val req = call.receive<SendMessageRequest>()
                require(req.text.isNotBlank()) { "Сообщение не может быть пустым" }
                require(req.text.length <= 2000) { "Максимум 2000 символов" }

                val msg = newSuspendedTransaction {
                    val id = UUID.randomUUID()
                    val now = Instant.now()
                    ChatMessagesTable.insert {
                        it[ChatMessagesTable.id] = id
                        it[senderId] = UUID.fromString(call.userId)
                        it[senderUsername] = call.username
                        it[text] = req.text
                        it[createdAt] = now
                    }
                    ChatMessageResponse(
                        id = id.toString(),
                        senderId = call.userId,
                        senderUsername = call.username,
                        text = req.text,
                        createdAt = now.toEpochMilli(),
                    )
                }
                call.respond(HttpStatusCode.Created, msg)
            }

            // List conversations (private messages grouped by other user)
            get("/conversations") {
                val uid = UUID.fromString(call.userId)
                val conversations = newSuspendedTransaction {
                    // Get all private messages involving this user, pick latest per partner
                    val sent = PrivateMessagesTable.selectAll()
                        .where { PrivateMessagesTable.senderId eq uid }
                    val received = PrivateMessagesTable.selectAll()
                        .where { PrivateMessagesTable.receiverId eq uid }

                    val allMessages = (sent.toList() + received.toList())
                    val grouped = allMessages.groupBy { row ->
                        val sid = row[PrivateMessagesTable.senderId]
                        val rid = row[PrivateMessagesTable.receiverId]
                        if (sid == uid) rid else sid
                    }

                    grouped.map { (partnerId, msgs) ->
                        val latest = msgs.maxByOrNull { it[PrivateMessagesTable.createdAt] }!!
                        val sid = latest[PrivateMessagesTable.senderId]
                        val partnerName = if (sid == uid) {
                            latest[PrivateMessagesTable.receiverUsername]
                        } else {
                            latest[PrivateMessagesTable.senderUsername]
                        }
                        ConversationResponse(
                            userId = partnerId.toString(),
                            username = partnerName,
                            lastMessage = latest[PrivateMessagesTable.text].take(100),
                            lastMessageAt = latest[PrivateMessagesTable.createdAt].toEpochMilli(),
                        )
                    }.sortedByDescending { it.lastMessageAt }
                }
                call.respond(conversations)
            }

            // Get private messages with a specific user
            get("/private/{userId}") {
                val uid = UUID.fromString(call.userId)
                val partnerId = UUID.fromString(call.parameters["userId"]!!)
                val since = call.request.queryParameters["since"]?.toLongOrNull()

                val messages = newSuspendedTransaction {
                    val baseCondition = (
                        (PrivateMessagesTable.senderId eq uid) and (PrivateMessagesTable.receiverId eq partnerId)
                    ) or (
                        (PrivateMessagesTable.senderId eq partnerId) and (PrivateMessagesTable.receiverId eq uid)
                    )

                    val condition = if (since != null) {
                        baseCondition and (PrivateMessagesTable.createdAt greater Instant.ofEpochMilli(since))
                    } else {
                        baseCondition
                    }

                    PrivateMessagesTable.selectAll()
                        .where { condition }
                        .orderBy(PrivateMessagesTable.createdAt, SortOrder.DESC)
                        .limit(100)
                        .map { row ->
                            PrivateMessageResponse(
                                id = row[PrivateMessagesTable.id].toString(),
                                senderId = row[PrivateMessagesTable.senderId].toString(),
                                senderUsername = row[PrivateMessagesTable.senderUsername],
                                receiverId = row[PrivateMessagesTable.receiverId].toString(),
                                receiverUsername = row[PrivateMessagesTable.receiverUsername],
                                text = row[PrivateMessagesTable.text],
                                createdAt = row[PrivateMessagesTable.createdAt].toEpochMilli(),
                            )
                        }
                }.reversed()
                call.respond(messages)
            }

            // Send private message
            post("/private/{userId}") {
                val req = call.receive<SendPrivateMessageRequest>()
                require(req.text.isNotBlank()) { "Сообщение не может быть пустым" }
                require(req.text.length <= 2000) { "Максимум 2000 символов" }

                val receiverId = UUID.fromString(call.parameters["userId"]!!)
                // Get receiver username from identity service... for now just store the ID
                // We'll pass it from the client
                val receiverUsername = call.request.queryParameters["receiverUsername"] ?: "user"

                val msg = newSuspendedTransaction {
                    val id = UUID.randomUUID()
                    val now = Instant.now()
                    PrivateMessagesTable.insert {
                        it[PrivateMessagesTable.id] = id
                        it[senderId] = UUID.fromString(call.userId)
                        it[senderUsername] = call.username
                        it[PrivateMessagesTable.receiverId] = receiverId
                        it[PrivateMessagesTable.receiverUsername] = receiverUsername
                        it[text] = req.text
                        it[createdAt] = now
                    }
                    PrivateMessageResponse(
                        id = id.toString(),
                        senderId = call.userId,
                        senderUsername = call.username,
                        receiverId = receiverId.toString(),
                        receiverUsername = receiverUsername,
                        text = req.text,
                        createdAt = now.toEpochMilli(),
                    )
                }
                call.respond(HttpStatusCode.Created, msg)
            }
        }
    }
}
