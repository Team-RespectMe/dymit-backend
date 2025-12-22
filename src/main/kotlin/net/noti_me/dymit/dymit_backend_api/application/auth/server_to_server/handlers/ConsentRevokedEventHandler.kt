package net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.handlers

import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SEvent
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SPayload
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.ConsentRevokedEvent
import net.noti_me.dymit.dymit_backend_api.application.member.MemberServiceFacade
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import org.springframework.stereotype.Service

@Service
class ConsentRevokedEventHandler(
    private val loadMemberPort: LoadMemberPort,
    private val memberServiceFacade: MemberServiceFacade
): AppleS2SEventHandler() {

    override fun isSupport(payload: AppleS2SPayload): Boolean {
        return payload.events["type"] == ConsentRevokedEvent.EVENT_TYPE
    }

    override fun castToEvent(payload: AppleS2SPayload): AppleS2SEvent {
        return ConsentRevokedEvent.from(payload)
    }

    override fun process(event: AppleS2SEvent, oidcInfo: OidcIdentity) {
        val consentRevokedEvent = event as ConsentRevokedEvent
        val member = loadMemberPort.loadByOidcIdentity(oidcInfo)
            ?: return // 이미 삭제된 상태일 수 있음
        val memberInfo = MemberInfo.from(member)
        memberServiceFacade.deleteMember(memberInfo, member.identifier)
    }
}