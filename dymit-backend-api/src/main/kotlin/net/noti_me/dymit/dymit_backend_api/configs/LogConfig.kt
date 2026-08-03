package net.noti_me.dymit.dymit_backend_api.configs

import com.fasterxml.jackson.databind.ObjectMapper
import net.noti_me.dymit.dymit_backend_api.common.logging.LogReportFilter
import net.noti_me.dymit.dymit_backend_api.common.logging.LogReporter
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordMessageReporter
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordWebhookTransport
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures conditional API error reporting.
 */
@Configuration
class LogConfig {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates the configured Discord API error reporter.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "log-reporter",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun messageReporter(
        objectMapper: ObjectMapper,
        transport: DiscordWebhookTransport,
        @Value("\${log-reporter.discord.webhook.url:}")
        discordWebhookUrl: String
    ): LogReporter {
        logger.info("LogReporter is enabled with Discord webhook URL: $discordWebhookUrl")
        return DiscordMessageReporter(
            objectMapper = objectMapper,
            transport = transport,
            discordWebhookUrl = discordWebhookUrl
        )
    }

    /**
     * Creates the servlet filter when an error reporter is enabled.
     */
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
