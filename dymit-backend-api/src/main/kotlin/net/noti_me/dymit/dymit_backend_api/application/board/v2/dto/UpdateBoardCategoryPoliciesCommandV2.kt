package net.noti_me.dymit.dymit_backend_api.application.board.v2.dto

import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryPolicy

/**
 * 게시판 카테고리 정책 수정 커맨드입니다.
 */
class UpdateBoardCategoryPoliciesCommandV2(
    val policies: List<BoardCategoryPolicy>
)
