package com.toiletgen.sos

import com.toiletgen.sos.config.sosModule
import com.toiletgen.sos.infrastructure.http.sosRoutes
import com.toiletgen.sos.infrastructure.persistence.SosNotificationsTable
import com.toiletgen.sos.infrastructure.persistence.SosRequestsTable
import com.toiletgen.shared.events.EventTopics
import com.toiletgen.shared.messaging.messagingModule
import com.toiletgen.shared.security.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8083, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val jwtConfig = JwtConfig(
        secret = System.getenv("JWT_SECRET") ?: "toilet-gen-secret-key-change-in-prod",
        issuer = "toiletgen-identity", audience = "toiletgen-app", realm = "ToiletGen",
    )

    install(Koin) {
        modules(
            securityModule(jwtConfig),
            messagingModule(
                bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092",
                groupId = "sos-service",
                topics = listOf(EventTopics.TOILET),
            ),
            sosModule,
        )
    }

    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
    install(WebSockets)
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Ошибка")))
        }
        exception<Exception> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "Внутренняя ошибка")))
        }
    }

    val jwtService by inject<JwtService>()
    configureJwtAuth(jwtService, jwtConfig)

    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/sos_db"
    Database.connect(HikariDataSource(HikariConfig().apply {
        jdbcUrl = dbUrl; username = System.getenv("DATABASE_USER") ?: "postgres"
        password = System.getenv("DATABASE_PASSWORD") ?: "postgres"
        driverClassName = "org.postgresql.Driver"; maximumPoolSize = 10
    }))
    transaction { SchemaUtils.create(SosRequestsTable, SosNotificationsTable) }

    routing { sosRoutes() }
}
