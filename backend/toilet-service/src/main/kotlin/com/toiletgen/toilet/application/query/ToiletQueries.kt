package com.toiletgen.toilet.application.query

data class GetNearbyToiletsQuery(val lat: Double, val lon: Double, val radiusKm: Double = 1.0)
data class GetToiletByIdQuery(val id: String)
data class GetReviewsByToiletQuery(val toiletId: String)
