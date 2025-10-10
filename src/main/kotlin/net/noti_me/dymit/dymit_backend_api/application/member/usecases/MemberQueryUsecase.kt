package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberQueryDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface MemberQueryUsecase {

    fun getMemberById(loginMember: MemberInfo, memberId: String): MemberQueryDto
}