package com.toiletgen.gamification

import com.toiletgen.gamification.application.handler.EventHandler
import com.toiletgen.gamification.config.gamificationModule
import com.toiletgen.gamification.domain.model.AchievementCatalog
import com.toiletgen.gamification.domain.repository.GamificationRepository
import com.toiletgen.gamification.infrastructure.http.gamificationRoutes
import com.toiletgen.gamification.infrastructure.persistence.*
import com.toiletgen.shared.events.EventTopics
import com.toiletgen.shared.messaging.EventConsumer
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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8084, host = "0.0.0.0", module = Application::module).start(wait = true)
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
                groupId = "gamification-service",
                topics = listOf(EventTopics.IDENTITY, EventTopics.TOILET, EventTopics.SOS),
            ),
            gamificationModule,
        )
    }

    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
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

    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/gamification_db"
    Database.connect(HikariDataSource(HikariConfig().apply {
        jdbcUrl = dbUrl; username = System.getenv("DATABASE_USER") ?: "postgres"
        password = System.getenv("DATABASE_PASSWORD") ?: "postgres"
        driverClassName = "org.postgresql.Driver"; maximumPoolSize = 10
    }))
    transaction {
        SchemaUtils.create(AchievementsCatalogTable, UserAchievementsTable, UserStatsTable)
    }

    // Seed default achievements
    val repo by inject<GamificationRepository>()
    CoroutineScope(Dispatchers.IO).launch {
        val existing = repo.getAllAchievements()
        val existingTypes = existing.map { it.conditionType }.toSet()
        val defaults = listOf(
            AchievementCatalog(name = "Первый отзыв", description = "Написать первый отзыв", icon = "review", conditionType = "FIRST_REVIEW", threshold = 1),
            AchievementCatalog(name = "Критик", description = "Написать 10 отзывов", icon = "reviews_10", conditionType = "REVIEWER_10", threshold = 10),
            AchievementCatalog(name = "Картограф", description = "Добавить первую точку на карту", icon = "map", conditionType = "CARTOGRAPHER", threshold = 1),
            AchievementCatalog(name = "Первый SOS", description = "Отправить первый SOS запрос", icon = "sos", conditionType = "FIRST_SOS", threshold = 1),
            AchievementCatalog(name = "Добрый самаритянин", description = "Помочь человеку по SOS", icon = "help", conditionType = "GOOD_SAMARITAN", threshold = 1),
            AchievementCatalog(name = "Первый визит", description = "Дойти до туалета по маршруту", icon = "visit", conditionType = "FIRST_VISIT", threshold = 1),
            AchievementCatalog(name = "Путешественник", description = "Посетить 10 туалетов по маршруту", icon = "visits_10", conditionType = "VISITOR_10", threshold = 10),
        )
        defaults.filter { it.conditionType !in existingTypes }.forEach { repo.createAchievement(it) }
    }

    // Start Kafka consumer
    val eventConsumer by inject<EventConsumer>()
    val eventHandler by inject<EventHandler>()
    eventConsumer.start(CoroutineScope(Dispatchers.IO)) { topic, key, value ->
        eventHandler.handle(topic, key, value)
    }

    routing { gamificationRoutes() }
}
