package net.noti_me.dymit.dymit_backend_api.member.domain

import java.time.Instant

data class RefreshToken(
    val token: String,
    val expiresAt: Instant
) {
    fun isExpired(): Boolean {
        return Instant.now().isAfter(expiresAt)
    }
}
