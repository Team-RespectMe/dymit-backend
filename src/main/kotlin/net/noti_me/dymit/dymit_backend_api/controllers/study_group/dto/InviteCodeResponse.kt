package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import java.time.LocalDateTime

@Schema(description = "스터디 그룹 초대 코드 응답 DTO")
data class InviteCodeResponse(
    @Schema(description = "초대 코드 ID")
    val code: String,
    @Schema(description = "초대 코드 생성 일시")
    val createdAt: LocalDateTime,
    @Schema(description = "초대 코드 만료 일시")
    val expireAt: LocalDateTime
): BaseResponse() {
}