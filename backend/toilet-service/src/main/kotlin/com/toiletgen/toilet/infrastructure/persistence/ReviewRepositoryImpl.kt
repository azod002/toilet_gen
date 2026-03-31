package com.toiletgen.toilet.infrastructure.persistence

import com.toiletgen.toilet.domain.model.Review
import com.toiletgen.toilet.domain.repository.ReviewRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ReviewRepositoryImpl : ReviewRepository {

    private fun ResultRow.toReview() = Review(
        id = this[ReviewsTable.id],
        toiletId = this[ReviewsTable.toiletId],
        userId = this[ReviewsTable.userId],
        username = this[ReviewsTable.username],
        rating = this[ReviewsTable.rating],
        cleanlinessSmell = this[ReviewsTable.cleanlinessSmell],
        cleanlinessDirt = this[ReviewsTable.cleanlinessDirt],
        hasToiletPaper = this[ReviewsTable.hasToiletPaper],
        comment = this[ReviewsTable.comment],
        createdAt = this[ReviewsTable.createdAt],
    )

    override suspend fun findByToiletId(toiletId: UUID): List<Review> = newSuspendedTransaction {
        ReviewsTable.selectAll().where { ReviewsTable.toiletId eq toiletId }
            .orderBy(ReviewsTable.createdAt, SortOrder.DESC)
            .map { it.toReview() }
    }

    override suspend fun create(review: Review): Review = newSuspendedTransaction {
        ReviewsTable.insert {
            it[id] = review.id
            it[toiletId] = review.toiletId
            it[userId] = review.userId
            it[username] = review.username
            it[rating] = review.rating
            it[cleanlinessSmell] = review.cleanlinessSmell
            it[cleanlinessDirt] = review.cleanlinessDirt
            it[hasToiletPaper] = review.hasToiletPaper
            it[comment] = review.comment
            it[createdAt] = review.createdAt
        }
        review
    }

    override suspend fun countByToiletId(toiletId: UUID): Int = newSuspendedTransaction {
        ReviewsTable.selectAll().where { ReviewsTable.toiletId eq toiletId }.count().toInt()
    }

    override suspend fun avgRatingByToiletId(toiletId: UUID): Double = newSuspendedTransaction {
        ReviewsTable.select(ReviewsTable.rating.avg())
            .where { ReviewsTable.toiletId eq toiletId }
            .singleOrNull()
            ?.get(ReviewsTable.rating.avg())?.toDouble() ?: 0.0
    }

    override suspend fun avgCleanlinessByToiletId(toiletId: UUID): Double = newSuspendedTransaction {
        val smell = ReviewsTable.select(ReviewsTable.cleanlinessSmell.avg())
            .where { ReviewsTable.toiletId eq toiletId }
            .singleOrNull()?.get(ReviewsTable.cleanlinessSmell.avg())?.toDouble() ?: 0.0
        val dirt = ReviewsTable.select(ReviewsTable.cleanlinessDirt.avg())
            .where { ReviewsTable.toiletId eq toiletId }
            .singleOrNull()?.get(ReviewsTable.cleanlinessDirt.avg())?.toDouble() ?: 0.0
        (smell + dirt) / 2.0
    }

    override suspend fun deleteByToiletId(toiletId: UUID) = newSuspendedTransaction {
        ReviewsTable.deleteWhere { Op.build { ReviewsTable.toiletId eq toiletId } }
        Unit
    }
}
