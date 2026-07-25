package net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.handlers

import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AppleS2SPayload

interface S2SEventProcessor {

    fun handle(appleS2SPayload: AppleS2SPayload)
}