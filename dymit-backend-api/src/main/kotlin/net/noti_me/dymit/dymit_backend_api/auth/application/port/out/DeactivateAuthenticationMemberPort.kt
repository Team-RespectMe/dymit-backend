package net.noti_me.dymit.dymit_backend_api.auth.application.port.out

/**
 * 외부 인증 제공자 이벤트에 따라 회원 탈퇴를 수행합니다.
 */
interface DeactivateAuthenticationMemberPort {

    /**
     * OIDC 식별 정보에 해당하는 회원을 탈퇴 처리합니다.
     *
     * @param provider OIDC 제공자 이름
     * @param subject OIDC subject
     */
    fun deactivateByOidcIdentity(
        provider: String,
        subject: String
    )
}
