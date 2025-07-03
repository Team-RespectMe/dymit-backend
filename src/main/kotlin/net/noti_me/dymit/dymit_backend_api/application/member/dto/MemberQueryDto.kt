package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

class MemberQueryDto(
    val id: String,
    val nickname: String,
    val oidcInfo: List<OidcIdentity>,
    val profileImageVo: MemberProfileImageVo
) {

}