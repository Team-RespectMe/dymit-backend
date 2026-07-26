package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.BoardCategoryPolicyDtoV2
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface GetBoardCategoriesUseCaseV2 {

    fun execute(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String
    ): List<BoardCategoryPolicyDtoV2>
}
