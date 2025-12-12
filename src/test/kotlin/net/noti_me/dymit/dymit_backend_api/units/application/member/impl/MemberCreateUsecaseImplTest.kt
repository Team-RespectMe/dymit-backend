package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.member.dto.CreateMemberCommand
import net.noti_me.dymit.dymit_backend_api.application.member.impl.CreateMemberUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher


internal class MemberCreateUsecaseImplTest(): BehaviorSpec() {

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val authenticationProvider = mockk<OidcAuthenticationProvider>()

    private val providers = listOf(authenticationProvider)

    private val jwtService = mockk<JwtAuthService>()

    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val memberCreateUsecase = CreateMemberUseCaseImpl(
        saveMemberPort = saveMemberPort,
        loadMemberPort = loadMemberPort,
        oidcAuthenticationProviders = providers,
        jwtAuthService = jwtService,
        eventPublisher = eventPublisher
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

    val command = CreateMemberCommand(
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

            `when`("닉네임이 중복됐다면") {
                every { loadMemberPort.loadByOidcIdentity(any()) } returns mockk<Member>()
                every { loadMemberPort.existsByNickname(command.nickname) } returns true
                then("ConflictException이 발생한다") {
                    shouldThrowExactly<ConflictException> {
                        memberCreateUsecase.createMember(command)
                    }
                }
            }

            `when`("신규 회원이고, 닉네임이 중복돼지 않았다면") {
                every { loadMemberPort.loadByOidcIdentity( any() ) } returns null
                every { loadMemberPort.existsByNickname(command.nickname) } returns false
                then("회원가입이 성공한다") {
                    every { saveMemberPort.persist(any()) } returns Member(
                        id = ObjectId.get(),
                        nickname = command.nickname,
                        oidcIdentities = mutableSetOf(
                            OidcIdentity(provider = "GOOGLE", subject = commonOidcIdTokenPayload.sub)
                        )
                    )
                    every { jwtService.loginByOidcToken(any(), any()) } returns LoginResult(
                        memberId = "memberId",
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

        given("닉네임 중복 검사를 한다") {
            `when`("이미 사용 중인 닉네임이라면") {
                every { loadMemberPort.existsByNickname(command.nickname) } returns true

                then("ConflictException이 발생한다") {
                    shouldThrowExactly<ConflictException> {
                        memberCreateUsecase.checkNickname(command.nickname)
                    }
                }
            }

            `when`("사용 가능한 닉네임이라면") {
                every { loadMemberPort.existsByNickname(command.nickname) } returns false

                then("에러없이 동작한다.") {
                    shouldNotThrowAny {
                        memberCreateUsecase.checkNickname(command.nickname)
                    }
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }
}