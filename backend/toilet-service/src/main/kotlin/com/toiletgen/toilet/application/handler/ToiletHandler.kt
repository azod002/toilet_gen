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
import java.util.UUID

class ToiletHandler(
    private val toiletRepository: ToiletRepository,
    private val reviewRepository: ReviewRepository,
    private val eventPublisher: ToiletEventPublisher,
    private val visitRepository: VisitRepository,
) {
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
        val saved = toiletRepository.create(toilet)
        eventPublisher.toiletCreated(saved)
        return saved
    }

    suspend fun getNearby(query: GetNearbyToiletsQuery): List<Toilet> =
        toiletRepository.findNearby(query.lat, query.lon, query.radiusKm)

    suspend fun getById(query: GetToiletByIdQuery): Toilet =
        toiletRepository.findById(UUID.fromString(query.id))
            ?: throw IllegalArgumentException("Туалет не найден")

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
        val saved = reviewRepository.create(review)

        // Recalculate rating
        val avgRating = reviewRepository.avgRatingByToiletId(toiletId)
        val avgCleanliness = reviewRepository.avgCleanlinessByToiletId(toiletId)
        val count = reviewRepository.countByToiletId(toiletId)
        toiletRepository.updateRating(toiletId, avgRating, avgCleanliness, count)

        eventPublisher.reviewAdded(saved)
        eventPublisher.ratingUpdated(cmd.toiletId, avgRating)

        return saved
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

    suspend fun visitToilet(toiletId: String, userId: String) {
        val id = UUID.fromString(toiletId)
        toiletRepository.findById(id) ?: throw IllegalArgumentException("Туалет не найден")
        visitRepository.create(Visit(userId = UUID.fromString(userId), toiletId = id))
        eventPublisher.toiletVisited(toiletId, userId)
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
