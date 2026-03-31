package com.toiletgen.gamification.application.handler

import com.toiletgen.shared.events.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class EventHandler(
    private val gamificationHandler: GamificationHandler,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val logger = LoggerFactory.getLogger(EventHandler::class.java)

    suspend fun handle(topic: String, key: String?, value: String) {
        try {
            when (topic) {
                EventTopics.TOILET -> handleToiletEvent(value)
                EventTopics.SOS -> handleSosEvent(value)
                EventTopics.IDENTITY -> handleIdentityEvent(value)
            }
        } catch (e: Exception) {
            logger.error("Ошибка обработки события: $value", e)
        }
    }

    private suspend fun handleToiletEvent(value: String) {
        // Try parsing as different event types
        try {
            val event = json.decodeFromString<ReviewAdded>(value)
            gamificationHandler.processReviewAdded(event.userId)
            return
        } catch (_: Exception) {}
        try {
            val event = json.decodeFromString<ToiletCreated>(value)
            gamificationHandler.processToiletCreated(event.ownerId)
            return
        } catch (_: Exception) {}
        try {
            val event = json.decodeFromString<ToiletVisited>(value)
            gamificationHandler.processToiletVisited(event.userId)
            return
        } catch (_: Exception) {}
    }

    private suspend fun handleSosEvent(value: String) {
        try {
            val event = json.decodeFromString<SOSRequested>(value)
            gamificationHandler.processSosRequested(event.userId)
            return
        } catch (_: Exception) {}
        try {
            val event = json.decodeFromString<SOSAccepted>(value)
            gamificationHandler.processSosAccepted(event.ownerId)
            return
        } catch (_: Exception) {}
    }

    private suspend fun handleIdentityEvent(value: String) {
        // Could process UserRegistered to create initial stats
    }
}
