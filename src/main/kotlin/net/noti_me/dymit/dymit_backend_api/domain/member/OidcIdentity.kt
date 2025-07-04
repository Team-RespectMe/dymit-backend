package net.noti_me.dymit.dymit_backend_api.domain.member

data class OidcIdentity(
    val provider: String,
    val subject: String,
    val email: String? = null,
//    val name: String? = null,
)