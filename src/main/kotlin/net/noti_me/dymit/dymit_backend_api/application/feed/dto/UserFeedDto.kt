package net.noti_me.dymit.dymit_backend_api.application.feed.dto

import net.noti_me.dymit.dymit_backend_api.controllers.user_feed.vo.FeedMessageVo
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import java.time.LocalDateTime

data class UserFeedDto(
    val id: String,
    val memberId: String,
    val iconType: IconType,
    val eventName: String = "",
    val messages: List<FeedMessageVo>,
    val associates: List<AssociatedResource>,
    val createdAt: LocalDateTime,
    val isRead: Boolean
) {
    companion object {
        fun from(userFeed: UserFeed): UserFeedDto {
            return UserFeedDto(
                id = userFeed.identifier,
                memberId = userFeed.memberId.toHexString(),
                iconType = userFeed.iconType,
                eventName = userFeed.eventName,
                messages = userFeed.messages.map { FeedMessageVo.from(it) },
                associates = userFeed.associates,
                createdAt = userFeed.createdAt ?: LocalDateTime.now(),
                isRead = userFeed.isRead
            )
        }
    }
}
