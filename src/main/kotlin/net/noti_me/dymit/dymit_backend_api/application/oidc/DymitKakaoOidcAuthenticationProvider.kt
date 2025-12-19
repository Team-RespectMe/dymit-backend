package net.noti_me.dymit.dymit_backend_api.application.oidc

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import com.auth0.jwt.interfaces.DecodedJWT
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.JwksProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.KakaoOidcIdTokenPayload

@Component
class DymitKakaoOidcAuthenticationProvider(
    private val jwksProvider: KakaoJwksProvider,
    @Value("\${dymit.oidc.kakao.audience}")
    private val _audience: String
): AbstractOidcAuthenticationProvider() {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {

        private const val ISSUER_NAME = "https://kauth.kakao.com"

        private const val PROVIDER_NAME = "kakao"
    }

    override fun convertIdToken(decodedJWT: DecodedJWT): CommonOidcIdTokenPayload {
        val payload = KakaoOidcIdTokenPayload.valueOf(decodedJWT)
        return payload.toCommonPayload()
    }

    override fun getProviderName(): String {
        return PROVIDER_NAME
    }

    override fun getIssuer(): String {
        return ISSUER_NAME
    }

    override fun getJwksProvider(): JwksProvider {
        return jwksProvider
    }

    override fun getAudience(): List<String> {
        return _audience.split(",").map{it.trim()}
    }
}

