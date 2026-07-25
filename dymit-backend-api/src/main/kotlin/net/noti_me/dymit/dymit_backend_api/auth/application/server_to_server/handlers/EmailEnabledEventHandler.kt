package net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.handlers

import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AppleS2SEvent
import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AppleS2SPayload
import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.EmailEnabledEvent
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.UpdateAuthenticationIdentityPort
import org.springframework.stereotype.Service

@Service
class EmailEnabledEventHandler(
    private val updateAuthenticationIdentityPort: UpdateAuthenticationIdentityPort
): AppleS2SEventHandler() {

    override fun isSupport(payload: AppleS2SPayload): Boolean {
        return payload.events["type"] == EmailEnabledEvent.EVENT_TYPE
    }

    override fun castToEvent(payload: AppleS2SPayload): AppleS2SEvent {
        return EmailEnabledEvent.from(payload)
    }

    override fun process(
        event: AppleS2SEvent,
        provider: String,
        subject: String
    ) {
        val emailEnabledEvent = event as EmailEnabledEvent
        updateAuthenticationIdentityPort.updateEmail(
            provider = provider,
            subject = subject,
            email = emailEnabledEvent.email
        )
    }
}
