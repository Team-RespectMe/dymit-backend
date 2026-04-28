package net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto

class AccountDeletedEvent(
    type: String,
    sub: String,
    eventTime: Long
): AppleS2SEvent(type, sub, eventTime) {

    init {
        require(type == EVENT_TYPE) { "Invalid event type: $type" }
    }

    companion object {

        const val EVENT_TYPE = "account-delete"

        fun from(payload: AppleS2SPayload): AccountDeletedEvent {
            return AccountDeletedEvent(
                type = payload.events["type"] as String,
                sub = payload.events["sub"] as String,
                eventTime = payload.events["event_time"] as Long
            )
        }
    }
}