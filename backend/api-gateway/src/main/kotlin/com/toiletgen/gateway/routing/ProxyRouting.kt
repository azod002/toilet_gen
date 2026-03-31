package com.toiletgen.gateway.routing

import com.toiletgen.gateway.config.ServiceConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.proxyRoutes(client: HttpClient, rawClient: HttpClient, config: ServiceConfig) {

    // Identity Service
    route("/api/v1/auth/{...}") {
        handle { proxy(call, client, config.identityUrl) }
    }
    route("/api/v1/users/{...}") {
        handle { proxy(call, client, config.identityUrl) }
    }

    // Toilet Service
    route("/api/v1/toilets/{...}") {
        handle { proxy(call, client, config.toiletUrl) }
    }
    route("/api/v1/toilets") {
        handle { proxy(call, client, config.toiletUrl) }
    }

    // Books (via Toilet Service) — uses raw client for binary data
    route("/api/v1/books/{...}") {
        handle { proxyBinary(call, rawClient, config.toiletUrl) }
    }
    route("/api/v1/books") {
        handle { proxyBinary(call, rawClient, config.toiletUrl) }
    }

    // Chat (via Toilet Service)
    route("/api/v1/chat/{...}") {
        handle { proxy(call, client, config.toiletUrl) }
    }
    route("/api/v1/chat") {
        handle { proxy(call, client, config.toiletUrl) }
    }

    // Forum (via Toilet Service)
    route("/api/v1/forum/{...}") {
        handle { proxy(call, client, config.toiletUrl) }
    }
    route("/api/v1/forum") {
        handle { proxy(call, client, config.toiletUrl) }
    }

    // Reports (via Toilet Service)
    route("/api/v1/reports/{...}") {
        handle { proxy(call, client, config.toiletUrl) }
    }
    route("/api/v1/reports") {
        handle { proxy(call, client, config.toiletUrl) }
    }

    // SOS Service
    route("/api/v1/sos/{...}") {
        handle { proxy(call, client, config.sosUrl) }
    }

    // Gamification Service
    route("/api/v1/achievements/{...}") {
        handle { proxy(call, client, config.gamificationUrl) }
    }
    route("/api/v1/achievements") {
        handle { proxy(call, client, config.gamificationUrl) }
    }
    route("/api/v1/report/{...}") {
        handle { proxy(call, client, config.gamificationUrl) }
    }
}

// JSON-based proxy (existing behavior)
private suspend fun proxy(call: io.ktor.server.application.ApplicationCall, client: HttpClient, baseUrl: String) {
    val path = call.request.uri
    val response = client.request("$baseUrl$path") {
        method = call.request.httpMethod
        headers {
            call.request.headers.forEach { name, values ->
                if (name !in listOf("Host", "Content-Length", "Transfer-Encoding")) {
                    values.forEach { append(name, it) }
                }
            }
        }
        if (call.request.httpMethod in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
            setBody(call.receiveText())
            contentType(call.request.contentType())
        }
    }

    call.respond(
        status = response.status,
        message = response.bodyAsText(),
    )
}

// Binary-capable proxy (for multipart uploads and file downloads)
private suspend fun proxyBinary(call: io.ktor.server.application.ApplicationCall, client: HttpClient, baseUrl: String) {
    val path = call.request.uri
    val response = client.request("$baseUrl$path") {
        method = call.request.httpMethod
        headers {
            call.request.headers.forEach { name, values ->
                if (name !in listOf("Host", "Content-Length", "Transfer-Encoding")) {
                    values.forEach { append(name, it) }
                }
            }
        }
        if (call.request.httpMethod in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
            val bodyBytes = call.receive<ByteArray>()
            setBody(bodyBytes)
        }
    }

    val responseBytes = response.readBytes()
    val ct = response.contentType() ?: ContentType.Application.OctetStream

    // Forward relevant response headers
    response.headers[HttpHeaders.ContentDisposition]?.let {
        call.response.header(HttpHeaders.ContentDisposition, it)
    }

    call.respondBytes(responseBytes, ct, response.status)
}
