package net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto

class EmailEnabledEvent(
    type: String,
    sub: String,
    eventTime: Long,
    val email: String,
    val isPrivateEmail: String,
): AppleS2SEvent(type, sub, eventTime) {
    init {
        require(type == EVENT_TYPE) { "Invalid event type: $type" }
    }

    companion object {

        const val EVENT_TYPE = "email-enabled"

        fun from(payload: AppleS2SPayload): EmailEnabledEvent {
            return EmailEnabledEvent(
                type = payload.events["type"] as String,
                sub = payload.events["sub"] as String,
                email = payload.events["email"] as String,
                isPrivateEmail = payload.events["is_private_email"] as String,
                eventTime = payload.events["event_time"] as Long
            )
        }
    }
}