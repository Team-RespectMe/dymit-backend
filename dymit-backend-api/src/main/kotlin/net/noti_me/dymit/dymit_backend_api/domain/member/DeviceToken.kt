
package net.noti_me.dymit.dymit_backend_api.domain.member

import java.time.Instant


class DeviceToken(
    val token: String,
    var isActive: Boolean = true,
) {

    override fun equals(other: Any?): Boolean {
        if ( this === other) return true
        if (other !is DeviceToken) return false

        return token == other.token
    }

    override fun hashCode(): Int {
        return token.hashCode()
    }
}