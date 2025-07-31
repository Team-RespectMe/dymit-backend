package net.noti_me.dymit.dymit_backend_api.application.oidc

import com.auth0.jwt.interfaces.DecodedJWT
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.AppleJwksProvider
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.GoogleJwksProvider
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.JwksProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.AppleOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.GoogleOidcIdTokenPayload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class DymitAppleOidcAuthenticationProvider(
    private val jwksProvider: AppleJwksProvider,
    @Value("\${dymit.oidc.apple.audience}")
    private val _audience: String
) : AbstractOidcAuthenticationProvider() {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("DymitAppleOidcAuthenticationProvider initialized with audience: {}", _audience)
    }

    companion object {

        private const val ISSUER_NAME = "https://appleid.apple.com"

        private const val PROVIDER_NAME = "apple"
    }

    override fun convertIdToken(decodedJWT: DecodedJWT): CommonOidcIdTokenPayload {
        val payload =  AppleOidcIdTokenPayload.valueOf(decodedJWT)
        return AppleOidcIdTokenPayload.toCommonPayload(payload)
    }

    override fun getJwksProvider(): JwksProvider {
        return jwksProvider
    }

    override fun getIssuer(): String {
        return ISSUER_NAME
    }

    override fun getProviderName(): String {
        return PROVIDER_NAME
    }

    override fun getAudience(): List<String> {
        return _audience.split(",")
    }
}