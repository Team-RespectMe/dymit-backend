package net.noti_me.dymit.dymit_backend_api.common.logging.discord

import com.fasterxml.jackson.databind.ObjectMapper
import net.noti_me.dymit.dymit_backend_api.common.logging.LogReporter
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DiscordMessageReporter(
    private val objectMapper: ObjectMapper,
    private val webClient : WebClient,
    private val discordWebhookUrl: String
) : LogReporter {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)

    override fun send(request: ContentCachingRequestWrapper, response: ContentCachingResponseWrapper) {
        val time = Instant.now()
        val embeds = listOf(
            Embed(
                title = "Dymit API Error Information",
                description = """
### :alarm_clock:Error Time  
                    ${formatter.format(time)}
### :inbox_tray:Request
                    - Content Type: ${request.contentType}  
                    - Method: ${request.method}  
                    - URI: ${request.requestURI}  
                    - Remote Address: ${request.remoteAddr}  
                    - User Agent: ${request.getHeader("User-Agent")}
                    - Authorization: ${request.getHeader("Authorization")}
                    - Content-Type: ${request.contentType}
                    - Request Body : ${if (request.contentType?.startsWith("multipart/form-data") == true)
                        request.parameterMap.keys.joinToString(", ") { it }
                            .chunked(40)
                            .joinToString("\n")
                    else
                        request.contentAsByteArray.toString(Charsets.UTF_8)
                            .chunked(40)
                            .joinToString("\n")}
                    - Parameters: ${request.queryString}
### :outbox_tray:Response
                    - Status: ${response.status}
                    - Response Body: ${response.contentAsByteArray.toString(Charsets.UTF_8)}
                   """.trimIndent())
        )

        val dto = DiscordMessageDto("Error Log", embeds)
        logger.debug(objectMapper.writeValueAsString(dto))
        webClient
            .post()
            .uri(discordWebhookUrl)
            .bodyValue(dto)
            .retrieve()
            .bodyToMono(Void::class.java)
            .doOnError { error -> logger.error("Failed to send Discord message: ${error.message}") }
            .subscribe()
    }
}