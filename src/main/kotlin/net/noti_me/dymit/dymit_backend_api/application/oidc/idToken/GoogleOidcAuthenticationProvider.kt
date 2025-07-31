package net.noti_me.dymit.dymit_backend_api.application.oidc.idToken

import com.auth0.jwt.interfaces.DecodedJWT
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.GoogleJwksProvider
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.JwksProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.AbstractOidcAuthenticationProvider
import org.springframework.stereotype.Component
//@Component
//class GoogleOidcAuthenticationProvider(
//    private val issuerName: String = "https://accounts.google.com",
//    private val googleJwksProvider: GoogleJwksProvider,
//): AbstractOidcAuthenticationProvider() {
//
//    override fun convertIdToken(decodedJWT: DecodedJWT): CommonOidcIdTokenPayload {
//        val providerPayload = GoogleOidcIdTokenPayload.valueOf(decodedJWT)
//        return providerPayload.toCommonPayload()
//    }
//
//    override fun getJwksProvider(): JwksProvider {
//        return googleJwksProvider
//    }
//
//    override fun getIssuer(): String {
//        return issuerName
//    }
//
//    override fun getProviderName(): String {
//        return "google"
//    }
//}