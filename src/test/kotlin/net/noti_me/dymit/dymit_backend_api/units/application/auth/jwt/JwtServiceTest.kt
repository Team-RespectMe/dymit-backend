package net.noti_me.dymit.dymit_backend_api.units.application.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import net.noti_me.dymit.dymit_backend_api.application.auth.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.configs.JwtConfig
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.bson.types.ObjectId

class JwtServiceTest() : BehaviorSpec() {

    private val jwtConfig = JwtConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        accessTokenExpiration = 3600000L, // 1 hour
        refreshTokenExpiration = 86400000L // 1 day
    )

    private val jwtService = JwtService(jwtConfig)

    private val member = Member( id = ObjectId.get(),
        nickname = "test-nickname",
        oidcIdentities = mutableSetOf(OidcIdentity(
            provider = "GOOGLE",
            subject = "test-subject"
        ))
    )

    init {

        beforeEach {
        }

        given("회원 정보가 주어지고 ") {
            `when`("Access Token을 생성하면") {
                val accessToken = jwtService.createAccessToken(member)

                then("유효한 JWT 문자열이 반환되어야 한다") {
                    accessToken shouldNotBe  null
                    accessToken.startsWith("eyJ") shouldBe true // JWT는 'eyJ'로 시작해야 함
                }
            }

            `when`("Refresh Token을 생성하면") {
                val refreshToken = jwtService.createRefreshToken(member)
                then("유효한 JWT 문자열이 반환되어야 한다") {
                    refreshToken shouldNotBe null
                    refreshToken.startsWith("eyJ") shouldBe true // JWT는 'eyJ'로 시작해야 함
                }
            }
        }

        given("Access Token이 주어지고") {
            val accessToken = jwtService.createAccessToken(member)

            `when`("토큰을 검증하면") {
                val decodedToken = jwtService.verifyAccessToken(accessToken)

                then("토큰의 subject가 회원 ID와 일치해야 한다") {
                    decodedToken.subject shouldBe member.identifier
                    decodedToken.claims["nickname"]?.asString() shouldBe member.nickname
                }
            }

            `when`("토큰을 디코드하면") {
                val decodedToken = jwtService.decodeToken(accessToken)

                then("토큰의 subject가 회원 정보와 일치해야 한다") {
                    decodedToken.subject shouldBe member.identifier
                    decodedToken.claims["nickname"]?.asString() shouldBe member.nickname
                }
            }
        }

        given("Refresh Token이 주어지고") {
            val refreshToken = jwtService.createRefreshToken(member)
            `when`("토큰을 검증하면") {
                val decodedToken = jwtService.verifyRefreshToken(refreshToken)

                then("토큰의 subject가 회원 ID와 일치해야 한다") {
                    decodedToken.subject shouldBe member.identifier
                }
            }

            `when`("토큰을 디코드하면") {
                val decodedToken = jwtService.decodeToken(refreshToken)

                then("토큰의 subject가 회원 ID와 일치해야 한다") {
                    decodedToken.subject shouldBe member.identifier
                }
            }
        }

        given("만료된 Refresh Token이 주어지고") {
            val refreshToken = createExpiredToken()
            `when`("토큰을 디코드하면") {
                val decodedToken = jwtService.decodeToken(refreshToken)

                then("토큰의 subject가 회원 ID와 일치해야 한다") {
                    decodedToken.subject shouldBe member.identifier
                }
            }
        }

        given("만료된 Access Token이 주어지고") {
            val accessToken = createExpiredToken()
            `when`("토큰을 검증하면") {
                then("예외가 발생해야 한다") {
                    shouldThrow<JWTVerificationException> {
                        jwtService.verifyAccessToken(accessToken)
                    }
                }
            }
        }

        given("만료된 Refresh Token이 주어지고") {
            val refreshToken = createExpiredToken()
            `when`("토큰을 검증하면") {
                then("예외가 발생해야 한다") {
                    shouldThrow<JWTVerificationException> {
                        jwtService.verifyRefreshToken(refreshToken)
                    }
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }

    private fun createExpiredToken(): String {
        val algorithm = Algorithm.HMAC256(jwtConfig.secret)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withSubject(member.id.toHexString())
            .withClaim("nickname", member.nickname)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() - 1000)) // 1초 전으로 설정하여 만료된 토큰 생성
            .sign(algorithm)
    }
}