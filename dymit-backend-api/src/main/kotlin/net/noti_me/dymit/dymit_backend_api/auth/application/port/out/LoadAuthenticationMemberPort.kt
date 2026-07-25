package net.noti_me.dymit.dymit_backend_api.auth.application.port.out

import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.dto.AuthenticationMemberDto

/**
 * 인증 처리에 필요한 회원 정보를 조회합니다.
 */
interface LoadAuthenticationMemberPort {

    /**
     * 회원 식별자로 인증용 회원 정보를 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return 인증용 회원 정보, 존재하지 않으면 null
     */
    fun loadByMemberId(memberId: String): AuthenticationMemberDto?

    /**
     * OIDC 식별 정보로 인증용 회원 정보를 조회합니다.
     *
     * @param provider OIDC 제공자 이름
     * @param subject OIDC subject
     * @return 인증용 회원 정보, 존재하지 않으면 null
     */
    fun loadByOidcIdentity(
        provider: String,
        subject: String
    ): AuthenticationMemberDto?
}
