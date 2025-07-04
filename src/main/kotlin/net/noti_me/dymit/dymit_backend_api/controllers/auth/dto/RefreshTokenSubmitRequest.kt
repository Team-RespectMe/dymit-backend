package net.noti_me.dymit.dymit_backend_api.controllers.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

@Schema(name = "RefreshTokenSubmitRequest", description = "리프레시 토큰 제출 요청 DTO")
class RefreshTokenSubmitRequest(
    @field: NotEmpty
    val refreshToken: String
) {
}