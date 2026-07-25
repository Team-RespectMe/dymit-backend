package net.noti_me.dymit.dymit_backend_api.member.application.impl

import net.noti_me.dymit.dymit_backend_api.member.application.dto.CreateMemberCommand
import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberAuthenticationResultDto
import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.CreateMemberUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberCreatedEvent
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.SaveMemberPort
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class CreateMemberUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val oidcAuthenticationProviders: List<OidcAuthenticationProvider>,
    private val jwtService: JwtService,
    private val eventPublisher: ApplicationEventPublisher
) : CreateMemberUseCase {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun createMember(command: CreateMemberCommand): MemberCreateResult {
        val oidcAuthenticationProvider = oidcAuthenticationProviders
            .firstOrNull { it.support(command.oidcProvider.name) }
            ?: throw IllegalArgumentException("지원하지 않는 OIDC 프로바이더 입니다 ${command.oidcProvider.name}")

        val payload = oidcAuthenticationProvider.getPayload(command.idToken)

        logger.debug("회원가입 요청: ${command.oidcProvider.name}, sub: ${payload.sub}, email: ${payload.email}")
        loadMemberPort.loadByOidcIdentity(OidcIdentity(provider = command.oidcProvider.name, subject = payload.sub))
            ?.let{ throw ConflictException(message= "이미 회원가입이 된 계정입니다.") }

        if ( loadMemberPort.existsByNickname(command.nickname) ) {
            throw ConflictException("CONFLICT", "이미 사용 중인 닉네임입니다.")
        }

        var member = Member(
            nickname = command.nickname,
            oidcIdentities = mutableSetOf(OidcIdentity(
                provider = command.oidcProvider.name,
                subject = payload.sub,
                email = payload.email
            )),
            interests = command.interests.toMutableSet()
        )

        member = saveMemberPort.persist(member)
        eventPublisher.publishEvent(MemberCreatedEvent(member))
        val refreshToken = jwtService.createRefreshToken(member.identifier)
        val accessToken = jwtService.createAccessToken(
            memberId = member.identifier,
            nickname = member.nickname,
            roles = member.roles.map { it.name }
        )
        member.addRefreshToken(refreshToken.token, refreshToken.expireAt)
        saveMemberPort.persist(member)

        return MemberCreateResult.from(
            member = MemberDto.fromEntity(member),
            loginResult = MemberAuthenticationResultDto(
                memberId = member.identifier,
                accessToken = accessToken.token,
                refreshToken = refreshToken.token
            )
        )
    }
}
