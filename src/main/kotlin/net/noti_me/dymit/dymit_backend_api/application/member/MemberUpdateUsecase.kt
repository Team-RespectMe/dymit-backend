package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberNicknameUpdateCommand

interface MemberUpdateUsecase {

    fun updateNickname(id: String, request: MemberNicknameUpdateCommand): MemberDto
}
