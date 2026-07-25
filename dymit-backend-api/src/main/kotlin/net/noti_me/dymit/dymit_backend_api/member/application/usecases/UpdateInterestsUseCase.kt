package net.noti_me.dymit.dymit_backend_api.member.application.usecases

import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateInterestsCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UpdateInterestsUseCase {

    fun updateInterests(
        loginMember: MemberInfo,
        command: UpdateInterestsCommand
    ): MemberDto
}