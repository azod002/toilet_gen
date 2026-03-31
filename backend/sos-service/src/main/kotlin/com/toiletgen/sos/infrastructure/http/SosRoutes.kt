package com.toiletgen.sos.infrastructure.http

import com.toiletgen.sos.application.handler.SosHandler
import com.toiletgen.shared.security.userId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class CreateSosRequestBody(val latitude: Double, val longitude: Double)

@Serializable
data class AcceptSosBody(val toiletId: String)

@Serializable
data class SosResponse(
    val id: String, val userId: String, val latitude: Double, val longitude: Double,
    val status: String, val matchedToiletId: String?, val createdAt: Long,
)

fun Route.sosRoutes() {
    val handler by application.inject<SosHandler>()

    authenticate("auth-jwt") {
        route("/api/v1/sos") {
            post("/request") {
                val body = call.receive<CreateSosRequestBody>()
                val request = handler.createRequest(call.userId, body.latitude, body.longitude)
                call.respond(HttpStatusCode.Created, request.toResponse())
            }

            get("/request/{id}/status") {
                val id = call.parameters["id"]!!
                val request = handler.getRequestStatus(id)
                call.respond(request.toResponse())
            }

            post("/request/{id}/accept") {
                val id = call.parameters["id"]!!
                val body = call.receive<AcceptSosBody>()
                handler.acceptRequest(id, call.userId, body.toiletId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Принято"))
            }

            post("/request/{id}/decline") {
                val id = call.parameters["id"]!!
                handler.declineRequest(id, call.userId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Отклонено"))
            }

            webSocket("/ws") {
                // Simple WebSocket for SOS status updates
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val requestId = frame.readText()
                        // Poll status and send updates
                        val status = handler.getRequestStatus(requestId)
                        send(Frame.Text(status.status.name))
                    }
                }
            }
        }
    }
}

private fun com.toiletgen.sos.domain.model.SosRequest.toResponse() = SosResponse(
    id = id.toString(), userId = userId.toString(), latitude = latitude, longitude = longitude,
    status = status.name, matchedToiletId = matchedToiletId?.toString(), createdAt = createdAt.toEpochMilli(),
)
