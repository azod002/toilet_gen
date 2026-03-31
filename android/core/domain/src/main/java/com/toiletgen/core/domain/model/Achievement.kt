package com.toiletgen.core.domain.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
)
