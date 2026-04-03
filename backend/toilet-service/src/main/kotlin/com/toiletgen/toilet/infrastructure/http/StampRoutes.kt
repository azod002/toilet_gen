package com.toiletgen.toilet.infrastructure.http

import com.toiletgen.toilet.domain.model.StampTrade
import com.toiletgen.toilet.domain.model.TradeStatus
import com.toiletgen.toilet.domain.repository.StampRepository
import com.toiletgen.toilet.domain.repository.ToiletRepository
import com.toiletgen.shared.security.userId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Serializable
data class StampResponse(
    val id: String,
    val toiletId: String,
    val toiletName: String,
    val toiletType: String,
    val latitude: Double,
    val longitude: Double,
    val obtainedAt: Long,
)

@Serializable
data class TradeRequest(
    val receiverId: String,
    val myStampId: String,
    val theirStampId: String,
)

@Serializable
data class TradeResponse(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val senderStampId: String,
    val receiverStampId: String,
    val status: String,
    val createdAt: Long,
)

private val STAMP_COOLDOWN: Duration = Duration.ofHours(4)

fun Route.stampRoutes() {
    val stampRepo by application.inject<StampRepository>()
    val toiletRepo by application.inject<ToiletRepository>()

    authenticate("auth-jwt") {
        // Collect stamp from a toilet
        post("/api/v1/toilets/{id}/collect-stamp") {
            val toiletId = UUID.fromString(call.parameters["id"]!!)
            val userId = UUID.fromString(call.userId)

            toiletRepo.findById(toiletId) ?: throw IllegalArgumentException("Туалет не найден")

            val lastTime = stampRepo.getLastStampTime(userId, toiletId)
            if (lastTime != null && Duration.between(lastTime, Instant.now()) < STAMP_COOLDOWN) {
                val remaining = STAMP_COOLDOWN.minus(Duration.between(lastTime, Instant.now()))
                val minutes = remaining.toMinutes()
                @Serializable
                data class CooldownResponse(val error: String, val retryAfterSeconds: Long)
                call.respond(HttpStatusCode.TooManyRequests, CooldownResponse(
                    error = "Следующую марку можно получить через ${minutes / 60}ч ${minutes % 60}мин",
                    retryAfterSeconds = remaining.seconds,
                ))
                return@post
            }

            val stamp = stampRepo.collectStamp(userId, toiletId)
            val toilet = toiletRepo.findById(toiletId)!!
            call.respond(HttpStatusCode.Created, StampResponse(
                id = stamp.id.toString(),
                toiletId = toiletId.toString(),
                toiletName = toilet.name,
                toiletType = toilet.type.name,
                latitude = toilet.latitude,
                longitude = toilet.longitude,
                obtainedAt = stamp.obtainedAt.toEpochMilli(),
            ))
        }

        // My stamp inventory
        get("/api/v1/stamps/me") {
            val userId = UUID.fromString(call.userId)
            val stamps = stampRepo.getUserStamps(userId)
            val response = stamps.map { stamp ->
                val toilet = toiletRepo.findById(stamp.toiletId)
                StampResponse(
                    id = stamp.id.toString(),
                    toiletId = stamp.toiletId.toString(),
                    toiletName = toilet?.name ?: "Удалённый туалет",
                    toiletType = toilet?.type?.name ?: "UNKNOWN",
                    latitude = toilet?.latitude ?: 0.0,
                    longitude = toilet?.longitude ?: 0.0,
                    obtainedAt = stamp.obtainedAt.toEpochMilli(),
                )
            }
            call.respond(response)
        }

        // Create trade offer
        post("/api/v1/stamps/trade") {
            val userId = UUID.fromString(call.userId)
            val req = call.receive<TradeRequest>()

            val myStamp = stampRepo.getStampById(UUID.fromString(req.myStampId))
                ?: throw IllegalArgumentException("Ваша марка не найдена")
            if (myStamp.userId != userId) throw IllegalArgumentException("Это не ваша марка")

            val theirStamp = stampRepo.getStampById(UUID.fromString(req.theirStampId))
                ?: throw IllegalArgumentException("Марка получателя не найдена")
            val receiverId = UUID.fromString(req.receiverId)
            if (theirStamp.userId != receiverId) throw IllegalArgumentException("Эта марка не принадлежит получателю")

            val trade = stampRepo.createTrade(StampTrade(
                senderId = userId,
                receiverId = receiverId,
                senderStampId = myStamp.id,
                receiverStampId = theirStamp.id,
            ))
            call.respond(HttpStatusCode.Created, trade.toResponse())
        }

        // My pending trades
        get("/api/v1/stamps/trades/me") {
            val userId = UUID.fromString(call.userId)
            val trades = stampRepo.getPendingTradesForUser(userId)
            call.respond(trades.map { it.toResponse() })
        }

        // Accept trade
        post("/api/v1/stamps/trade/{id}/accept") {
            val userId = UUID.fromString(call.userId)
            val tradeId = UUID.fromString(call.parameters["id"]!!)
            val trade = stampRepo.getTradeById(tradeId)
                ?: throw IllegalArgumentException("Обмен не найден")
            if (trade.receiverId != userId) throw IllegalArgumentException("Вы не участник этого обмена")
            if (trade.status != TradeStatus.PENDING) throw IllegalArgumentException("Обмен уже завершён")

            // Verify both stamps still belong to correct users
            val senderStamp = stampRepo.getStampById(trade.senderStampId)
            val receiverStamp = stampRepo.getStampById(trade.receiverStampId)
            if (senderStamp == null || senderStamp.userId != trade.senderId)
                throw IllegalArgumentException("Марка отправителя больше недоступна")
            if (receiverStamp == null || receiverStamp.userId != trade.receiverId)
                throw IllegalArgumentException("Ваша марка больше недоступна")

            // Swap ownership
            stampRepo.transferStamp(trade.senderStampId, trade.receiverId)
            stampRepo.transferStamp(trade.receiverStampId, trade.senderId)
            stampRepo.updateTradeStatus(tradeId, "accepted")

            call.respond(trade.copy(status = TradeStatus.ACCEPTED).toResponse())
        }

        // Decline trade
        post("/api/v1/stamps/trade/{id}/decline") {
            val userId = UUID.fromString(call.userId)
            val tradeId = UUID.fromString(call.parameters["id"]!!)
            val trade = stampRepo.getTradeById(tradeId)
                ?: throw IllegalArgumentException("Обмен не найден")
            if (trade.receiverId != userId && trade.senderId != userId)
                throw IllegalArgumentException("Вы не участник этого обмена")
            if (trade.status != TradeStatus.PENDING) throw IllegalArgumentException("Обмен уже завершён")

            stampRepo.updateTradeStatus(tradeId, "declined")
            call.respond(trade.copy(status = TradeStatus.DECLINED).toResponse())
        }
    }
}

private fun StampTrade.toResponse() = TradeResponse(
    id = id.toString(), senderId = senderId.toString(),
    receiverId = receiverId.toString(), senderStampId = senderStampId.toString(),
    receiverStampId = receiverStampId.toString(), status = status.name.lowercase(),
    createdAt = createdAt.toEpochMilli(),
)
