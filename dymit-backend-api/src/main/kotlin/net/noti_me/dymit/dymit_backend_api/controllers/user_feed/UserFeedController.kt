package net.noti_me.dymit.dymit_backend_api.controllers.user_feed

import io.swagger.v3.oas.annotations.Parameter
import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.application.feed.UserFeedService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.user_feed.dto.UserFeedResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/user-feeds")
class UserFeedController(
    private val userFeedService: UserFeedService
) : UserFeedApi {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getUserFeeds(
        @LoginMember loginMember: MemberInfo,
        @Parameter(description = "커서 feed id") @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int
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

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{feedId}")
    @RolesAllowed("MEMBER", "ADMIN")
    override fun deleteUserFeed(
        @LoginMember loginMember: MemberInfo,
        @PathVariable feedId: String
    ) {
        userFeedService.deleteUserFeed(loginMember, feedId)
    }

    @PatchMapping("/{feedId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun markFeedAsRead(
        @LoginMember loginMember: MemberInfo,
        @PathVariable feedId: String
    ) {
        userFeedService.markFeedAsRead(loginMember, feedId)
    }
}
