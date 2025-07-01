package net.noti_me.dymit.dymit_backend_api.application.auth.oidc

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.stereotype.Service

@Service
class GoogleJwtksProvider(
    webClient: WebClient,
    objectMapper: ObjectMapper
) : AbstractJwksProvider(webClient, objectMapper, "google") {
    override val jwksUrl: String = "https://www.googleapis.com/oauth2/v3/certs"
}