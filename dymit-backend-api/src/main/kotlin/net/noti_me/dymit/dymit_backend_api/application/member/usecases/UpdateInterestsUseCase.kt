package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.application.member.dto.UpdateInterestsCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UpdateInterestsUseCase {

    fun updateInterests(
        loginMember: MemberInfo,
        command: UpdateInterestsCommand
    ): MemberDto
}