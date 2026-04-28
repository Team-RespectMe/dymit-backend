package net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto


abstract class AppleS2SEvent(
    val type: String,
    val sub: String,
    val eventTime: Long
) {

    final fun isType(expectedType: String): Boolean {
        return type == expectedType
    }
}