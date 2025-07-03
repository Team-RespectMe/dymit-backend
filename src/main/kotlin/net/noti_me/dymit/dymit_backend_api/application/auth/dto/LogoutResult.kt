package net.noti_me.dymit.dymit_backend_api.application.auth.dto

import java.time.Instant

data class LogoutResult(
    val token: String,
    val success: Boolean,
    val invalidatedAt: Instant = Instant.now()
)
