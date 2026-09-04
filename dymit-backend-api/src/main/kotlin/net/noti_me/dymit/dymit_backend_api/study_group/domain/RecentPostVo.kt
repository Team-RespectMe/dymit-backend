package net.noti_me.dymit.dymit_backend_api.study_group.domain

import java.time.Instant

class RecentPostVo(
    val postId: String,
    val title: String,
    val createdAt: Instant
) {

    companion object {
        fun of(
            postId: String,
            title: String,
            createdAt: Instant
        ): RecentPostVo {
            return RecentPostVo(
                postId = postId,
                title = title,
                createdAt = createdAt
            )
        }
    }
}
