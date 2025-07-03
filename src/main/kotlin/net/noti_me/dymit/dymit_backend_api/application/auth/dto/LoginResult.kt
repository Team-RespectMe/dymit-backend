package net.noti_me.dymit.dymit_backend_api.application.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 결과 객체")
data class LoginResult(
    @Schema(description = "access token")
    val accessToken: String,
    @Schema(description = "refresh token")
    val refreshToken: String
)
