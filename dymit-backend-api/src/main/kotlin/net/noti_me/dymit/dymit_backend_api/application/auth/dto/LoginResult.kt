package net.noti_me.dymit.dymit_backend_api.application.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse

@Schema(description = "로그인 결과 객체")
class LoginResult(
    val memberId: String,
    val accessToken: String,
    val refreshToken: String
) : BaseResponse() {

}
