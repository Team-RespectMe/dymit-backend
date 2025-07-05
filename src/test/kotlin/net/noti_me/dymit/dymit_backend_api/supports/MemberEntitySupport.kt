package net.noti_me.dymit.dymit_backend_api.supports

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

fun createOidcIdentity(
    provider: String = "GOOGLE",
    subject: String = "subject-id",
    email: String = "subject-id@email.com"
): OidcIdentity {
    return OidcIdentity(
        provider = provider,
        subject = subject,
        email = email
    )
}

fun createMemberEntity(
    id: String = "member-id",
    nickname: String = "nickname",
    oidcIdentities: List<OidcIdentity> = listOf(
        createOidcIdentity()
    )
) : Member {
    return Member(
        id = id,
        nickname = nickname,
        oidcIdentities = oidcIdentities.toMutableSet(),
        profileImage = null
    )
}