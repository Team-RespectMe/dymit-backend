package net.noti_me.dymit.dymit_backend_api.member.application.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface DeleteMemberUseCase {

    fun deleteMember(loginMember: MemberInfo, memberId: String)
}
