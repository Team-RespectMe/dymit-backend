package net.noti_me.dymit.dymit_backend_api.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfig(
    @Value("\${log-reporter.discord.webhook.url}")
    private val webhookUrl: String,
    @Value("\${log-reporter.discord.quartz.webhook.url}")
    private val quartzWebhookUrl: String
) {

    fun getUrl(): String {
        return webhookUrl
    }

    fun getQuartzUrl(): String {
        return quartzWebhookUrl
    }
}

