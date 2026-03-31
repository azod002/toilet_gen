package com.toiletgen.gateway

import com.toiletgen.gateway.config.ServiceConfig
import com.toiletgen.gateway.middleware.RateLimiter
import com.toiletgen.gateway.routing.proxyRoutes
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = ServiceConfig()
    val rateLimiter = RateLimiter()

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        engine {
            requestTimeout = 30_000
        }
    }

    // Raw client without ContentNegotiation for binary proxying (multipart, file downloads)
    val rawClient = HttpClient(CIO) {
        engine {
            requestTimeout = 120_000
        }
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    install(CallLogging)

    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
    }

    routing {
        // Health check
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok", "service" to "api-gateway"))
        }

        proxyRoutes(client, rawClient, config)
    }
}
