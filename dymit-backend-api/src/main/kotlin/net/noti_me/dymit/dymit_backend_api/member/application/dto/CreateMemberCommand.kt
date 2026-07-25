package net.noti_me.dymit.dymit_backend_api.member.application.dto

import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcProvider

data class CreateMemberCommand(
    val nickname: String,
    val oidcProvider: OidcProvider,
    val idToken: String,
    val interests: List<String>,
) {
}
