package com.toiletgen.toilet.infrastructure.persistence

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseSeeder {

    fun seedIfEmpty() {
        val count = transaction { ToiletsTable.selectAll().count() }
        if (count > 0L) {
            println("Toilets table already has $count records, skipping seed.")
            return
        }

        println("Toilets table is empty, seeding from OSM data...")
        val sql = this::class.java.classLoader
            .getResourceAsStream("seed_toilets.sql")
            ?.bufferedReader()
            ?.readText()

        if (sql == null) {
            println("seed_toilets.sql not found in resources, skipping seed.")
            return
        }

        transaction {
            exec(sql)
        }

        val newCount = transaction { ToiletsTable.selectAll().count() }
        println("Seeded $newCount toilets from OSM data.")
    }
}
