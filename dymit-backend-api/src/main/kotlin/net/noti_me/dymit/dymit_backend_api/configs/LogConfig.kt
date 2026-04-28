package net.noti_me.dymit.dymit_backend_api.configs

import com.fasterxml.jackson.databind.ObjectMapper
import net.noti_me.dymit.dymit_backend_api.common.logging.LogReportFilter
import net.noti_me.dymit.dymit_backend_api.common.logging.LogReporter
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordMessageReporter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class LogConfig {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnProperty(
        prefix = "log-reporter",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun messageReporter(
        objectMapper: ObjectMapper,
        webClient: WebClient,
        @Value("\${log-reporter.discord.webhook.url:}")
        discordWebhookUrl: String
    ): LogReporter {
        logger.info("LogReporter is enabled with Discord webhook URL: $discordWebhookUrl")
        return DiscordMessageReporter(
            objectMapper = objectMapper,
            webClient = webClient,
            discordWebhookUrl = discordWebhookUrl
        )
    }

    @Bean
    @ConditionalOnBean(LogReporter::class)
    fun logReportFilter(
        logReporter: LogReporter
    ) : LogReportFilter {
        logger.info("LogReporter exists, creating LogReportFilter")
        return LogReportFilter(
            logReporter = logReporter
        )
    }
}