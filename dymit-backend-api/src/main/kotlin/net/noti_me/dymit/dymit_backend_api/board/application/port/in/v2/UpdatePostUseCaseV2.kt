package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UpdatePostUseCaseV2 {

    fun execute(memberInfo: MemberInfo, postId: String, command: PostCommandV2): PostDtoV2
}
