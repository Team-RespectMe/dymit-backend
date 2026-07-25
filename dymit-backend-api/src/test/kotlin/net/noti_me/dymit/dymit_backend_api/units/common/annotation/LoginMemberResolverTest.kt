package net.noti_me.dymit.dymit_backend_api.units.common.annotation

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMemberResolver
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.core.MethodParameter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.ServletWebRequest

class LoginMemberResolverTest : BehaviorSpec({

    val resolver = LoginMemberResolver()
    val memberInfo = MemberInfo.of(
        memberId = "member-id",
        nickname = "nickname",
        roles = listOf("ROLE_MEMBER")
    )

    beforeEach {
        SecurityContextHolder.clearContext()
    }

    afterEach {
        SecurityContextHolder.clearContext()
    }

    given("@LoginMember로 선언된 파라미터가 주어지면") {
        val parameter = MethodParameter(
            LoginMemberHandler::class.java.getDeclaredMethod("required", MemberInfo::class.java),
            0
        )

        `when`("인증된 회원 정보가 있으면") {
            then("인증 주체를 그대로 반환한다") {
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(memberInfo, null)

                resolver.supportsParameter(parameter) shouldBe true
                resolver.resolveArgument(
                    parameter,
                    null,
                    ServletWebRequest(MockHttpServletRequest()),
                    null
                ) shouldBe memberInfo
            }
        }
    }

    given("@LoginMember가 없는 파라미터가 주어지면") {
        val parameter = MethodParameter(
            LoginMemberHandler::class.java.getDeclaredMethod("plain", String::class.java),
            0
        )

        then("resolver가 지원하지 않는다") {
            resolver.supportsParameter(parameter) shouldBe false
        }
    }

    given("인증되지 않은 optional @LoginMember 파라미터가 주어지면") {
        val parameter = MethodParameter(
            LoginMemberHandler::class.java.getDeclaredMethod("optional", MemberInfo::class.java),
            0
        )

        then("리팩터링 전부터의 NullPointerException을 유지한다") {
            shouldThrow<NullPointerException> {
                resolver.resolveArgument(
                    parameter,
                    null,
                    ServletWebRequest(MockHttpServletRequest()),
                    null
                )
            }
        }
    }
})

private class LoginMemberHandler {

    fun required(@LoginMember memberInfo: MemberInfo) = memberInfo

    fun optional(@LoginMember(required = false) memberInfo: MemberInfo?) = memberInfo

    fun plain(value: String) = value
}
