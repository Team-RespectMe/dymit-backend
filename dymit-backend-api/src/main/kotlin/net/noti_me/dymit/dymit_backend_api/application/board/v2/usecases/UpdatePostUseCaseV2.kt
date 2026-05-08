package net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UpdatePostUseCaseV2 {

    fun update(memberInfo: MemberInfo, postId: String, command: PostCommandV2): PostDtoV2
}
