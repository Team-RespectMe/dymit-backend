package net.noti_me.dymit.dymit_backend_api.units.application.auth.impl

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl.JwtAuthService
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.supports.createJwtConfig
import net.noti_me.dymit.dymit_backend_api.supports.createJwtService

internal class JwtAuthServiceTest() : BehaviorSpec() {

    private val oidcProvider = mockk<OidcAuthenticationProvider>()

    private val oidcProviders = listOf(oidcProvider)

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val jwtService = createJwtService(createJwtConfig())

    private val jwtAuthService = JwtAuthService(
        loadMemberPort = loadMemberPort,
        oidcAuthenticationProviders = oidcProviders,
        saveMemberPort = saveMemberPort,
        jwtService = jwtService
    )

    private val commonOidcIdTokenPayload = CommonOidcIdTokenPayload(
        iss = "https://accounts.google.com",
        sub = "test-subject",
        aud = listOf("test-audience"),
        iat = 1234567890L,
        exp = 1234567890L + 3600L, // 1 hour later
        email = "",
        name = "Test User",
    )

    private val oidcIdentity = OidcIdentity(
        provider = "GOOGLE",
        subject = commonOidcIdTokenPayload.sub
    )

    private var member = createMember()

    fun createMember() = Member(
        id = "random",
        nickname = "test-nickname",
        oidcIdentities = mutableSetOf(oidcIdentity)
    )

    init {

        beforeEach {
            member = createMember()
            every { oidcProvider.support(any()) } returns true
            every { oidcProvider.getPayload(any()) } returns commonOidcIdTokenPayload
        }

        given("로그인 요청이 주어진다.") {
            `when`("해당 OIDC 정보로 가입된 사용자가 없으면") {
                every { loadMemberPort.loadByOidcIdentity(any()) } returns null
                then("NotFoundException 이 발생한다") {
                    shouldThrowExactly<NotFoundException> {
                         jwtAuthService.login(
                            provider = OidcProvider.GOOGLE,
                            idToken = "test-id-token"
                         )
                    }
                }
            }

            `when`("해당 OIDC 정보로 가입된 사용자가 있으면") {
                every { loadMemberPort.loadByOidcIdentity(any()) } returns member
                then("로그인 결과가 반환된다") {
                    val loginResult = jwtAuthService.login(
                        provider = OidcProvider.GOOGLE,
                        idToken = "test-id-token"
                    )
                    loginResult.accessToken shouldNotBe null
                    loginResult.refreshToken shouldNotBe null
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }
}