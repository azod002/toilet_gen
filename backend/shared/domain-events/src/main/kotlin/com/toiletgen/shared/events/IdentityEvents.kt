package com.toiletgen.shared.events

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistered(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val username: String,
    val email: String,
) : DomainEvent

@Serializable
data class UserProfileUpdated(
    override val eventId: String,
    override val occurredAt: String,
    override val aggregateId: String,
    val username: String,
    val avatarUrl: String?,
) : DomainEvent
