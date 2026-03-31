package com.toiletgen.shared.events

import kotlinx.serialization.Serializable

@Serializable
data class SOSRequested(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
) : DomainEvent

@Serializable
data class SOSAccepted(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val requestId: String,
    val ownerId: String,
    val toiletId: String,
) : DomainEvent

@Serializable
data class SOSDeclined(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val requestId: String,
    val ownerId: String,
) : DomainEvent
