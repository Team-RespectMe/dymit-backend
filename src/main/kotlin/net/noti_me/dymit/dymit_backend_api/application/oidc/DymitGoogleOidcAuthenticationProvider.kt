package net.noti_me.dymit.dymit_backend_api.application.oidc

import com.auth0.jwt.interfaces.DecodedJWT
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.GoogleJwksProvider
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.JwksProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.GoogleOidcIdTokenPayload
import org.springframework.stereotype.Component

@Component
class DymitGoogleOidcAuthenticationProvider(
    private val jwksProvider: GoogleJwksProvider
) : AbstractOidcAuthenticationProvider() {

    companion object {

        private const val ISSUER_NAME = "https://accounts.google.com"

        private const val PROVIDER_NAME = "google"
    }

    override fun convertIdToken(decodedJWT: DecodedJWT): CommonOidcIdTokenPayload {
        return GoogleOidcIdTokenPayload.valueOf(decodedJWT).toCommonPayload()
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
}