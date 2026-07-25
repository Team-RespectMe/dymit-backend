package net.noti_me.dymit.dymit_backend_api.member.application.usecases

import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberQueryDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface QueryMemberUseCase {

    fun getMemberById(loginMember: MemberInfo, memberId: String): MemberQueryDto
}