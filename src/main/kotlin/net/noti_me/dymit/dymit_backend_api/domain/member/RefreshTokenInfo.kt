package net.noti_me.dymit.dymit_backend_api.domain.member

import java.time.Instant

data class RefreshTokenInfo(
    val token: String,
    val expiresAt: Instant
)
