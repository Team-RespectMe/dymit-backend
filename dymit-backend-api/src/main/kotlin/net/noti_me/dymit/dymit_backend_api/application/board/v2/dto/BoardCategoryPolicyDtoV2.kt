package net.noti_me.dymit.dymit_backend_api.application.board.v2.dto

import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryPolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory

/**
 * 게시판 카테고리 정책 V2 응답 DTO입니다.
 */
class BoardCategoryPolicyDtoV2(
    val category: PostCategory,
    val enabled: Boolean,
    val writePolicy: BoardCategoryWritePolicy
) {

    companion object {
        fun from(policy: BoardCategoryPolicy): BoardCategoryPolicyDtoV2 {
            return BoardCategoryPolicyDtoV2(
                category = policy.category,
                enabled = policy.enabled,
                writePolicy = policy.writePolicy
            )
        }
    }
}
