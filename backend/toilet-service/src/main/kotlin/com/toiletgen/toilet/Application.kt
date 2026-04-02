package com.toiletgen.toilet

import com.toiletgen.toilet.config.toiletModule
import com.toiletgen.toilet.infrastructure.http.bookRoutes
import com.toiletgen.toilet.infrastructure.http.chatRoutes
import com.toiletgen.toilet.infrastructure.http.forumRoutes
import com.toiletgen.toilet.infrastructure.http.reportRoutes
import com.toiletgen.toilet.infrastructure.http.stampRoutes
import com.toiletgen.toilet.infrastructure.http.toiletRoutes
import com.toiletgen.toilet.infrastructure.persistence.ForumRepliesTable
import com.toiletgen.toilet.infrastructure.persistence.ForumThreadsTable
import com.toiletgen.toilet.infrastructure.persistence.BooksTable
import com.toiletgen.toilet.infrastructure.persistence.ChatMessagesTable
import com.toiletgen.toilet.infrastructure.persistence.PrivateMessagesTable
import com.toiletgen.toilet.infrastructure.persistence.DatabaseSeeder
import com.toiletgen.toilet.infrastructure.persistence.ReportsTable
import com.toiletgen.toilet.infrastructure.persistence.ReviewsTable
import com.toiletgen.shared.messaging.outbox.OutboxPoller
import com.toiletgen.shared.messaging.outbox.OutboxTable
import com.toiletgen.toilet.infrastructure.persistence.StampTradesTable
import com.toiletgen.toilet.infrastructure.persistence.ToiletsTable
import com.toiletgen.toilet.infrastructure.persistence.UserStampsTable
import com.toiletgen.toilet.infrastructure.persistence.VisitsTable
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
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8082, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val jwtConfig = JwtConfig(
        secret = System.getenv("JWT_SECRET") ?: "toilet-gen-secret-key-change-in-prod",
        issuer = "toiletgen-identity",
        audience = "toiletgen-app",
        realm = "ToiletGen",
    )

    install(Koin) {
        modules(
            securityModule(jwtConfig),
            messagingModule(
                bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092",
                groupId = "toilet-service",
                topics = listOf(EventTopics.IDENTITY),
            ),
            toiletModule,
        )
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
    }

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

    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/toilet_db"
    val dbUser = System.getenv("DATABASE_USER") ?: "postgres"
    val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "postgres"

    Database.connect(HikariDataSource(HikariConfig().apply {
        jdbcUrl = dbUrl; username = dbUser; password = dbPassword
        driverClassName = "org.postgresql.Driver"; maximumPoolSize = 10
    }))
    transaction { SchemaUtils.create(ToiletsTable, ReviewsTable, VisitsTable, UserStampsTable, StampTradesTable, OutboxTable, BooksTable, ChatMessagesTable, PrivateMessagesTable, ForumThreadsTable, ForumRepliesTable, ReportsTable) }
    DatabaseSeeder.seedIfEmpty()

    // Start Transactional Outbox poller
    val outboxPoller by inject<OutboxPoller>()
    outboxPoller.start(CoroutineScope(Dispatchers.IO))

    routing {
        toiletRoutes()
        stampRoutes()
        bookRoutes()
        chatRoutes()
        forumRoutes()
        reportRoutes()
    }
}
