package net.noti_me.dymit.dymit_backend_api.auth.application.port.out.dto

import java.time.Instant

/**
 * 인증 흐름에서 필요한 최소 회원 정보입니다.
 *
 * @property memberId 회원 식별자
 * @property nickname 회원 닉네임
 * @property roles 인증 권한 이름 목록
 * @property isDeleted 탈퇴 여부
 * @property refreshTokens 등록된 리프레시 토큰 목록
 */
data class AuthenticationMemberDto(
    val memberId: String,
    val nickname: String,
    val roles: List<String>,
    val isDeleted: Boolean,
    val refreshTokens: List<AuthenticationRefreshTokenDto>
)

/**
 * 인증 모듈에서 사용하는 리프레시 토큰 정보입니다.
 *
 * @property token 토큰 문자열
 * @property expiresAt 만료 시각
 */
data class AuthenticationRefreshTokenDto(
    val token: String,
    val expiresAt: Instant
) {

    /**
     * 토큰 만료 여부를 반환합니다.
     *
     * @return 현재 시각 기준 만료 여부
     */
    fun isExpired(): Boolean {
        return Instant.now().isAfter(expiresAt)
    }
}
