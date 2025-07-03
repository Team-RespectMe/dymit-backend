package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

data class MemberCreateCommand(
    val nickname: String,
    val oidcProvider: OidcProvider,
    val idToken: String
) {
}
