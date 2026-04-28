package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

interface UpdateOidcIdentityUseCase {

    fun update(newOidcIdentity: OidcIdentity)
}