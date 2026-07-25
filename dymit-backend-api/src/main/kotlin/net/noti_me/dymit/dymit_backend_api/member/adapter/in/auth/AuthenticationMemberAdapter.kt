package net.noti_me.dymit.dymit_backend_api.member.adapter.`in`.auth

import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.dto.AuthenticationMemberDto
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.dto.AuthenticationRefreshTokenDto
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.DeactivateAuthenticationMemberPort
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.LoadAuthenticationMemberPort
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.ManageAuthenticationSessionPort
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.UpdateAuthenticationIdentityPort
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.DeleteMemberUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.UpdateOidcIdentityUseCase
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.OidcIdentity
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * 인증 모듈이 요청한 회원 기능을 회원 도메인 규칙으로 수행하는 어댑터입니다.
 */
@Component
class AuthenticationMemberAdapter(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val deleteMemberUseCase: DeleteMemberUseCase,
    private val updateOidcIdentityUseCase: UpdateOidcIdentityUseCase
) : LoadAuthenticationMemberPort,
    ManageAuthenticationSessionPort,
    DeactivateAuthenticationMemberPort,
    UpdateAuthenticationIdentityPort {

    override fun loadByMemberId(memberId: String): AuthenticationMemberDto? {
        return loadMemberPort.loadById(memberId)?.toAuthenticationMemberDto()
    }

    override fun loadByOidcIdentity(
        provider: String,
        subject: String
    ): AuthenticationMemberDto? {
        return loadMemberPort.loadByOidcIdentity(
            OidcIdentity(
                provider = provider,
                subject = subject
            )
        )?.toAuthenticationMemberDto()
    }

    override fun registerRefreshToken(
        memberId: String,
        refreshToken: String,
        expiresAt: Instant
    ) {
        val member = loadMemberPort.loadById(memberId) ?: return
        member.addRefreshToken(refreshToken, expiresAt)
        saveMemberPort.persist(member)
    }

    override fun removeRefreshToken(
        memberId: String,
        refreshToken: String
    ): Boolean {
        val member = loadMemberPort.loadById(memberId) ?: return false
        member.removeRefreshToken(refreshToken)
        saveMemberPort.update(member)
        return true
    }

    override fun rotateRefreshToken(
        memberId: String,
        previousRefreshToken: String,
        newRefreshToken: String,
        expiresAt: Instant
    ) {
        val member = loadMemberPort.loadById(memberId) ?: return
        member.removeRefreshToken(previousRefreshToken)
        member.addRefreshToken(newRefreshToken, expiresAt)
        saveMemberPort.update(member)
    }

    override fun recordAccess(memberId: String) {
        val member = loadMemberPort.loadById(memberId) ?: return
        member.updateLastAccessedAt()
        saveMemberPort.update(member)
    }

    override fun deactivateByOidcIdentity(
        provider: String,
        subject: String
    ) {
        val member = loadMemberPort.loadByOidcIdentity(
            OidcIdentity(
                provider = provider,
                subject = subject
            )
        ) ?: return
        val memberInfo = MemberInfo.of(
            memberId = member.identifier,
            nickname = member.nickname,
            roles = member.roles.map { it.name }
        )
        deleteMemberUseCase.deleteMember(
            loginMember = memberInfo,
            memberId = member.identifier
        )
    }

    override fun updateEmail(
        provider: String,
        subject: String,
        email: String?
    ) {
        updateOidcIdentityUseCase.update(
            OidcIdentity(
                provider = provider,
                subject = subject,
                email = email
            )
        )
    }

    private fun Member.toAuthenticationMemberDto(): AuthenticationMemberDto {
        return AuthenticationMemberDto(
            memberId = identifier,
            nickname = nickname,
            roles = roles.map { it.name },
            isDeleted = isDeleted,
            refreshTokens = refreshTokens.map {
                AuthenticationRefreshTokenDto(
                    token = it.token,
                    expiresAt = it.expiresAt
                )
            }
        )
    }
}
