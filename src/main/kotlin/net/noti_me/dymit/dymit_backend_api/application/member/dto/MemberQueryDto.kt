package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

class MemberQueryDto(
    val id: String,
    val nickname: String,
    val oidcInfo: List<OidcIdentity>,
    val profileImageVo: MemberProfileImageVo
) {

    companion object {
        fun fromEntity(member: Member): MemberQueryDto {
            return MemberQueryDto(
                id = member.identifier,
                nickname = member.nickname,
                oidcInfo = member.oidcIdentities.toList(),
                profileImageVo = MemberProfileImageVo(
                    url = member.profileImage?.url ?: "",
                    width = member.profileImage?.width ?: 0,
                    height = member.profileImage?.height ?: 0
                )
            )
        }
    }
}