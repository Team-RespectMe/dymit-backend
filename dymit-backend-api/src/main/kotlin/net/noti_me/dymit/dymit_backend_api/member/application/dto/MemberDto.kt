package net.noti_me.dymit.dymit_backend_api.member.application.dto

import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.member.domain.OidcIdentity
import java.time.Instant

data class MemberDto(
    val id: String,
    val nickname: String,
    val createdAt: Instant,
    val profileImage: MemberProfileImageVo?=null,
    val oidcIdentities : List<OidcIdentity> = emptyList(),
    val interests: Set<String> = emptySet()
) {

    companion object {
        fun fromEntity(
            entity: Member
        ): MemberDto {
            return MemberDto(
                id = entity.identifier,
                nickname = entity.nickname,
                createdAt = entity.createdAt ?: Instant.now(),
                profileImage = entity.profileImage,
                oidcIdentities = entity.oidcIdentities.toList(),
                interests = entity.interests.toSet()
            )
        }
    }
}
