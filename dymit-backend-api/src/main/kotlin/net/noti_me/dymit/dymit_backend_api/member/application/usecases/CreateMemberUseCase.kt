package net.noti_me.dymit.dymit_backend_api.member.application.usecases

import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.member.application.dto.CreateMemberCommand

interface CreateMemberUseCase {

    fun createMember(request: CreateMemberCommand): MemberCreateResult
}
