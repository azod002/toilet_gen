package com.toiletgen.toilet.infrastructure.http

import com.toiletgen.toilet.application.command.AddReviewCommand
import com.toiletgen.toilet.application.command.CreateToiletCommand
import com.toiletgen.toilet.application.handler.ToiletHandler
import com.toiletgen.toilet.application.query.GetNearbyToiletsQuery
import com.toiletgen.toilet.application.query.GetReviewsByToiletQuery
import com.toiletgen.toilet.application.query.GetToiletByIdQuery
import com.toiletgen.shared.security.role
import com.toiletgen.shared.security.userId
import com.toiletgen.shared.security.username
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class CreateToiletRequest(
    val name: String, val description: String = "", val type: String,
    val latitude: Double, val longitude: Double, val isPaid: Boolean = false,
    val price: Double? = null, val hasToiletPaper: Boolean = true,
)

@Serializable
data class CreateReviewRequest(
    val rating: Int, val cleanlinessSmell: Int, val cleanlinessDirt: Int,
    val hasToiletPaper: Boolean, val comment: String,
)

@Serializable
data class VisitResponse(
    val id: String, val toiletId: String, val toiletName: String,
    val toiletType: String, val latitude: Double, val longitude: Double,
    val visitedAt: Long,
)

@Serializable
data class ToiletResponse(
    val id: String, val ownerId: String?, val name: String, val description: String,
    val type: String, val latitude: Double, val longitude: Double, val isPaid: Boolean,
    val price: Double?, val hasToiletPaper: Boolean, val avgRating: Double,
    val avgCleanliness: Double, val reviewCount: Int, val createdAt: Long,
)

@Serializable
data class ReviewResponse(
    val id: String, val toiletId: String, val userId: String, val username: String,
    val rating: Int, val cleanlinessSmell: Int, val cleanlinessDirt: Int,
    val hasToiletPaper: Boolean, val comment: String, val createdAt: Long,
)

fun Route.toiletRoutes() {
    val handler by application.inject<ToiletHandler>()

    route("/api/v1/toilets") {
        get {
            val lat = call.parameters["lat"]?.toDoubleOrNull() ?: throw IllegalArgumentException("lat обязателен")
            val lon = call.parameters["lon"]?.toDoubleOrNull() ?: throw IllegalArgumentException("lon обязателен")
            val radius = call.parameters["radius"]?.toDoubleOrNull() ?: 1.0
            val toilets = handler.getNearby(GetNearbyToiletsQuery(lat, lon, radius))
            call.respond(toilets.map { it.toResponse() })
        }

        get("/{id}") {
            val id = call.parameters["id"]!!
            val toilet = handler.getById(GetToiletByIdQuery(id))
            call.respond(toilet.toResponse())
        }

        authenticate("auth-jwt") {
            post {
                val req = call.receive<CreateToiletRequest>()
                val toilet = handler.createToilet(CreateToiletCommand(
                    ownerId = call.userId, name = req.name, description = req.description,
                    type = req.type, latitude = req.latitude, longitude = req.longitude,
                    isPaid = req.isPaid, price = req.price, hasToiletPaper = req.hasToiletPaper,
                ))
                call.respond(HttpStatusCode.Created, toilet.toResponse())
            }
        }

        get("/{id}/reviews") {
            val toiletId = call.parameters["id"]!!
            val reviews = handler.getReviews(GetReviewsByToiletQuery(toiletId))
            call.respond(reviews.map { it.toResponse() })
        }

        authenticate("auth-jwt") {
            delete("/{id}") {
                val toiletId = call.parameters["id"]!!
                handler.deleteToilet(toiletId, call.userId, call.role)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        authenticate("auth-jwt") {
            post("/{id}/visit") {
                val toiletId = call.parameters["id"]!!
                handler.visitToilet(toiletId, call.userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        authenticate("auth-jwt") {
            get("/visits/me") {
                val visits = handler.getUserVisits(call.userId)
                call.respond(visits.map { (visit, toilet) ->
                    VisitResponse(
                        id = visit.id.toString(),
                        toiletId = visit.toiletId.toString(),
                        toiletName = toilet?.name ?: "Удалённый туалет",
                        toiletType = toilet?.type?.name ?: "UNKNOWN",
                        latitude = toilet?.latitude ?: 0.0,
                        longitude = toilet?.longitude ?: 0.0,
                        visitedAt = visit.visitedAt.toEpochMilli(),
                    )
                })
            }
        }

        authenticate("auth-jwt") {
            post("/{id}/reviews") {
                val toiletId = call.parameters["id"]!!
                val req = call.receive<CreateReviewRequest>()
                val review = handler.addReview(AddReviewCommand(
                    toiletId = toiletId, userId = call.userId, username = call.username,
                    rating = req.rating, cleanlinessSmell = req.cleanlinessSmell,
                    cleanlinessDirt = req.cleanlinessDirt, hasToiletPaper = req.hasToiletPaper,
                    comment = req.comment,
                ))
                call.respond(HttpStatusCode.Created, review.toResponse())
            }
        }
    }
}

private fun com.toiletgen.toilet.domain.model.Toilet.toResponse() = ToiletResponse(
    id = id.toString(), ownerId = ownerId?.toString(), name = name, description = description,
    type = type.name, latitude = latitude, longitude = longitude, isPaid = isPaid,
    price = price, hasToiletPaper = hasToiletPaper, avgRating = avgRating,
    avgCleanliness = avgCleanliness, reviewCount = reviewCount, createdAt = createdAt.toEpochMilli(),
)

private fun com.toiletgen.toilet.domain.model.Review.toResponse() = ReviewResponse(
    id = id.toString(), toiletId = toiletId.toString(), userId = userId.toString(),
    username = username, rating = rating, cleanlinessSmell = cleanlinessSmell,
    cleanlinessDirt = cleanlinessDirt, hasToiletPaper = hasToiletPaper,
    comment = comment, createdAt = createdAt.toEpochMilli(),
)
