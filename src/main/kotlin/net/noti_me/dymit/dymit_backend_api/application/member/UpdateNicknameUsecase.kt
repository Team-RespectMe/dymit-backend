package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberNicknameUpdateCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UpdateNicknameUsecase {

    fun updateNickname(
        loginMember: MemberInfo,
        memberId: String,
        command: MemberNicknameUpdateCommand
    ): MemberDto
}
