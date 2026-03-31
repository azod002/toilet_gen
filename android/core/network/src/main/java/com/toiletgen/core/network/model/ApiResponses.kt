package com.toiletgen.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val role: String = "user",
)

@Serializable
data class ToiletResponse(
    val id: String,
    val ownerId: String? = null,
    val name: String,
    val description: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isPaid: Boolean,
    val price: Double? = null,
    val hasToiletPaper: Boolean,
    val avgRating: Double,
    val avgCleanliness: Double,
    val reviewCount: Int,
    val createdAt: Long,
)

@Serializable
data class ReviewResponse(
    val id: String,
    val toiletId: String,
    val userId: String,
    val username: String,
    val rating: Int,
    val cleanlinessSmell: Int,
    val cleanlinessDirt: Int,
    val hasToiletPaper: Boolean,
    val comment: String,
    val createdAt: Long,
)

@Serializable
data class SosRequestResponse(
    val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val matchedToiletId: String? = null,
    val createdAt: Long,
)

@Serializable
data class AchievementResponse(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null,
)

@Serializable
data class VisitResponse(
    val id: String,
    val toiletId: String,
    val toiletName: String,
    val toiletType: String,
    val latitude: Double,
    val longitude: Double,
    val visitedAt: Long,
)

@Serializable
data class BookResponse(
    val id: String,
    val userId: String,
    val username: String,
    val title: String,
    val author: String,
    val fileSize: Long,
    val createdAt: Long,
)

@Serializable
data class ChatMessageResponse(
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class PrivateMessageResponse(
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val receiverId: String,
    val receiverUsername: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class ConversationResponse(
    val userId: String,
    val username: String,
    val lastMessage: String,
    val lastMessageAt: Long,
)

@Serializable
data class SendMessageRequest(val text: String)

@Serializable
data class ThreadResponse(
    val id: String,
    val userId: String,
    val username: String,
    val title: String,
    val text: String,
    val replyCount: Int,
    val createdAt: Long,
)

@Serializable
data class ReplyResponse(
    val id: String,
    val threadId: String,
    val userId: String,
    val username: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class ThreadDetailResponse(
    val thread: ThreadResponse,
    val replies: List<ReplyResponse>,
)

@Serializable
data class CreateThreadRequest(val title: String, val text: String)

@Serializable
data class CreateReplyRequest(val text: String)

@Serializable
data class CreateReportRequest(
    val contentType: String,
    val contentId: String,
    val reason: String,
)

@Serializable
data class ReportResponse(
    val id: String,
    val contentType: String,
    val contentId: String,
    val reason: String,
    val status: String,
)

@Serializable
data class YearlyReportResponse(
    val year: Int,
    val toiletsVisited: Int,
    val reviewsWritten: Int,
    val sosSent: Int,
    val sosHelped: Int,
    val toiletsCreated: Int,
    val favoriteToilet: ToiletResponse? = null,
    val topAchievements: List<AchievementResponse> = emptyList(),
)
