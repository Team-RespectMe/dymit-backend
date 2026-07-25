package net.noti_me.dymit.dymit_backend_api.auth.application.port.out

/**
 * 외부 인증 제공자의 식별 정보를 갱신합니다.
 */
interface UpdateAuthenticationIdentityPort {

    /**
     * OIDC 식별 정보의 이메일을 갱신합니다.
     *
     * @param provider OIDC 제공자 이름
     * @param subject OIDC subject
     * @param email 변경할 이메일
     */
    fun updateEmail(
        provider: String,
        subject: String,
        email: String?
    )
}
