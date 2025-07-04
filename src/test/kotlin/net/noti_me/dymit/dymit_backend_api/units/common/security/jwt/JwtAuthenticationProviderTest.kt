package net.noti_me.dymit.dymit_backend_api.units.common.security.jwt

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtAuthenticationToken
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.supports.createJwtConfig
import net.noti_me.dymit.dymit_backend_api.supports.createJwtService
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication

class JwtAuthenticationProviderTest : AnnotationSpec() {

    private val jwtService = createJwtService(createJwtConfig())

    private val provider = JwtAuthenticationProvider(jwtService)

    private val member = Member(
        id = "memberId",
        nickname = "nickname",
    )

    @Test
    fun `supports 테스트`() {
        provider.supports(TestingAuthenticationToken::class.java ) shouldBe false
        provider.supports(JwtAuthenticationToken::class.java ) shouldBe true
    }

    @Test
    fun `올바른 JWT 토큰 인증 테스트`() {
        // Given
        val token = jwtService.createAccessToken(member)
        val jwtAuthenticationToken: Authentication = JwtAuthenticationToken(token);

        // When
        val authentication: Authentication? = provider.authenticate(jwtAuthenticationToken)

        authentication shouldNotBe null
        authentication!!
        // Then
        authentication.isAuthenticated shouldBe true
        authentication::class shouldBe JwtAuthenticationToken::class
        authentication.principal::class shouldBe MemberInfo::class
        val principal = authentication.principal as MemberInfo
        principal.memberId shouldBe member.id
        principal.nickname shouldBe member.nickname
        principal.roles.size shouldBe 1
    }
}