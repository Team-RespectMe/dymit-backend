package net.noti_me.dymit.dymit_backend_api.units.auth.application.impl

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.LoadAuthenticationMemberPort
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.ManageAuthenticationSessionPort
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.dto.AuthenticationMemberDto
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.dto.AuthenticationRefreshTokenDto
import net.noti_me.dymit.dymit_backend_api.auth.application.usecases.impl.OidcLoginUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.TokenInfo
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcProvider
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.idToken.CommonOidcIdTokenPayload
import java.time.Instant

class OidcLoginUseCaseImplTest : BehaviorSpec({

    val oidcProvider = mockk<OidcAuthenticationProvider>()
    val loadAuthenticationMemberPort = mockk<LoadAuthenticationMemberPort>()
    val manageAuthenticationSessionPort = mockk<ManageAuthenticationSessionPort>(relaxed = true)
    val jwtService = mockk<JwtService>()
    val useCase = OidcLoginUseCaseImpl(
        oidcAuthenticationProviders = listOf(oidcProvider),
        loadAuthenticationMemberPort = loadAuthenticationMemberPort,
        manageAuthenticationSessionPort = manageAuthenticationSessionPort,
        jwtService = jwtService
    )
    val member = AuthenticationMemberDto(
        memberId = "member-id",
        nickname = "nickname",
        roles = listOf("ROLE_MEMBER"),
        isDeleted = false,
        refreshTokens = listOf(
            AuthenticationRefreshTokenDto("existing-token", Instant.parse("2026-07-26T00:00:00Z"))
        )
    )
    val payload = CommonOidcIdTokenPayload(
        iss = "issuer",
        sub = "oidc-subject",
        aud = listOf("audience"),
        iat = 1L,
        exp = 2L,
        email = "member@example.com"
    )
    val refreshExpiresAt = Instant.parse("2026-07-27T00:00:00Z")

    given("auth 전용 회원 DTO를 반환하는 인증 포트가 주어지면") {
        every { oidcProvider.support(OidcProvider.GOOGLE.name) } returns true
        every { oidcProvider.getPayload("id-token") } returns payload
        every {
            loadAuthenticationMemberPort.loadByOidcIdentity(OidcProvider.GOOGLE.name, payload.sub)
        } returns member
        every { jwtService.createRefreshToken(member.memberId) } returns TokenInfo("refresh-token", refreshExpiresAt)
        every {
            jwtService.createAccessToken(member.memberId, member.nickname, member.roles)
        } returns TokenInfo("access-token", refreshExpiresAt)

        `when`("OIDC 로그인을 수행하면") {
            val result = useCase.login(OidcProvider.GOOGLE, "id-token")

            then("auth DTO의 값으로 토큰과 세션을 생성한다") {
                result.memberId shouldBe member.memberId
                result.accessToken shouldBe "access-token"
                result.refreshToken shouldBe "refresh-token"
                verify(exactly = 1) {
                    manageAuthenticationSessionPort.registerRefreshToken(
                        member.memberId,
                        "refresh-token",
                        refreshExpiresAt
                    )
                }
            }
        }
    }
})
