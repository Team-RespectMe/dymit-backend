package net.noti_me.dymit.dymit_backend_api.controllers.user_feed

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.user_feed.dto.UserFeedResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "사용자 피드 API", description = "사용자 피드 관련 API")
@RequestMapping("/api/v1/user-feeds")
interface UserFeedApi {

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        summary = "사용자 피드 목록 조회",
        description = "로그인된 사용자의 피드 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "피드 목록 조회 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    fun getUserFeeds(
        @LoginMember loginMember: MemberInfo,
        @Parameter(description = "커서 feed id") @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int
    ): ListResponse<UserFeedResponse>

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        summary = "사용자 피드 삭제",
        description = "특정 피드를 삭제합니다. 본인의 피드만 삭제할 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "피드 삭제 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패"
            ),
            ApiResponse(
                responseCode = "403",
                description = "삭제 권한 없음"
            ),
            ApiResponse(
                responseCode = "404",
                description = "피드를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{feedId}")
    fun deleteUserFeed(
        @LoginMember loginMember: MemberInfo,
        @Parameter(description = "피드 ID", required = true)
        @PathVariable feedId: String
    )

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        summary = "사용자 피드 읽음 처리",
        description = "특정 피드를 읽음 상태로 변경합니다. 본인의 피드만 처리할 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "피드 읽음 처리 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패"
            ),
            ApiResponse(
                responseCode = "403",
                description = "읽음 처리 권한 없음"
            ),
            ApiResponse(
                responseCode = "404",
                description = "피드를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{feedId}/read")
    fun markFeedAsRead(
        @LoginMember loginMember: MemberInfo,
        @Parameter(description = "피드 ID", required = true)
        @PathVariable feedId: String
    )
}
