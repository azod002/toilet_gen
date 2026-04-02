package com.toiletgen.toilet.infrastructure.persistence

import com.toiletgen.toilet.domain.model.Toilet
import com.toiletgen.toilet.domain.model.ToiletType
import com.toiletgen.toilet.domain.repository.ToiletRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.math.cos

class ToiletRepositoryImpl : ToiletRepository {

    private fun ResultRow.toToilet() = Toilet(
        id = this[ToiletsTable.id],
        ownerId = this[ToiletsTable.ownerId],
        name = this[ToiletsTable.name],
        description = this[ToiletsTable.description],
        type = ToiletType.valueOf(this[ToiletsTable.type]),
        latitude = this[ToiletsTable.latitude],
        longitude = this[ToiletsTable.longitude],
        isPaid = this[ToiletsTable.isPaid],
        price = this[ToiletsTable.price],
        hasToiletPaper = this[ToiletsTable.hasToiletPaper],
        avgRating = this[ToiletsTable.avgRating],
        avgCleanliness = this[ToiletsTable.avgCleanliness],
        reviewCount = this[ToiletsTable.reviewCount],
        createdAt = this[ToiletsTable.createdAt],
    )

    override suspend fun findById(id: UUID): Toilet? = newSuspendedTransaction {
        ToiletsTable.selectAll().where { ToiletsTable.id eq id }.singleOrNull()?.toToilet()
    }

    override suspend fun findNearby(lat: Double, lon: Double, radiusKm: Double): List<Toilet> = newSuspendedTransaction {
        // Approximate bounding box (1 degree lat ~ 111km)
        val latDelta = radiusKm / 111.0
        val lonDelta = radiusKm / (111.0 * cos(Math.toRadians(lat)))

        ToiletsTable.selectAll().where {
            (ToiletsTable.latitude greaterEq (lat - latDelta)) and
            (ToiletsTable.latitude lessEq (lat + latDelta)) and
            (ToiletsTable.longitude greaterEq (lon - lonDelta)) and
            (ToiletsTable.longitude lessEq (lon + lonDelta))
        }.map { it.toToilet() }
    }

    override suspend fun create(toilet: Toilet): Toilet = newSuspendedTransaction {
        ToiletsTable.insert {
            it[id] = toilet.id
            it[ownerId] = toilet.ownerId
            it[name] = toilet.name
            it[description] = toilet.description
            it[type] = toilet.type.name
            it[latitude] = toilet.latitude
            it[longitude] = toilet.longitude
            it[isPaid] = toilet.isPaid
            it[price] = toilet.price
            it[hasToiletPaper] = toilet.hasToiletPaper
            it[avgRating] = toilet.avgRating
            it[avgCleanliness] = toilet.avgCleanliness
            it[reviewCount] = toilet.reviewCount
            it[createdAt] = toilet.createdAt
        }
        toilet
    }

    override suspend fun update(toilet: Toilet): Toilet = newSuspendedTransaction {
        ToiletsTable.update({ ToiletsTable.id eq toilet.id }) {
            it[name] = toilet.name
            it[description] = toilet.description
            it[type] = toilet.type.name
            it[isPaid] = toilet.isPaid
            it[price] = toilet.price
            it[hasToiletPaper] = toilet.hasToiletPaper
        }
        toilet
    }

    override suspend fun updateRating(toiletId: UUID, avgRating: Double, avgCleanliness: Double, reviewCount: Int) = newSuspendedTransaction {
        ToiletsTable.update({ ToiletsTable.id eq toiletId }) {
            it[ToiletsTable.avgRating] = avgRating
            it[ToiletsTable.avgCleanliness] = avgCleanliness
            it[ToiletsTable.reviewCount] = reviewCount
        }
        Unit
    }

    override suspend fun delete(id: UUID) = newSuspendedTransaction {
        ToiletsTable.deleteWhere { Op.build { ToiletsTable.id eq id } }
        Unit
    }

    override fun createInTransaction(toilet: Toilet): Toilet {
        ToiletsTable.insert {
            it[id] = toilet.id
            it[ownerId] = toilet.ownerId
            it[name] = toilet.name
            it[description] = toilet.description
            it[type] = toilet.type.name
            it[latitude] = toilet.latitude
            it[longitude] = toilet.longitude
            it[isPaid] = toilet.isPaid
            it[price] = toilet.price
            it[hasToiletPaper] = toilet.hasToiletPaper
            it[avgRating] = toilet.avgRating
            it[avgCleanliness] = toilet.avgCleanliness
            it[reviewCount] = toilet.reviewCount
            it[createdAt] = toilet.createdAt
        }
        return toilet
    }

    override fun updateRatingInTransaction(toiletId: UUID, avgRating: Double, avgCleanliness: Double, reviewCount: Int) {
        ToiletsTable.update({ ToiletsTable.id eq toiletId }) {
            it[ToiletsTable.avgRating] = avgRating
            it[ToiletsTable.avgCleanliness] = avgCleanliness
            it[ToiletsTable.reviewCount] = reviewCount
        }
    }
}
