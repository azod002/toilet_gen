package com.toiletgen.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToiletCreated(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val ownerId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("toiletType") val type: String,
) : DomainEvent

@Serializable
data class ReviewAdded(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val toiletId: String,
    val userId: String,
    val rating: Int,
) : DomainEvent

@Serializable
data class ToiletVisited(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val toiletId: String,
    val userId: String,
) : DomainEvent

@Serializable
data class RatingUpdated(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val toiletId: String,
    val newAvgRating: Double,
) : DomainEvent
