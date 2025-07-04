package net.noti_me.dymit.dymit_backend_api.common.security.jwt

import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole

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
    val roles: List<MemberRole>
) {
}