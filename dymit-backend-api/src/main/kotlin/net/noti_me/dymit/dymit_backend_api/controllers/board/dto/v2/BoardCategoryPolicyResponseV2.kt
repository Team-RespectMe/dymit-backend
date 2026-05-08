package net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.BoardCategoryPolicyDtoV2
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory

/**
 * 게시판 카테고리 정책 응답 DTO입니다.
 */
@Schema(description = "게시판 카테고리 정책 응답 DTO")
class BoardCategoryPolicyResponseV2(
    val category: PostCategory,
    val enabled: Boolean,
    val writePolicy: BoardCategoryWritePolicy
) {

    companion object {
        fun from(dto: BoardCategoryPolicyDtoV2): BoardCategoryPolicyResponseV2 {
            return BoardCategoryPolicyResponseV2(
                category = dto.category,
                enabled = dto.enabled,
                writePolicy = dto.writePolicy
            )
        }
    }
}
