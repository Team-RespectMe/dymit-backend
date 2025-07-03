package net.noti_me.dymit.dymit_backend_api.application.auth.oidc

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.stereotype.Service

@Component
class GoogleJwksProvider(
    webClient: WebClient,
) : AbstractJwksProvider(webClient) {
    override val jwksUrl: String = "https://www.googleapis.com/oauth2/v3/certs"
}