package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query

import net.noti_me.dymit.dymit_backend_api.study_group.domain.RecentPostVo

class PostPreview(
    val postId: String,
    val title: String,
    val createdAt: java.time.Instant
) {

    companion object {
        fun from(post: RecentPostVo): PostPreview {
            return PostPreview(
                postId = post.postId,
                title = post.title,
                createdAt = post.createdAt
            )
        }
    }
}