package net.noti_me.dymit.dymit_backend_api.supports

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole

fun createMemberInfo(entity: Member): MemberInfo {
    return MemberInfo(
        memberId = entity.identifier,
        nickname = entity.nickname,
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )
}
