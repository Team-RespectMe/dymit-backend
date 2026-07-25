package net.noti_me.dymit.dymit_backend_api.member.application.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.OidcIdentity

interface UpdateOidcIdentityUseCase {

    fun update(newOidcIdentity: OidcIdentity)
}