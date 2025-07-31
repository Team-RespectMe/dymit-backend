package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface MemberDeleteUsecase {

    fun deleteMember(loginMember: MemberInfo, memberId: String)
}
