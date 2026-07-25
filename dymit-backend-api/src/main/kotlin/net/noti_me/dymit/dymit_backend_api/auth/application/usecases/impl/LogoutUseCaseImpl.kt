package net.noti_me.dymit.dymit_backend_api.auth.application.usecases.impl

import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.LoadAuthenticationMemberPort
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.ManageAuthenticationSessionPort
import net.noti_me.dymit.dymit_backend_api.auth.application.usecases.LogoutUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtService
import org.springframework.stereotype.Service

@Service
class LogoutUseCaseImpl(
    private val jwtService: JwtService,
    private val loadAuthenticationMemberPort: LoadAuthenticationMemberPort,
    private val manageAuthenticationSessionPort: ManageAuthenticationSessionPort
): LogoutUseCase {

    override fun logout(refreshToken: String): Boolean {
        val decodedJWT = jwtService.decodeToken(refreshToken)
        val memberId = decodedJWT.subject
        loadAuthenticationMemberPort.loadByMemberId(memberId) ?: return false
        return manageAuthenticationSessionPort.removeRefreshToken(
            memberId = memberId,
            refreshToken = refreshToken
        )
    }
}
