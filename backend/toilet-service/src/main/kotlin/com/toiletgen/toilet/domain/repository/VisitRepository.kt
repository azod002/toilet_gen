package com.toiletgen.toilet.domain.repository

import com.toiletgen.toilet.domain.model.Visit
import java.util.UUID

interface VisitRepository {
    suspend fun create(visit: Visit): Visit
    suspend fun findByUserId(userId: UUID): List<Visit>
    suspend fun deleteByToiletId(toiletId: UUID)
}
