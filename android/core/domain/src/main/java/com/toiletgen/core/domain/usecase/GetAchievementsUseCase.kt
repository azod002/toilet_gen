package com.toiletgen.core.domain.usecase

import com.toiletgen.core.domain.repository.AchievementRepository

class GetAchievementsUseCase(private val repository: AchievementRepository) {
    operator fun invoke() = repository.getMyAchievements()
}
