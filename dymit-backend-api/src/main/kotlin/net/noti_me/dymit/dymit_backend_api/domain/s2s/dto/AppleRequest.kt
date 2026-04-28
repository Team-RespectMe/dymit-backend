package net.noti_me.dymit.dymit_backend_api.domain.s2s.dto

import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto.AppleS2SRequest

class AppleRequest(
    val payload: String
) {

    fun toCommand(): AppleS2SRequest {
        return AppleS2SRequest(
            payload = this.payload
        )
    }
}