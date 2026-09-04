package net.noti_me.dymit.dymit_backend_api.study_group.application.dto

import java.time.Instant

data class InviteCodeVo(
    val code: String = "",
    val createdAt: Instant = Instant.now(),
    val expireAt: Instant = Instant.now()
) {

}
