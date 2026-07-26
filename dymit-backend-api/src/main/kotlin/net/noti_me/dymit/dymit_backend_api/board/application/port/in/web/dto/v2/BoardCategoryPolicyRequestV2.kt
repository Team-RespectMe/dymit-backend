package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.v2

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardCategoryPolicy
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory

/**
 * 게시판 카테고리 정책 수정 요청 아이템 DTO입니다.
 */
@Schema(description = "게시판 카테고리 정책 수정 요청 아이템 DTO")
class BoardCategoryPolicyRequestV2(
    val category: PostCategory,
    val enabled: Boolean,
    val writePolicy: BoardCategoryWritePolicy
) {

    fun toDomain(): BoardCategoryPolicy {
        return BoardCategoryPolicy(
            category = category,
            enabled = enabled,
            writePolicy = writePolicy
        )
    }
}
