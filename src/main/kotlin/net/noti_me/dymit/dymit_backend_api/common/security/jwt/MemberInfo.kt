package net.noti_me.dymit.dymit_backend_api.common.security.jwt

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.JwtClaims
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * JwtAuthentication 인증된 사용자 정보를 나타낸다.
 * JwtAuthenticationProvider 에서 인증을 완료하고 나면
 * @param id 사용자 ID
 * @param nickname 사용자 닉네임
 * @param roles 사용자 권한 목록
 */
class MemberInfo(
    val memberId: String,
    val nickname: String,
    val roles: List<MemberRole>,
) {

    companion object {

        fun from(jwtClaims: JwtClaims): MemberInfo {
            return MemberInfo(
                memberId = jwtClaims.memberId,
                nickname = jwtClaims.nickname,
                roles = jwtClaims.roles.map {
                    when (it) {
                        "ROLE_MEMBER" -> MemberRole.ROLE_MEMBER
                        "ROLE_ADMIN" -> MemberRole.ROLE_ADMIN
                        else -> throw IllegalArgumentException("Invalid role: $it")
                    }
                }
            )
        }
    }

    override fun toString(): String {
        return "MemberInfo(memberId='$memberId', nickname='$nickname', roles=$roles)"
    }
}