package net.noti_me.dymit.dymit_backend_api.auth.application.port.`in`.server_to_server.dto

import net.noti_me.dymit.dymit_backend_api.auth.application.server_to_server.dto.AppleS2SRequest

class AppleRequest(
    val payload: String
) {

    fun toCommand(): AppleS2SRequest {
        return AppleS2SRequest(
            payload = this.payload
        )
    }
}