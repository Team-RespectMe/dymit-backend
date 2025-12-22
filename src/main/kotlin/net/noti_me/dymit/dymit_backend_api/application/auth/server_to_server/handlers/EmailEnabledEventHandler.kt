package net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.handlers

import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SEvent
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SPayload
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.EmailEnabledEvent
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.UpdateOidcIdentityUseCase
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.springframework.stereotype.Service

@Service
class EmailEnabledEventHandler(
    private val updateOidcIdentityUseCase: UpdateOidcIdentityUseCase
): AppleS2SEventHandler() {

    override fun isSupport(payload: AppleS2SPayload): Boolean {
        return payload.events["type"] == EmailEnabledEvent.EVENT_TYPE
    }

    override fun castToEvent(payload: AppleS2SPayload): AppleS2SEvent {
        return EmailEnabledEvent.from(payload)
    }

    override fun process(event: AppleS2SEvent, oidcInfo: OidcIdentity) {
        val emailEnabledEvent = event as EmailEnabledEvent
        oidcInfo.email = emailEnabledEvent.email
        updateOidcIdentityUseCase.update(oidcInfo)
    }
}