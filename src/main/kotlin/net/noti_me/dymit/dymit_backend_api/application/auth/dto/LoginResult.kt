package net.noti_me.dymit.dymit_backend_api.application.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse

@Schema(description = "로그인 결과 객체")
class LoginResult(
    @Schema(description = "사용자 ID", example = "6884dec1beed715fdd4a7639")
    val memberId: String,
    @Schema(description = "access token")
    val accessToken: String,
    @Schema(description = "refresh token")
    val refreshToken: String
) : BaseResponse() {

}
