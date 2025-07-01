package net.noti_me.dymit.dymit_backend_api.application.member.dto

import java.time.Instant
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

data class MemberDto(
    val id: String,
    val nickname: String,
    val createdAt: Instant,
    val oidcIdentities : List<OidcIdentity> = emptyList()
)
