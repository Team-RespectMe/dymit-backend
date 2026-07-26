package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto

import net.noti_me.dymit.dymit_backend_api.board.domain.BoardCategoryPolicy
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory

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
