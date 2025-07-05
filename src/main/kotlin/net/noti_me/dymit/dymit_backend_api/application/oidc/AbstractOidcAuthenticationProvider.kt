package net.noti_me.dymit.dymit_backend_api.application.oidc

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.JwksProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.common.errors.UnauthorizedException
import org.springframework.beans.factory.annotation.Value
import java.security.interfaces.RSAPublicKey

abstract class AbstractOidcAuthenticationProvider : OidcAuthenticationProvider {

//    abstract fun getAudience(): List<String>

    @Value("\${spring.profiles.active}")
    private lateinit var activeProfile: String

    abstract fun getIssuer(): String

    abstract fun getJwksProvider(): JwksProvider

    abstract fun getProviderName(): String

    abstract fun convertIdToken(decodedJWT: DecodedJWT): CommonOidcIdTokenPayload

    override fun support(providerName: String): Boolean {
        return providerName.equals(getProviderName(), ignoreCase = true)
    }

    override fun getPayload(idToken: String): CommonOidcIdTokenPayload {
        val decodedJWT = JWT.decode(idToken)
        val jwksProvider = getJwksProvider()
        val publicKey = jwksProvider.getPublicKey(decodedJWT.keyId)
        val algorithm = getAlgorithm(decodedJWT, publicKey)
        var verifierBuilder = JWT.require(algorithm)
            .withIssuer(getIssuer())

        if (activeProfile == "test") {
            // 테스트 환경에서는 expiry 검증을 하지 않음.
            verifierBuilder.acceptExpiresAt(3153600000L)
        }

        val verifier = verifierBuilder.build()
        // TODO: Audience 검증 로직 추가

        return try {
            val verifiedJWT: DecodedJWT = verifier.verify(idToken)
            convertIdToken(verifiedJWT)
        } catch (e: Exception) {
            throw UnauthorizedException("OIDC 인증 실패, OIDC ID 토큰 검증에 실패하였습니다. message : ${e.message ?: "Unknown error"}")
        }
    }

    private fun getAlgorithm(decodedJWT: DecodedJWT, pubKey: RSAPublicKey): Algorithm {
        return when(decodedJWT.algorithm) {
            "RS256" -> Algorithm.RSA256(pubKey)
            "RS384" -> Algorithm.RSA384(pubKey)
            "RS512" -> Algorithm.RSA512(pubKey)
            else -> throw UnsupportedOperationException("Unsupported algorithm: ${decodedJWT.algorithm}")
        }
    }
}