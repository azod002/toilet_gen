package com.toiletgen.toilet.infrastructure.http

import com.toiletgen.toilet.infrastructure.persistence.ReportsTable
import com.toiletgen.shared.security.userId
import com.toiletgen.shared.security.username
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

@Serializable
data class CreateReportRequest(
    val contentType: String,
    val contentId: String,
    val reason: String,
)

@Serializable
data class ReportResponse(
    val id: String,
    val contentType: String,
    val contentId: String,
    val reason: String,
    val status: String,
)

fun Route.reportRoutes() {
    authenticate("auth-jwt") {
        route("/api/v1/reports") {
            post {
                val req = call.receive<CreateReportRequest>()
                val reporterId = UUID.fromString(call.userId)
                val reporterName = call.username ?: "user"

                require(req.contentType in listOf("chat_message", "private_message", "forum_thread", "forum_reply")) {
                    "Неверный тип контента"
                }
                require(req.reason.isNotBlank() && req.reason.length <= 500) {
                    "Причина жалобы обязательна (до 500 символов)"
                }

                val reportId = UUID.randomUUID()
                newSuspendedTransaction {
                    ReportsTable.insert {
                        it[id] = reportId
                        it[ReportsTable.reporterId] = reporterId
                        it[reporterUsername] = reporterName
                        it[contentType] = req.contentType
                        it[contentId] = UUID.fromString(req.contentId)
                        it[reason] = req.reason
                        it[status] = "pending"
                        it[createdAt] = Instant.now()
                    }
                }

                call.respond(
                    HttpStatusCode.Created,
                    ReportResponse(
                        id = reportId.toString(),
                        contentType = req.contentType,
                        contentId = req.contentId,
                        reason = req.reason,
                        status = "pending",
                    ),
                )
            }
        }
    }
}
