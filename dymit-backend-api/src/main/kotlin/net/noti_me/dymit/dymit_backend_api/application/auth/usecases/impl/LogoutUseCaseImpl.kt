package net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.auth.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.springframework.stereotype.Service

@Service
class LogoutUseCaseImpl(
    private val jwtService: JwtService,
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort
): LogoutUseCase {

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

