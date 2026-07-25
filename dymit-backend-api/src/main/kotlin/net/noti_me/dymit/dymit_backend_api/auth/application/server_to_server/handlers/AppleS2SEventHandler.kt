package net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.handlers

import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AppleS2SEvent
import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AppleS2SPayload
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcProvider

abstract class AppleS2SEventHandler(): S2SEventProcessor {

    abstract fun isSupport(payload: AppleS2SPayload): Boolean

    protected abstract fun castToEvent(payload: AppleS2SPayload): AppleS2SEvent

    protected abstract fun process(
        event: AppleS2SEvent,
        provider: String,
        subject: String
    )

    final override fun handle(appleS2SPayload: AppleS2SPayload) {
        if (!isSupport(payload = appleS2SPayload)) return
        val event = castToEvent(payload = appleS2SPayload)
        process(
            event = event,
            provider = OidcProvider.APPLE.name,
            subject = event.sub
        )
    }
}
