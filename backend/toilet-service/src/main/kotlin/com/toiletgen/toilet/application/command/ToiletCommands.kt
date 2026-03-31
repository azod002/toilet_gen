package com.toiletgen.toilet.application.command

data class CreateToiletCommand(
    val ownerId: String?,
    val name: String,
    val description: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isPaid: Boolean,
    val price: Double?,
    val hasToiletPaper: Boolean,
)

data class AddReviewCommand(
    val toiletId: String,
    val userId: String,
    val username: String,
    val rating: Int,
    val cleanlinessSmell: Int,
    val cleanlinessDirt: Int,
    val hasToiletPaper: Boolean,
    val comment: String,
)
