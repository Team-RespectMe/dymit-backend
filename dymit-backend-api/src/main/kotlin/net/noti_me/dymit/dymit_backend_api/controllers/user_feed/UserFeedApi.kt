package net.noti_me.dymit.dymit_backend_api.controllers.user_feed

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.user_feed.dto.UserFeedResponse

@Tag(name = "사용자 피드 API", description = "사용자 피드 관련 API")
interface UserFeedApi {

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        method = "GET",
        summary = "사용자 피드 목록 조회",
        description = "로그인된 사용자의 피드 목록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "피드 목록 조회 성공")
    fun getUserFeeds(
        loginMember: MemberInfo,
        cursor: String?,
        size: Int
    ): ListResponse<UserFeedResponse>

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        method = "DELETE",
        summary = "사용자 피드 삭제",
        description = "특정 피드를 삭제합니다. 본인의 피드만 삭제할 수 있습니다."
    )
    @ApiResponse(responseCode = "204", description = "피드 삭제 성공")
    fun deleteUserFeed(
        loginMember: MemberInfo,
        feedId: String
    )

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        method = "PATCH",
        summary = "사용자 피드 읽음 처리",
        description = "특정 피드를 읽음 상태로 변경합니다. 본인의 피드만 처리할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "피드 읽음 처리 성공")
    fun markFeedAsRead(
        loginMember: MemberInfo, feedId: String
    )
}
