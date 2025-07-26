package net.noti_me.dymit.dymit_backend_api.configs

import com.fasterxml.jackson.databind.ObjectMapper
import net.noti_me.dymit.dymit_backend_api.common.logging.LogReporter
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordMessageReporter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class LogConfig {

    @Bean
    @ConditionalOnProperty(
        prefix = "log-reporter",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @ConditionalOnProperty(
        prefix = "log-reporter",
        name = ["discord.webhook.url"],
    )
    fun messageReporter(
        objectMapper: ObjectMapper,
        webClient: WebClient,
        @Value("\${discord.webhook.url}")
        discordWebhookUrl: String
    ): LogReporter {
        return DiscordMessageReporter(
            objectMapper = objectMapper,
            webClient = webClient,
            discordWebhookUrl = discordWebhookUrl
        )
    }
}