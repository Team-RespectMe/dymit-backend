package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateCommand

interface MemberCreateUsecase {

    fun createMember(request: MemberCreateCommand): MemberCreateResult

    fun isDuplicatedNickname(nickname: String)
}
