package net.noti_me.dymit.dymit_backend_api.units.common.security.jwt

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtAuthenticationToken
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole

internal class JwtAuthenticationTokenTest : AnnotationSpec() {

    @Test
    fun `미인증 토큰 생성 테스트`() {
        // Given
        val principal: Any? = null
        val credentials: String = "1234"

        // When
        val token = JwtAuthenticationToken(principal = principal, credentials = credentials)

        // Then
        token.isAuthenticated shouldBe false
        token.credentials shouldBe credentials
        token.principal shouldBe null
    }

    @Test
    fun `인증된 토큰 생성 테스트`() {
        // Given
        val principal = MemberInfo(
            memberId = "memberId",
            nickname = "nickname",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
        val credentials: String? = null

        // When
        val token = JwtAuthenticationToken(principal = principal, credentials = credentials)

        // Then
        token.isAuthenticated shouldBe true
        token.credentials shouldBe null
        token.principal shouldBe principal
    }
}