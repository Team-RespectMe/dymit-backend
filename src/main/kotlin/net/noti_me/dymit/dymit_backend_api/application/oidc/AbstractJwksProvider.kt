package net.noti_me.dymit.dymit_backend_api.application.auth.oidc

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*
import java.util.concurrent.TimeUnit
import net.noti_me.dymit.dymit_backend_api.common.errors.InternalServerError

abstract class AbstractJwksProvider(
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val providerName: String
): JwksProvider {

    abstract val jwksUrl: String

    private var cachedJwks: List<JWKKey> = emptyList()

    private var cacheExpiryTime: Long = 0L

    private val cacheTTL = TimeUnit.HOURS.toMillis(1)

    private val lock = Any()

    override fun getPublicKey(kid: String): RSAPublicKey {
        val jwtKey = fetchJwks(kid).firstOrNull { it.kid == kid }
            ?: throw InternalServerError("OIDC 로그인 실패, kid에 해당하는 공개키를 찾을 수 없습니다.")

        return createPublicKey(jwtKey)
    }

    override fun isSupported(provider: String): Boolean {
        return provider.equals(providerName, ignoreCase = true)
    }

    private fun createPublicKey(jwkKey: JWKKey): RSAPublicKey {
        val modulus = jwkKey.n
        val exponent = jwkKey.e
        val modulusBytes = Base64.getUrlDecoder().decode(modulus)
        val exponentBytes = Base64.getUrlDecoder().decode(exponent)
        val rsaPublicKeySpec = RSAPublicKeySpec(BigInteger(1, modulusBytes), BigInteger(1, exponentBytes))
        return KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec) as RSAPublicKey
    }

    private fun fetchJwks(kid: String): List<JWKKey> {
        if (!cachedJwks.isEmpty() && System.currentTimeMillis() < cacheExpiryTime) {
            return cachedJwks
        }

        synchronized(lock) {
            if (!cachedJwks.isEmpty() && System.currentTimeMillis() < cacheExpiryTime) {
                return cachedJwks
            }

            return webClient.get()
                .uri(jwksUrl)
                .retrieve()
                .bodyToMono(JWKList::class.java)
                .block()
                ?.keys.also {
                    cachedJwks = it ?: emptyList()
                    cacheExpiryTime = System.currentTimeMillis() + cacheTTL
                }
                ?: throw InternalServerError("OIDC 로그인 실패, JWKS를 가져오는 데 실패했습니다.")
        }
    }
}