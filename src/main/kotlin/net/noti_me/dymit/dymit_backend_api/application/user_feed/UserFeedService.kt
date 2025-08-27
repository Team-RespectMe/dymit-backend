package net.noti_me.dymit.dymit_backend_api.application.user_feed

import net.noti_me.dymit.dymit_backend_api.application.user_feed.dto.UserFeedDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UserFeedService {

    fun getUserFeeds(
        memberInfo: MemberInfo,
        cursorId: String?,
        size: Int
    ): List<UserFeedDto>

    fun deleteUserFeed(memberInfo: MemberInfo, feedId: String)

    fun markFeedAsRead(memberInfo: MemberInfo, feedId: String)
}
