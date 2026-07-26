package net.noti_me.dymit.dymit_backend_api.study_group.domain

import java.time.LocalDateTime

class RecentPostVo(
    val postId: String,
    val title: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun of(
            postId: String,
            title: String,
            createdAt: LocalDateTime
        ): RecentPostVo {
            return RecentPostVo(
                postId = postId,
                title = title,
                createdAt = createdAt
            )
        }
    }
}
