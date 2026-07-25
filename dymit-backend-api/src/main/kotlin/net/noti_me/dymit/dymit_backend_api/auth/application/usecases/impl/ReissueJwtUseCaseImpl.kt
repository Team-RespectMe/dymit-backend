package net.noti_me.dymit.dymit_backend_api.auth.application.usecases.impl

import net.noti_me.dymit.dymit_backend_api.auth.application.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.LoadAuthenticationMemberPort
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.ManageAuthenticationSessionPort
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.auth.application.usecases.ReissueJwtUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ReissueJwtUseCaseImpl(
    private val jwtService: JwtService,
    private val loadAuthenticationMemberPort: LoadAuthenticationMemberPort,
    private val manageAuthenticationSessionPort: ManageAuthenticationSessionPort
): ReissueJwtUseCase {


    override fun reissue(refreshToken: String): LoginResult {
        val decodedToken = jwtService.decodeToken(refreshToken)
        val memberId = decodedToken.subject
        val member = loadAuthenticationMemberPort.loadByMemberId(memberId)
            ?: throw UnauthorizedException(code="AE-003", message="사용자 정보를 찾을 수 없습니다.")
        val existsToken = member.refreshTokens.find {
            it.token==refreshToken
        }  ?: throw UnauthorizedException(code="AE-004", message="비활성화 되었거나 등록되지 않은 리프레시 토큰입니다.")

        if ( member.isDeleted ) {
            throw UnauthorizedException(code="AE-002", message="삭제된 회원입니다. 관리자에게 문의하세요.")
        }

        // Refresh 토큰의 유효기간이 하루 이하로 남은 경우 재발급 로직을 수행하고, 기존 토큰을 제거한다.
        // 우선 expiredAt을 Instant로 변환
        val expiresAt = decodedToken.expiresAt!!.toInstant()
        val current = Instant.now()

        if (  existsToken.isExpired() ) {
            manageAuthenticationSessionPort.removeRefreshToken(
                memberId = member.memberId,
                refreshToken = refreshToken
            )
            throw UnauthorizedException("AE-005", "만료된 리프레시 토큰입니다.")
        }

        var newRefreshToken = refreshToken
        if ( expiresAt.isBefore(current.plusMillis(24 * 60 * 60 * 1000) ) ) {
            val newRefreshTokenInfo = jwtService.createRefreshToken(member.memberId)
            manageAuthenticationSessionPort.rotateRefreshToken(
                memberId = member.memberId,
                previousRefreshToken = refreshToken,
                newRefreshToken = newRefreshTokenInfo.token,
                expiresAt = newRefreshTokenInfo.expireAt
            )
            newRefreshToken = newRefreshTokenInfo.token
        } else {
            manageAuthenticationSessionPort.recordAccess(member.memberId)
        }

        return LoginResult(
            memberId = member.memberId,
            accessToken = jwtService.createAccessToken(
                memberId = member.memberId,
                nickname = member.nickname,
                roles = member.roles
            ).token,
            refreshToken = newRefreshToken
        )

    }
}
