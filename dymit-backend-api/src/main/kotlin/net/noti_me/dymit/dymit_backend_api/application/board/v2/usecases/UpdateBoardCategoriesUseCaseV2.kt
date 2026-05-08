package net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.BoardCategoryPolicyDtoV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.UpdateBoardCategoryPoliciesCommandV2
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UpdateBoardCategoriesUseCaseV2 {

    fun updateCategories(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        command: UpdateBoardCategoryPoliciesCommandV2
    ): List<BoardCategoryPolicyDtoV2>
}
