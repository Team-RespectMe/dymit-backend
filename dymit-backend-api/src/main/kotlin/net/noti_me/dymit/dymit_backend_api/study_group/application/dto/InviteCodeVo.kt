package net.noti_me.dymit.dymit_backend_api.study_group.application.dto

import java.time.LocalDateTime

data class InviteCodeVo(
    val code: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expireAt: LocalDateTime = LocalDateTime.now()
) {

}