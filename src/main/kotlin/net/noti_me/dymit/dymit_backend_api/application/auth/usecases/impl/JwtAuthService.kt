package net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl

import com.auth0.jwt.exceptions.JWTVerificationException
import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.auth.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.JwtAuthUsecase
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.errors.UnauthorizedException
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtAuthService(
    private val oidcAuthenticationProviders: List<OidcAuthenticationProvider>,
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val jwtService: JwtService
): JwtAuthUsecase {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun login(provider: OidcProvider, idToken: String): LoginResult {
        val oidcAuthenticationProvider = oidcAuthenticationProviders
            .firstOrNull { it.support(provider.name) }
            ?: throw BadRequestException("지원하지 않는 OIDC 프로바이더 입니다 ${provider.name}")
        val payload = oidcAuthenticationProvider.getPayload(idToken)
        logger.info("OIDC Login 요청 : ${provider.name}, sub: ${payload.sub}, email: ${payload.email}")

        val member = loadMemberPort.loadByOidcIdentity(
            OidcIdentity(
                provider = provider.name,
                subject = payload.sub
            )
        ) ?: throw NotFoundException("존재하지 않는 회원입니다. 회원 가입이 필요합니다.")

        val refreshToken = jwtService.createRefreshToken(member)
        val accessToken = jwtService.createAccessToken(member)

        val result = LoginResult(
            memberId = member.identifier,
            accessToken = accessToken.token,
            refreshToken = refreshToken.token
        )
        member.addRefreshToken(refreshToken.token, refreshToken.expireAt)
        saveMemberPort.persist(member)
        return result
    }

    override fun reissueAccessToken(refreshToken: String): LoginResult {
        val decodedToken = jwtService.decodeToken(refreshToken)
        val memberId = decodedToken.subject
        val member = loadMemberPort.loadById(memberId)
            ?: throw UnauthorizedException("AE-003", "사용자 정보를 찾을 수 없습니다.")
        val existsToken = member.refreshTokens.find {
            it.token==refreshToken
        }  ?: throw UnauthorizedException("AE-004", "비활성화 되었거나 등록되지 않은 리프레시 토큰입니다.")

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

    override fun logout(refreshToken: String): Boolean {
        val decodedJWT = jwtService.decodeToken(refreshToken)
        val memberId = decodedJWT.subject
        val member = loadMemberPort.loadById(memberId)
            ?: return false
        member.removeRefreshToken(refreshToken)
        saveMemberPort.persist(member)
        return true
    }
}