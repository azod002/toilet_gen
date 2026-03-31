package com.toiletgen.gateway.config

data class ServiceConfig(
    val identityUrl: String = System.getenv("IDENTITY_SERVICE_URL") ?: "http://localhost:8081",
    val toiletUrl: String = System.getenv("TOILET_SERVICE_URL") ?: "http://localhost:8082",
    val sosUrl: String = System.getenv("SOS_SERVICE_URL") ?: "http://localhost:8083",
    val gamificationUrl: String = System.getenv("GAMIFICATION_SERVICE_URL") ?: "http://localhost:8084",
)
