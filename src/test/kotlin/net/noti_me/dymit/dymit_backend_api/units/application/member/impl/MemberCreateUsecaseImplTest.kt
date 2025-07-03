package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl.JwtAuthService
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.member.impl.MemberCreateUsecaseImpl
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.SaveMemberPort


internal class MemberCreateUsecaseImplTest(): BehaviorSpec() {

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val authenticationProvider = mockk<OidcAuthenticationProvider>()

    private val providers = listOf(authenticationProvider)

    private val jwtService = mockk<JwtAuthService>()

    private val memberCreateUsecase = MemberCreateUsecaseImpl(
        saveMemberPort = saveMemberPort,
        loadMemberPort = loadMemberPort,
        oidcAuthenticationProviders = providers,
        jwtAuthService = jwtService
    )

    private val commonOidcIdTokenPayload = CommonOidcIdTokenPayload(
        iss = "https://accounts.google.com",
        sub = "test-subject",
        aud = listOf("test-audience"),
        iat = 1234567890L,
        exp = 1234567890L + 3600L, // 1 hour later
        email = "test-email@dymit.com",
        name = "Test User",
        profileImageUrl = null
    )

    val command = MemberCreateCommand(
        nickname = "test-nickname",
        oidcProvider = OidcProvider.GOOGLE,
        idToken = ""
    )


    init {

        beforeEach {
            every { authenticationProvider.support(any()) } returns true
            every { authenticationProvider.getPayload( any() ) } returns commonOidcIdTokenPayload
        }

        given("회원가입 커맨드가 주어진다") {
            `when`("이미 가입된 회원이라면") {
                every { loadMemberPort.loadByOidcIdentity(any()) } returns mockk<Member>()
                then("ConflictException이 발생한다") {
                    shouldThrowExactly<ConflictException> {
                        memberCreateUsecase.createMember(command)
                    }
                }
            }

            `when`("신규 회원이라면") {
                every { loadMemberPort.loadByOidcIdentity( any() ) } returns null
                then("회원가입이 성공한다") {
                    every { saveMemberPort.persist(any()) } returns Member(
                        id = "random",
                        nickname = command.nickname,
                        oidcIdentities = mutableSetOf(
                            OidcIdentity(provider = "GOOGLE", subject = commonOidcIdTokenPayload.sub)
                        )
                    )
                    every { jwtService.login(any(), any()) } returns LoginResult(
                        accessToken = "eyJ...",
                        refreshToken = "eyJ..."
                    )

                    val result = memberCreateUsecase.createMember(command)
                    result.member.nickname shouldBe command.nickname
                    result.member.oidcIdentities.size shouldBe 1
                    result.member.oidcIdentities.first().provider shouldBe command.oidcProvider.name
                    result.member.oidcIdentities.first().subject shouldBe commonOidcIdTokenPayload.sub
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }
}