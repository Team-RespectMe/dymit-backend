package net.noti_me.dymit.dymit_backend_api.application.feed

import net.noti_me.dymit.dymit_backend_api.application.feed.dto.UserFeedDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

interface UserFeedService {

    fun createUserFeed(userFeed: UserFeed): UserFeed

    fun getUserFeeds(
        memberInfo: MemberInfo,
        cursorId: String?,
        size: Int
    ): List<UserFeedDto>

    fun deleteUserFeed(memberInfo: MemberInfo, feedId: String)

    fun markFeedAsRead(memberInfo: MemberInfo, feedId: String)
}
