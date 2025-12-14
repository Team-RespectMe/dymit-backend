package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.AuthServiceFacade
import net.noti_me.dymit.dymit_backend_api.application.member.dto.CreateMemberCommand
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.application.member.impl.CreateMemberUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher

internal class CreateMemberUseCaseImplTest(): BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val oidcAuthenticationProviders = listOf(
        mockk<OidcAuthenticationProvider>()
    )

    private val authServiceFacade = mockk<AuthServiceFacade>()

    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val usecase = CreateMemberUseCaseImpl(
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort,
        oidcAuthenticationProviders = oidcAuthenticationProviders,
        authService = authServiceFacade,
        eventPublisher = eventPublisher
    )

    private val memberId = ObjectId.get()

    private var member = createMemberEntity(id=memberId, interests = setOf("Kotlin", "Spring"))

    private val samplePayload = CommonOidcIdTokenPayload(
        iss = "https://accounts.google.com",
        sub = member.identifier,
        aud = listOf("sample-audience"),
        exp = 1716239022,
        iat = 1616239022,
        email = "${member.id}@example.com",
    )

    init {

        beforeEach {
            member = createMemberEntity(id=memberId)
        }

        afterEach {
            clearAllMocks()
        }

        Given("사용자 생성 명령이 주어진다.") {
            val command = CreateMemberCommand(
                nickname = member.nickname,
                oidcProvider = OidcProvider.GOOGLE,
                idToken = "SampleToken",
                interests = member.interests.toList()
            )

            When("지원하는 OIDC 플랫폼이 아니라면") {
                every { oidcAuthenticationProviders[0].support(any()) } returns false
                Then("IllegalArgumentException 예외가 발생한다.") {
                    shouldThrow<IllegalArgumentException> {
                        usecase.createMember(command)
                    }
               }
            }

            When("해당 OIDC 플랫폼으로 이미 가입한 정보가 있다면") {
                every{ oidcAuthenticationProviders[0].support(OidcProvider.GOOGLE.name) } returns true
                every{ oidcAuthenticationProviders[0].getPayload("SampleToken") } returns samplePayload
                every { loadMemberPort.loadByOidcIdentity(any()) } returns member

                Then("ConflictException 예외가 발생한다.") {
                    shouldThrow<ConflictException> {
                        usecase.createMember(command)
                    }
               }
            }

            When("닉네임이 이미 사용 중이라면") {
                every{ oidcAuthenticationProviders[0].support(OidcProvider.GOOGLE.name) } returns true
                every{ oidcAuthenticationProviders[0].getPayload("SampleToken") } returns samplePayload
                every { loadMemberPort.loadByOidcIdentity(any()) } returns null
                every { loadMemberPort.existsByNickname(member.nickname) } returns true

                Then("ConflictException 예외가 발생한다.") {
                    shouldThrow<ConflictException> {
                        usecase.createMember(command)
                    }
                }
            }

            When("정상적인 사용자 생성 명령이라면") {
                every{ oidcAuthenticationProviders[0].support(OidcProvider.GOOGLE.name) } returns true
                every{ oidcAuthenticationProviders[0].getPayload("SampleToken") } returns samplePayload
                every { loadMemberPort.loadByOidcIdentity(any()) } returns null
                every { loadMemberPort.existsByNickname(member.nickname) } returns false
                every { saveMemberPort.persist(any()) } returns member
                every { authServiceFacade.loginByOidcToken(
                    provider = OidcProvider.GOOGLE,
                    idToken = "SampleToken"
                ) } returns mockk()

                Then("사용자가 생성되고, MemberCreateResult가 반환된다.") {
                    val result = usecase.createMember(command)
                    result::class shouldBe MemberCreateResult::class
               }
            }
        }
    }
}