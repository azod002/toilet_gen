package com.toiletgen.toilet.application.handler

import com.toiletgen.toilet.application.command.AddReviewCommand
import com.toiletgen.toilet.application.command.CreateToiletCommand
import com.toiletgen.toilet.application.query.GetNearbyToiletsQuery
import com.toiletgen.toilet.application.query.GetReviewsByToiletQuery
import com.toiletgen.toilet.application.query.GetToiletByIdQuery
import com.toiletgen.toilet.domain.event.ToiletEventPublisher
import com.toiletgen.toilet.domain.model.Review
import com.toiletgen.toilet.domain.model.Toilet
import com.toiletgen.toilet.domain.model.ToiletType
import com.toiletgen.toilet.domain.model.Visit
import com.toiletgen.toilet.domain.repository.ReviewRepository
import com.toiletgen.toilet.domain.repository.ToiletRepository
import com.toiletgen.toilet.domain.repository.VisitRepository
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ToiletHandler(
    private val toiletRepository: ToiletRepository,
    private val reviewRepository: ReviewRepository,
    private val eventPublisher: ToiletEventPublisher,
    private val visitRepository: VisitRepository,
) {
    /**
     * Create toilet + publish event in a single transaction (Transactional Outbox).
     */
    suspend fun createToilet(cmd: CreateToiletCommand): Toilet {
        val toilet = Toilet(
            ownerId = cmd.ownerId?.let { UUID.fromString(it) },
            name = cmd.name,
            description = cmd.description,
            type = ToiletType.valueOf(cmd.type),
            latitude = cmd.latitude,
            longitude = cmd.longitude,
            isPaid = cmd.isPaid,
            price = cmd.price,
            hasToiletPaper = cmd.hasToiletPaper,
        )
        return newSuspendedTransaction {
            toiletRepository.createInTransaction(toilet)
            eventPublisher.toiletCreated(toilet)
            toilet
        }
    }

    suspend fun getNearby(query: GetNearbyToiletsQuery): List<Toilet> =
        toiletRepository.findNearby(query.lat, query.lon, query.radiusKm)

    suspend fun getById(query: GetToiletByIdQuery): Toilet =
        toiletRepository.findById(UUID.fromString(query.id))
            ?: throw IllegalArgumentException("Туалет не найден")

    /**
     * Add review + recalculate rating + publish events in a single transaction.
     */
    suspend fun addReview(cmd: AddReviewCommand): Review {
        val toiletId = UUID.fromString(cmd.toiletId)
        toiletRepository.findById(toiletId) ?: throw IllegalArgumentException("Туалет не найден")

        val review = Review(
            toiletId = toiletId,
            userId = UUID.fromString(cmd.userId),
            username = cmd.username,
            rating = cmd.rating,
            cleanlinessSmell = cmd.cleanlinessSmell,
            cleanlinessDirt = cmd.cleanlinessDirt,
            hasToiletPaper = cmd.hasToiletPaper,
            comment = cmd.comment,
        )

        return newSuspendedTransaction {
            val saved = reviewRepository.createInTransaction(review)

            // Recalculate rating
            val avgRating = reviewRepository.avgRatingByToiletIdInTransaction(toiletId)
            val avgCleanliness = reviewRepository.avgCleanlinessByToiletIdInTransaction(toiletId)
            val count = reviewRepository.countByToiletIdInTransaction(toiletId)
            toiletRepository.updateRatingInTransaction(toiletId, avgRating, avgCleanliness, count)

            // Outbox events (same transaction)
            eventPublisher.reviewAdded(saved)
            eventPublisher.ratingUpdated(cmd.toiletId, avgRating)

            saved
        }
    }

    suspend fun deleteToilet(toiletId: String, userId: String, role: String = "user") {
        val id = UUID.fromString(toiletId)
        val toilet = toiletRepository.findById(id) ?: throw IllegalArgumentException("Туалет не найден")
        if (role != "moderator" && toilet.ownerId?.toString() != userId) {
            throw IllegalArgumentException("Только создатель или модератор может удалить туалет")
        }
        visitRepository.deleteByToiletId(id)
        reviewRepository.deleteByToiletId(id)
        toiletRepository.delete(id)
    }

    /**
     * Visit toilet + publish event in a single transaction.
     */
    suspend fun visitToilet(toiletId: String, userId: String) {
        val id = UUID.fromString(toiletId)
        toiletRepository.findById(id) ?: throw IllegalArgumentException("Туалет не найден")
        newSuspendedTransaction {
            visitRepository.createInTransaction(Visit(userId = UUID.fromString(userId), toiletId = id))
            eventPublisher.toiletVisited(toiletId, userId)
        }
    }

    suspend fun getUserVisits(userId: String): List<Pair<Visit, Toilet?>> {
        val visits = visitRepository.findByUserId(UUID.fromString(userId))
        return visits.map { visit ->
            visit to toiletRepository.findById(visit.toiletId)
        }
    }

    suspend fun getReviews(query: GetReviewsByToiletQuery): List<Review> =
        reviewRepository.findByToiletId(UUID.fromString(query.toiletId))
}
