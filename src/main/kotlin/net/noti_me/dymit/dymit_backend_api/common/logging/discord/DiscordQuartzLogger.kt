package net.noti_me.dymit.dymit_backend_api.common.logging.discord

import com.fasterxml.jackson.databind.ObjectMapper
import net.noti_me.dymit.dymit_backend_api.configs.DiscordConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class DiscordQuartzLogger(
    private val config: DiscordConfig,
    private val om: ObjectMapper,
    private val webClient: WebClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun log(title: String, message: String) {
        val embed = Embed(title = title, description = message)
        val discordMessage = DiscordMessageDto(content = "", embeds = listOf(embed))
        logger.info("Sending Discord Quartz log: $title - $message")
        webClient.post()
            .uri(config.getQuartzUrl())
            .bodyValue(discordMessage)
            .retrieve()
            .bodyToMono(Void::class.java)
            .subscribe()
    }

    fun error(message: String) {
        val embed = Embed(title = "Error", description = message)
        val discordMessage = DiscordMessageDto(content = "", embeds = listOf(embed))
        logger.error("Sending Discord Quartz error log: $message")
        webClient.post()
            .uri(config.getQuartzUrl())
            .bodyValue(discordMessage)
            .retrieve()
            .bodyToMono(Void::class.java)
            .subscribe()
    }
}

