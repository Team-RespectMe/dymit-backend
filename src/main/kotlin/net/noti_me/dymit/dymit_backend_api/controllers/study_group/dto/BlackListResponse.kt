package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.BlacklistDto

@Schema(description = "차단 목록에 등록된 회원 DTO")
data class BlackListResponse(
    @Schema(description = "차단 된 회원 ID", example = "64b64c8f5f3c2a6d9f0e4b1a")
    val memberId: String,
    @Schema(description = "차단 된 회원 닉네임", example = "nickname")
    val nickname: String,
    @Schema(description = "차단 사유", example = "부적절한 언행")
    val reason: String
) {

    companion object {
        fun from(dto: BlacklistDto): BlackListResponse {
            return BlackListResponse(
                memberId = dto.id,
                nickname = dto.nickname,
                reason = dto.reason
            )
        }
    }
}