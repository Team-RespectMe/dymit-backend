package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.MemberCreateUsecase

class MemberCreateUsecaseImpl : MemberCreateUsecase {

    override fun createMember(request: MemberCreateCommand): MemberCreateResult { 
        TODO("Not yet implemented")
    }
}