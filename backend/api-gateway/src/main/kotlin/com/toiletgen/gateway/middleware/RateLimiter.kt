package com.toiletgen.gateway.middleware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class RateLimiter(
    private val maxRequests: Int = 100,
    private val windowMs: Long = 60_000,
) {
    private val requests = ConcurrentHashMap<String, Pair<AtomicInteger, AtomicLong>>()

    suspend fun check(call: ApplicationCall): Boolean {
        val ip = call.request.local.remoteAddress
        val now = System.currentTimeMillis()
        val (count, windowStart) = requests.getOrPut(ip) {
            AtomicInteger(0) to AtomicLong(now)
        }

        if (now - windowStart.get() > windowMs) {
            count.set(0)
            windowStart.set(now)
        }

        return if (count.incrementAndGet() > maxRequests) {
            call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "Слишком много запросов"))
            false
        } else true
    }
}
