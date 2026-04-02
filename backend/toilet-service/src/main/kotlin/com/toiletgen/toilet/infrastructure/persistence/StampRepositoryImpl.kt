package com.toiletgen.toilet.infrastructure.persistence

import com.toiletgen.toilet.domain.model.StampTrade
import com.toiletgen.toilet.domain.model.TradeStatus
import com.toiletgen.toilet.domain.model.UserStamp
import com.toiletgen.toilet.domain.repository.StampRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

class StampRepositoryImpl : StampRepository {

    override suspend fun collectStamp(userId: UUID, toiletId: UUID): UserStamp = newSuspendedTransaction {
        val stamp = UserStamp(userId = userId, toiletId = toiletId)
        UserStampsTable.insert {
            it[id] = stamp.id
            it[UserStampsTable.userId] = stamp.userId
            it[UserStampsTable.toiletId] = stamp.toiletId
            it[obtainedAt] = stamp.obtainedAt
        }
        stamp
    }

    override suspend fun getLastStampTime(userId: UUID, toiletId: UUID): Instant? = newSuspendedTransaction {
        UserStampsTable.selectAll()
            .where { (UserStampsTable.userId eq userId) and (UserStampsTable.toiletId eq toiletId) }
            .orderBy(UserStampsTable.obtainedAt, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.get(UserStampsTable.obtainedAt)
    }

    override suspend fun getUserStamps(userId: UUID): List<UserStamp> = newSuspendedTransaction {
        UserStampsTable.selectAll()
            .where { UserStampsTable.userId eq userId }
            .orderBy(UserStampsTable.obtainedAt, SortOrder.DESC)
            .map { it.toStamp() }
    }

    override suspend fun getStampById(id: UUID): UserStamp? = newSuspendedTransaction {
        UserStampsTable.selectAll()
            .where { UserStampsTable.id eq id }
            .singleOrNull()?.toStamp()
    }

    override suspend fun deleteStamp(id: UUID): Unit = newSuspendedTransaction {
        UserStampsTable.deleteWhere { UserStampsTable.id eq id }
    }

    override suspend fun transferStamp(stampId: UUID, newOwnerId: UUID): Unit = newSuspendedTransaction {
        UserStampsTable.update({ UserStampsTable.id eq stampId }) {
            it[userId] = newOwnerId
        }
    }

    override suspend fun createTrade(trade: StampTrade): StampTrade = newSuspendedTransaction {
        StampTradesTable.insert {
            it[id] = trade.id
            it[senderId] = trade.senderId
            it[receiverId] = trade.receiverId
            it[senderStampId] = trade.senderStampId
            it[receiverStampId] = trade.receiverStampId
            it[status] = trade.status.name.lowercase()
            it[createdAt] = trade.createdAt
        }
        trade
    }

    override suspend fun getTradeById(id: UUID): StampTrade? = newSuspendedTransaction {
        StampTradesTable.selectAll()
            .where { StampTradesTable.id eq id }
            .singleOrNull()?.toTrade()
    }

    override suspend fun getPendingTradesForUser(userId: UUID): List<StampTrade> = newSuspendedTransaction {
        StampTradesTable.selectAll()
            .where {
                (StampTradesTable.status eq "pending") and
                    ((StampTradesTable.senderId eq userId) or (StampTradesTable.receiverId eq userId))
            }
            .orderBy(StampTradesTable.createdAt, SortOrder.DESC)
            .map { it.toTrade() }
    }

    override suspend fun updateTradeStatus(tradeId: UUID, status: String): Unit = newSuspendedTransaction {
        StampTradesTable.update({ StampTradesTable.id eq tradeId }) {
            it[StampTradesTable.status] = status
        }
    }

    private fun ResultRow.toStamp() = UserStamp(
        id = this[UserStampsTable.id],
        userId = this[UserStampsTable.userId],
        toiletId = this[UserStampsTable.toiletId],
        obtainedAt = this[UserStampsTable.obtainedAt],
    )

    private fun ResultRow.toTrade() = StampTrade(
        id = this[StampTradesTable.id],
        senderId = this[StampTradesTable.senderId],
        receiverId = this[StampTradesTable.receiverId],
        senderStampId = this[StampTradesTable.senderStampId],
        receiverStampId = this[StampTradesTable.receiverStampId],
        status = TradeStatus.valueOf(this[StampTradesTable.status].uppercase()),
        createdAt = this[StampTradesTable.createdAt],
    )
}
