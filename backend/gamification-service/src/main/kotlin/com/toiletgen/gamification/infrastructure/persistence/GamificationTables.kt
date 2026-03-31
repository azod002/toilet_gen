package com.toiletgen.gamification.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object AchievementsCatalogTable : Table("achievements_catalog") {
    val id = uuid("id")
    val name = varchar("name", 200)
    val description = text("description")
    val icon = varchar("icon", 500)
    val conditionType = varchar("condition_type", 50)
    val threshold = integer("threshold")
    override val primaryKey = PrimaryKey(id)
}

object UserAchievementsTable : Table("user_achievements") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val achievementId = uuid("achievement_id").references(AchievementsCatalogTable.id)
    val unlockedAt = timestamp("unlocked_at")
    override val primaryKey = PrimaryKey(id)
}

object UserStatsTable : Table("user_stats") {
    val userId = uuid("user_id")
    val year = integer("year")
    val toiletsVisited = integer("toilets_visited").default(0)
    val reviewsWritten = integer("reviews_written").default(0)
    val sosSent = integer("sos_sent").default(0)
    val sosHelped = integer("sos_helped").default(0)
    val toiletsCreated = integer("toilets_created").default(0)
    override val primaryKey = PrimaryKey(userId, year)
}
