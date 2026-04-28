package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

class MemberQueryDto(
    val id: String,
    val nickname: String,
    val oidcInfo: List<OidcIdentity>,
    val profileImageVo: MemberProfileImageVo,
    val interests: Set<String> = emptySet()
) {

    companion object {
        fun fromEntity(member: Member): MemberQueryDto {
            return MemberQueryDto(
                id = member.identifier,
                nickname = member.nickname,
                oidcInfo = member.oidcIdentities.toList(),
                profileImageVo = MemberProfileImageVo(
                    thumbnail = member.profileImage.thumbnail,
                    original = member.profileImage.original,
                    width = member.profileImage.width,
                    height = member.profileImage.height
                ),
                interests = member.interests.toSet()
            )
        }
    }
}