package net.noti_me.dymit.dymit_backend_api.application.auth.dto

data class LoginResult(
    val accessToken: String,
    val refreshToken: String
)
