package com.toiletgen.toilet.infrastructure.persistence

import com.toiletgen.toilet.domain.model.Visit
import com.toiletgen.toilet.domain.repository.VisitRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class VisitRepositoryImpl : VisitRepository {

    override suspend fun create(visit: Visit): Visit = newSuspendedTransaction {
        VisitsTable.insert {
            it[id] = visit.id
            it[userId] = visit.userId
            it[toiletId] = visit.toiletId
            it[visitedAt] = visit.visitedAt
        }
        visit
    }

    override suspend fun deleteByToiletId(toiletId: UUID) = newSuspendedTransaction {
        VisitsTable.deleteWhere { Op.build { VisitsTable.toiletId eq toiletId } }
        Unit
    }

    override suspend fun findByUserId(userId: UUID): List<Visit> = newSuspendedTransaction {
        VisitsTable.selectAll()
            .where { VisitsTable.userId eq userId }
            .orderBy(VisitsTable.visitedAt, SortOrder.DESC)
            .map {
                Visit(
                    id = it[VisitsTable.id],
                    userId = it[VisitsTable.userId],
                    toiletId = it[VisitsTable.toiletId],
                    visitedAt = it[VisitsTable.visitedAt],
                )
            }
    }
}
