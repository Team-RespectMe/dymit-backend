package net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.handlers

import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AccountDeletedEvent
import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AppleS2SEvent
import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AppleS2SPayload
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.DeactivateAuthenticationMemberPort
import org.springframework.stereotype.Service

@Service
class AccountDeletedEventHandler(
    private val deactivateAuthenticationMemberPort: DeactivateAuthenticationMemberPort
): AppleS2SEventHandler() {

    override fun isSupport(payload: AppleS2SPayload): Boolean {
        return payload.events["type"] == AccountDeletedEvent.Companion
    }

    override fun castToEvent(payload: AppleS2SPayload): AppleS2SEvent {
        return AccountDeletedEvent.from(payload)
    }

    override fun process(
        event: AppleS2SEvent,
        provider: String,
        subject: String
    ) {
        deactivateAuthenticationMemberPort.deactivateByOidcIdentity(
            provider = provider,
            subject = subject
        )
    }
}
