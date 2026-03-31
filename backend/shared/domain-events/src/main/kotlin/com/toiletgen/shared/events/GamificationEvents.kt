package com.toiletgen.shared.events

import kotlinx.serialization.Serializable

@Serializable
data class AchievementUnlocked(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val userId: String,
    val achievementId: String,
    val achievementName: String,
) : DomainEvent
