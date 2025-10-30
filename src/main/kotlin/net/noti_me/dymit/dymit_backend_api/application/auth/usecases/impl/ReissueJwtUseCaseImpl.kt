package net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.auth.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.ReissueJwtUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.*
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ReissueJwtUseCaseImpl(
    private val jwtService: JwtService,
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort
): ReissueJwtUseCase {


    override fun reissue(refreshToken: String): LoginResult {
        val decodedToken = jwtService.decodeToken(refreshToken)
        val memberId = decodedToken.subject
        val member = loadMemberPort.loadById(memberId)
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
            member.removeRefreshToken(refreshToken)
            saveMemberPort.update(member)
            throw UnauthorizedException("AE-005", "만료된 리프레시 토큰입니다.")
        }

        var newRefreshToken = refreshToken
        if ( expiresAt.isBefore(current.plusMillis(24 * 60 * 60 * 1000) ) ) {
            val newRefreshTokenInfo = jwtService.createRefreshToken(member)
            member.removeRefreshToken(refreshToken)
            member.addRefreshToken(newRefreshTokenInfo.token, newRefreshTokenInfo.expireAt)
            newRefreshToken = newRefreshTokenInfo.token
            saveMemberPort.persist(member)
        }

        return LoginResult(
            memberId = member.identifier,
            accessToken = jwtService.createAccessToken(member).token,
            refreshToken = newRefreshToken
        )

    }
}

