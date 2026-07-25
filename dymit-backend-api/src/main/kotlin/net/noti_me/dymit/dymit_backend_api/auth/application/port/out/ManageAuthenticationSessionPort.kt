package net.noti_me.dymit.dymit_backend_api.auth.application.port.out

import java.time.Instant

/**
 * 회원의 인증 세션과 최근 접근 시각을 관리합니다.
 */
interface ManageAuthenticationSessionPort {

    /**
     * 리프레시 토큰을 등록합니다.
     *
     * @param memberId 회원 식별자
     * @param refreshToken 리프레시 토큰
     * @param expiresAt 토큰 만료 시각
     */
    fun registerRefreshToken(
        memberId: String,
        refreshToken: String,
        expiresAt: Instant
    )

    /**
     * 리프레시 토큰을 제거합니다.
     *
     * @param memberId 회원 식별자
     * @param refreshToken 제거할 토큰
     * @return 회원이 존재하면 true
     */
    fun removeRefreshToken(
        memberId: String,
        refreshToken: String
    ): Boolean

    /**
     * 기존 리프레시 토큰을 새 토큰으로 교체합니다.
     *
     * @param memberId 회원 식별자
     * @param previousRefreshToken 기존 토큰
     * @param newRefreshToken 새 토큰
     * @param expiresAt 새 토큰 만료 시각
     */
    fun rotateRefreshToken(
        memberId: String,
        previousRefreshToken: String,
        newRefreshToken: String,
        expiresAt: Instant
    )

    /**
     * 회원의 최근 접근 시각을 갱신합니다.
     *
     * @param memberId 회원 식별자
     */
    fun recordAccess(memberId: String)
}
