package net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface RemovePostUseCaseV2 {

    fun remove(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    )
}
