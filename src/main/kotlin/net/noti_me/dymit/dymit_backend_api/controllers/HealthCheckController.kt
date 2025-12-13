package net.noti_me.dymit.dymit_backend_api.controllers

import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordQuartzLogger
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.PermitAll

@RestController
class HealthCheckController(
    private val discordQuartzLogger: DiscordQuartzLogger,
    private val loadMemberPort: LoadMemberPort
) {

    private val logger = LoggerFactory.getLogger(HealthCheckController::class.java)

    @GetMapping("/api/v1/health-check")
    @PermitAll
    fun healthCheck(): Long {
        return System.currentTimeMillis()
    }


/*     @GetMapping("api/v1/test/discord/quartz")
    fun testDiscordQuartzLogging(): String {
        discordQuartzLogger.log("Test Quartz Log", "This is a test log message from HealthCheckController.")
        return "Discord Quartz logging test messages have been sent."
    } */
}

