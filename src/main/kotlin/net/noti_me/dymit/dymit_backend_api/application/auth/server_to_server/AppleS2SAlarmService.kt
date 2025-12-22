package net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.AppleJwksProvider
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SPayload
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SRequest
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.handlers.AppleS2SEventHandler
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.handlers.S2SEventProcessor
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.interfaces.RSAPublicKey

/**
 * Apple Server to Server alarm 처리 서비스
 */
@Service
class AppleS2SAlarmService(
    private val jwkProvider: AppleJwksProvider,
    private val handlers: List<AppleS2SEventHandler>
) {

    private val logger = LoggerFactory.getLogger(AppleS2SAlarmService::class.java)

    companion object {
        const val ISSUER_NAME = "https://appleid.apple.com"
    }

    fun handleEvent(request: AppleS2SRequest) {
        val decodedJWT = verifyJwt(request.payload)
        val payload = AppleS2SPayload.from(decodedJWT)
        for ( handler in handlers ) {
            if ( handler.isSupport(payload) ) {
                handler.handle(payload)
                return
            }
        }
        logger.warn("No handler found for Apple S2S event type: ${payload.toString()}")
    }

    private fun getAlgorithm(decodedJWT: DecodedJWT, pubKey: RSAPublicKey): Algorithm {
        return when(decodedJWT.algorithm) {
            "RS256" -> Algorithm.RSA256(pubKey)
            "RS384" -> Algorithm.RSA384(pubKey)
            "RS512" -> Algorithm.RSA512(pubKey)
            else -> throw UnsupportedOperationException("Unsupported algorithm: ${decodedJWT.algorithm}")
        }
    }

    private fun verifyJwt(jwt: String): DecodedJWT {
        val decodedJWT = JWT.decode(jwt)
        val publicKey = jwkProvider.getPublicKey(decodedJWT.keyId)
        val algorithm = getAlgorithm(decodedJWT, publicKey)
        val verifier = JWT.require(algorithm)
            .withIssuer(ISSUER_NAME)
            .build()

        return try {
            verifier.verify(jwt)
        } catch ( e: JWTVerificationException ) {
            throw BadRequestException(message = "Invalid JWT token, expired or manipulated. ")
        }
    }
}