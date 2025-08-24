package net.noti_me.dymit.dymit_backend_api.domain.studyGroup

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import java.time.LocalDateTime

class RecentPostVo(
    val postId: String,
    val title: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(entity: Post): RecentPostVo {
            return RecentPostVo(
                postId = entity.id.toString(),
                title = entity.title,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}