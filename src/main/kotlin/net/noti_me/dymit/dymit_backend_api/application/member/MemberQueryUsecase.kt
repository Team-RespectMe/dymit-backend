package net.noti_me.dymit.dymit_backend_api.application.member

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberQueryDto
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

interface MemberQueryUsecase {

    fun getMemberById(loginMember: MemberInfo, memberId: String): MemberQueryDto
}