package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory

interface GetBoardPostsUseCaseV2 {

    fun execute(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        cursor: String?,
        size: Int,
        category: PostCategory?
    ): List<PostDtoV2>
}
