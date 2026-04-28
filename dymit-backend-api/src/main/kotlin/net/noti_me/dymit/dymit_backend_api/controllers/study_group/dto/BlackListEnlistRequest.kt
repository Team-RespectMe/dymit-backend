package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize

@Schema(description = "블랙리스트 등록 요청 DTO")
@Sanitize
class BlackListEnlistRequest(
    @Schema(description = "삭제될 회원의 memberId", example = "64b64c8f5f3c2a6d9f0e4b1a")
    @field: NotEmpty(message = "타겟 ID는 비어 있을 수 없습니다.")
    val targetId: String,
    @Schema(description = "블랙리스트 등록 사유", example = "부적절한 언행")
    @field: NotEmpty(message = "사유는 비어 있을 수 없습니다.")
    val reason: String
) {
}