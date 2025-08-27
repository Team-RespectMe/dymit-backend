package net.noti_me.dymit.dymit_backend_api.application.user_feed.dto

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import java.time.LocalDateTime

data class UserFeedDto(
    val id: String,
    val memberId: String,
    val message: String,
    val associates: List<AssociatedResource>,
    val createdAt: LocalDateTime,
    val isRead: Boolean
) {
    companion object {
        fun from(userFeed: UserFeed): UserFeedDto {
            return UserFeedDto(
                id = userFeed.id.toHexString(),
                memberId = userFeed.memberId.toHexString(),
                message = userFeed.message,
                associates = userFeed.associates,
                createdAt = userFeed.createdAt ?: LocalDateTime.now(),
                isRead = userFeed.isRead
            )
        }
    }
}
