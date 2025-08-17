package net.noti_me.dymit.dymit_backend_api.domain.studyGroup

import java.time.LocalDateTime

class RecentPostVo(
    val postId: String,
    val title: String,
    val createdAt: LocalDateTime
) {
}