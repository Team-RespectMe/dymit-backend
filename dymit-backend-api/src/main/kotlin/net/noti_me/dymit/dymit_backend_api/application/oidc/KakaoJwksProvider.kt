package net.noti_me.dymit.dymit_backend_api.application.oidc

import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.AbstractJwksProvider
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KakaoJwksProvider(
    private val webClient: WebClient
): AbstractJwksProvider(webClient) {

    override val jwksUrl: String = "https://kauth.kakao.com/.well-known/jwks.json"
}

