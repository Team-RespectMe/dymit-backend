package net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.BoardCategoryPolicyDtoV2
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface GetBoardCategoriesUseCaseV2 {

    fun getCategories(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String
    ): List<BoardCategoryPolicyDtoV2>
}
