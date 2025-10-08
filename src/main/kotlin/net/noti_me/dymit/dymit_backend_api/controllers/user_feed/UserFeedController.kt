package net.noti_me.dymit.dymit_backend_api.controllers.user_feed

import net.noti_me.dymit.dymit_backend_api.application.feed.UserFeedService
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.user_feed.dto.UserFeedResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class UserFeedController(
    private val userFeedService: UserFeedService
) : UserFeedApi {

    override fun getUserFeeds(
        loginMember: MemberInfo,
        cursor: String?,
        size: Int
    ): ListResponse<UserFeedResponse> {
//         Service에서 size + 1개를 조회 (다음 페이지 존재 여부 확인용)
        val feedDtos = userFeedService.getUserFeeds(loginMember, cursor, size + 1)
        val feedResponses = feedDtos.map { UserFeedResponse.from(it) }
        return ListResponse.of(
            size = size,
            items = feedResponses,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { size }
            }
        )
    }

    override fun deleteUserFeed(loginMember: MemberInfo, feedId: String) {
        TODO("Not yet implemented")
        userFeedService.deleteUserFeed(loginMember, feedId)
    }

    override fun markFeedAsRead(loginMember: MemberInfo, feedId: String) {
        userFeedService.markFeedAsRead(loginMember, feedId)
    }
}
