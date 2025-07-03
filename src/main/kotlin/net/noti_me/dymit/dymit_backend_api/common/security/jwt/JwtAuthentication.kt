package net.noti_me.dymit.dymit_backend_api.common.security.jwt

import org.springframework.security.core.GrantedAuthority
import java.security.Principal

class JwtAuthentication(
    val id: String,
    val nickname: String,
    val roles: MutableCollection<out GrantedAuthority>,
): Principal {

    override fun getName(): String? {
        return nickname
    }
}