package com.toiletgen.toilet.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object ToiletsTable : Table("toilets") {
    val id = uuid("id")
    val ownerId = uuid("owner_id").nullable()
    val name = varchar("name", 200)
    val description = text("description").default("")
    val type = varchar("type", 50)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val isPaid = bool("is_paid").default(false)
    val price = double("price").nullable()
    val hasToiletPaper = bool("has_toilet_paper").default(true)
    val avgRating = double("avg_rating").default(0.0)
    val avgCleanliness = double("avg_cleanliness").default(0.0)
    val reviewCount = integer("review_count").default(0)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object VisitsTable : Table("visits") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val toiletId = uuid("toilet_id").references(ToiletsTable.id)
    val visitedAt = timestamp("visited_at")
    override val primaryKey = PrimaryKey(id)
}

object BooksTable : Table("books") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val username = varchar("username", 100)
    val title = varchar("title", 300)
    val author = varchar("author", 200)
    val fileSize = long("file_size")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ChatMessagesTable : Table("chat_messages") {
    val id = uuid("id")
    val senderId = uuid("sender_id")
    val senderUsername = varchar("sender_username", 100)
    val text = text("text")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object PrivateMessagesTable : Table("private_messages") {
    val id = uuid("id")
    val senderId = uuid("sender_id")
    val senderUsername = varchar("sender_username", 100)
    val receiverId = uuid("receiver_id")
    val receiverUsername = varchar("receiver_username", 100)
    val text = text("text")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ForumThreadsTable : Table("forum_threads") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val username = varchar("username", 100)
    val title = varchar("title", 300)
    val text = text("text")
    val imageUrl = varchar("image_url", 500).nullable()
    val replyCount = integer("reply_count").default(0)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ForumRepliesTable : Table("forum_replies") {
    val id = uuid("id")
    val threadId = uuid("thread_id").references(ForumThreadsTable.id)
    val userId = uuid("user_id")
    val username = varchar("username", 100)
    val text = text("text")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ReportsTable : Table("reports") {
    val id = uuid("id")
    val reporterId = uuid("reporter_id")
    val reporterUsername = varchar("reporter_username", 100)
    val contentType = varchar("content_type", 50) // chat_message, private_message, forum_thread, forum_reply
    val contentId = uuid("content_id")
    val reason = varchar("reason", 500)
    val status = varchar("status", 20).default("pending") // pending, reviewed, dismissed
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ReviewsTable : Table("reviews") {
    val id = uuid("id")
    val toiletId = uuid("toilet_id").references(ToiletsTable.id)
    val userId = uuid("user_id")
    val username = varchar("username", 100)
    val rating = integer("rating")
    val cleanlinessSmell = integer("cleanliness_smell")
    val cleanlinessDirt = integer("cleanliness_dirt")
    val hasToiletPaper = bool("has_toilet_paper")
    val comment = text("comment")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}
