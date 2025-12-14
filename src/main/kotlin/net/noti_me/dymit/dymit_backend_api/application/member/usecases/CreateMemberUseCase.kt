package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.application.member.dto.CreateMemberCommand

interface CreateMemberUseCase {

    fun createMember(request: CreateMemberCommand): MemberCreateResult
}
