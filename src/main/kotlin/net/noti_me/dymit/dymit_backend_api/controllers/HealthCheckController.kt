package net.noti_me.dymit.dymit_backend_api.controllers

import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordQuartzLogger
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController(
    private val discordQuartzLogger: DiscordQuartzLogger,
    private val loadMemberPort: LoadMemberPort
) {

    private val logger = LoggerFactory.getLogger(HealthCheckController::class.java)

    @GetMapping("health-check")
    fun healthCheck(): Long {
        logger.error("Health check endpoint was called")
        return System.currentTimeMillis()
    }


/*     @GetMapping("api/v1/test/discord/quartz")
    fun testDiscordQuartzLogging(): String {
        discordQuartzLogger.log("Test Quartz Log", "This is a test log message from HealthCheckController.")
        return "Discord Quartz logging test messages have been sent."
    } */
}

