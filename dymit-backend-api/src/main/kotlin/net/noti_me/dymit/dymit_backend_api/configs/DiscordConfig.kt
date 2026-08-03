package net.noti_me.dymit.dymit_backend_api.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

/**
 * Provides the configured Discord webhook URLs for each independent caller.
 */
@Configuration
class DiscordConfig(
    @Value("\${log-reporter.discord.webhook.url}")
    private val webhookUrl: String,
    @Value("\${log-reporter.discord.quartz.webhook.url}")
    private val quartzWebhookUrl: String,
    @Value("\${discord.daily_statistics.webhook.url:}")
    private val dailyStatisticsWebhookUrl: String
) {

    /**
     * Returns the API error webhook URL.
     */
    fun getUrl(): String {
        return webhookUrl
    }

    /**
     * Returns the Quartz webhook URL.
     */
    fun getQuartzUrl(): String {
        return quartzWebhookUrl
    }

    /**
     * Returns the daily-statistics creation webhook URL.
     */
    fun getDailyStatisticsUrl(): String {
        return dailyStatisticsWebhookUrl
    }
}
