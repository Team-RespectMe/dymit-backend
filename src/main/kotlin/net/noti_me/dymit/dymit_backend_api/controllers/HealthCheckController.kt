package net.noti_me.dymit.dymit_backend_api.controllers

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController {

    private val logger = LoggerFactory.getLogger(HealthCheckController::class.java)

    @GetMapping("health-check")
    fun healthCheck(): Long {
        logger.error("Health check endpoint was called")
        return System.currentTimeMillis()
    }
}

