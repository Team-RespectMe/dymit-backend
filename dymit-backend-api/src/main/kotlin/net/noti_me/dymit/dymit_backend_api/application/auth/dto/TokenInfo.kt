package net.noti_me.dymit.dymit_backend_api.application.auth.dto

import java.time.Instant

class TokenInfo(
    val token: String,
    val expireAt: Instant
) {

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is TokenInfo -> false
            else -> token == other.token
        }
    }

    override fun hashCode(): Int {
        var result = token.hashCode()
        result = 31 * result + expireAt.hashCode()
        return result
    }
}