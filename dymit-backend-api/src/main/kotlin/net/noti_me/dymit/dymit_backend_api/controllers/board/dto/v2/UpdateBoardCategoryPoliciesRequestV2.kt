package net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.UpdateBoardCategoryPoliciesCommandV2

/**
 * 게시판 카테고리 정책 수정 요청 DTO입니다.
 */
@Schema(description = "게시판 카테고리 정책 수정 요청 DTO")
class UpdateBoardCategoryPoliciesRequestV2(
    @field:NotEmpty(message = "카테고리 정책은 비어 있을 수 없습니다.")
    @field:Valid
    val policies: List<BoardCategoryPolicyRequestV2>
) {

    fun toCommand(): UpdateBoardCategoryPoliciesCommandV2 {
        return UpdateBoardCategoryPoliciesCommandV2(
            policies = policies.map { it.toDomain() }
        )
    }
}
