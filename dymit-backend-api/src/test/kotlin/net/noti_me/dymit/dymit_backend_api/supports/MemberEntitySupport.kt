package net.noti_me.dymit.dymit_backend_api.supports

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.bson.types.ObjectId

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
    id: ObjectId = ObjectId.get(),
    nickname: String = "nickname",
    oidcIdentities: List<OidcIdentity> = listOf(
        createOidcIdentity()
    ),
    roles: List<MemberRole> = listOf(MemberRole.ROLE_MEMBER),
    interests:Set<String> = setOf<String>()
) : Member {
    return Member(
        id = id,
        nickname = nickname,
        oidcIdentities = oidcIdentities.toMutableSet(),
        profileImage = MemberProfileImageVo(),
        roles = roles.toMutableSet(),
        interests = interests.toMutableSet()
    )
}