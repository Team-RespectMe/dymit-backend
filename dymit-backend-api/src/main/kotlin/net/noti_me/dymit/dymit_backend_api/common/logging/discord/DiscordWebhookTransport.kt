package net.noti_me.dymit.dymit_backend_api.common.logging.discord

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * Sends Discord message payloads to a supplied webhook URL.
 */
@Component
class DiscordWebhookTransport(
    private val webClient: WebClient
) {

    /**
     * Sends the message and returns a publisher representing the HTTP exchange.
     */
    fun send(webhookUrl: String, message: DiscordMessageDto): Mono<Void> {
        return webClient.post()
            .uri(webhookUrl)
            .bodyValue(message)
            .retrieve()
            .bodyToMono(Void::class.java)
    }
}
