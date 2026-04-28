package net.noti_me.dymit.dymit_backend_api.domain.study_group

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import java.time.LocalDateTime

class RecentPostVo(
    val postId: String,
    val title: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(entity: Post?): RecentPostVo? {
            if ( entity == null) return null

            return RecentPostVo(
                    postId = entity.id.toString(),
                    title = entity.title,
                    createdAt = entity.createdAt ?: LocalDateTime.now()
                )
        }
    }
}