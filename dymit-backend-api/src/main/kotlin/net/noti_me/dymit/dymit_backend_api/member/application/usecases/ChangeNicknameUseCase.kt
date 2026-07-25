package net.noti_me.dymit.dymit_backend_api.member.application.usecases

import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateNicknameCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface ChangeNicknameUseCase {

    fun updateNickname(
        loginMember: MemberInfo,
        memberId: String,
        command: UpdateNicknameCommand
    ): MemberDto
}
