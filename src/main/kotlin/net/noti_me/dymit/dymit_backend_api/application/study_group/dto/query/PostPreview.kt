package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.RecentPostVo

class PostPreview(
    val postId: String,
    val title: String,
    val createdAt: java.time.LocalDateTime
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