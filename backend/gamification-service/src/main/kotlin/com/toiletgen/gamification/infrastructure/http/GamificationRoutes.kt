package com.toiletgen.gamification.infrastructure.http

import com.toiletgen.gamification.application.handler.GamificationHandler
import com.toiletgen.shared.security.userId
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class AchievementResponse(
    val id: String, val name: String, val description: String,
    val iconUrl: String, val isUnlocked: Boolean, val unlockedAt: Long? = null,
)

@Serializable
data class YearlyReportResponse(
    val year: Int, val toiletsVisited: Int, val reviewsWritten: Int,
    val sosSent: Int, val sosHelped: Int, val toiletsCreated: Int,
)

fun Route.gamificationRoutes() {
    val handler by application.inject<GamificationHandler>()

    authenticate("auth-jwt") {
        get("/api/v1/achievements") {
            val all = handler.getAllAchievements()
            call.respond(all.map {
                AchievementResponse(it.id.toString(), it.name, it.description, it.icon, false)
            })
        }

        get("/api/v1/achievements/me") {
            val achievements = handler.getUserAchievements(call.userId)
            call.respond(achievements.map { (a, unlocked) ->
                AchievementResponse(a.id.toString(), a.name, a.description, a.icon, unlocked)
            })
        }

        get("/api/v1/report/yearly/{year}") {
            val year = call.parameters["year"]?.toIntOrNull() ?: throw IllegalArgumentException("Год обязателен")
            val stats = handler.getYearlyReport(call.userId, year)
            call.respond(YearlyReportResponse(
                stats.year, stats.toiletsVisited, stats.reviewsWritten,
                stats.sosSent, stats.sosHelped, stats.toiletsCreated,
            ))
        }
    }
}
