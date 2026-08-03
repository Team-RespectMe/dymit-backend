package net.noti_me.dymit.dymit_backend_api.common.logging.discord

import net.noti_me.dymit.dymit_backend_api.configs.DiscordConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Formats and sends existing Quartz log messages to Discord.
 */
@Component
class DiscordQuartzLogger(
    private val config: DiscordConfig,
    private val transport: DiscordWebhookTransport
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Sends an informational Quartz log using the existing payload format.
     */
    fun log(title: String, message: String) {
        val embed = Embed(title = title, description = message)
        val discordMessage = DiscordMessageDto(content = "", embeds = listOf(embed))
        logger.info("Sending Discord Quartz log: $title - $message")
        transport.send(config.getQuartzUrl(), discordMessage).subscribe()
    }

    /**
     * Sends an error Quartz log using the existing payload format.
     */
    fun error(message: String) {
        val embed = Embed(title = "Error", description = message)
        val discordMessage = DiscordMessageDto(content = "", embeds = listOf(embed))
        logger.error("Sending Discord Quartz error log: $message")
        transport.send(config.getQuartzUrl(), discordMessage).subscribe()
    }
}
