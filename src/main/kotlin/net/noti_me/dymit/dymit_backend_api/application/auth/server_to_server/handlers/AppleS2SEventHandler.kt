package net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.handlers

import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SEvent
import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SPayload
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

abstract class AppleS2SEventHandler(): S2SEventProcessor {

    abstract fun isSupport(payload: AppleS2SPayload): Boolean

    protected abstract fun castToEvent(payload: AppleS2SPayload): AppleS2SEvent

    protected abstract fun process(event: AppleS2SEvent, oidcInfo: OidcIdentity)

    private final fun castToMemberOidcInfo(event: AppleS2SEvent): OidcIdentity {
        return OidcIdentity(
            provider = OidcProvider.APPLE.name,
            subject = event.sub,
        )
    }

    final override fun handle(appleS2SPayload: AppleS2SPayload) {
        if (!isSupport(payload = appleS2SPayload)) return
        val event = castToEvent(payload = appleS2SPayload)
        val oidcIdentity = castToMemberOidcInfo(event = event)
        process(event = event, oidcInfo = oidcIdentity)
    }
}