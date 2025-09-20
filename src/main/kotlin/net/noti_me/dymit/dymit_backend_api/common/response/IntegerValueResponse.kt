package net.noti_me.dymit.dymit_backend_api.common.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "정수 값을 반환하는 응답")
data class IntegerValueResponse(
    @Schema(description = "응답 결과", example = "3")
    val value: Int
): BaseResponse() {

}