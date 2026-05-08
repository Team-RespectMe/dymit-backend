package net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface GetPostUseCaseV2 {

    fun get(memberInfo: MemberInfo, groupId: String, boardId: String, postId: String): PostDtoV2
}
