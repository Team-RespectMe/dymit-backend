package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import java.time.LocalDateTime

data class MemberDto(
    val id: String,
    val nickname: String,
    val createdAt: LocalDateTime,
    val profileImage: MemberProfileImageVo?=null,
    val oidcIdentities : List<OidcIdentity> = emptyList()
) {

    companion object {
        fun fromEntity(
            entity: Member
        ): MemberDto {
            return MemberDto(
                id = entity.identifier,
                nickname = entity.nickname,
                createdAt = entity.createdAt ?: LocalDateTime.now(),
                profileImage = entity.profileImage,
                oidcIdentities = entity.oidcIdentities.toList()
            )
        }
    }
}
