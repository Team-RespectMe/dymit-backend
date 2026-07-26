package net.noti_me.dymit.dymit_backend_api.feed.adapter.`in`.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.DeleteUserFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.GetUserFeedsUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.MarkUserFeedAsReadUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.DeleteUserFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.GetUserFeedsCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.MarkUserFeedAsReadCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.UserFeedResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 사용자 피드 REST 입력 어댑터입니다.
 *
 * @param getUserFeedsUseCase 개인 피드 조회 유스케이스
 * @param deleteUserFeedUseCase 개인 피드 삭제 유스케이스
 * @param markUserFeedAsReadUseCase 개인 피드 읽음 처리 유스케이스
 */
@Tag(name = "사용자 피드 API", description = "사용자 피드 관련 API")
@RestController
@RequestMapping("/api/v1/user-feeds")
class UserFeedController(
    private val getUserFeedsUseCase: GetUserFeedsUseCase,
    private val deleteUserFeedUseCase: DeleteUserFeedUseCase,
    private val markUserFeedAsReadUseCase: MarkUserFeedAsReadUseCase
) {

    /**
     * 로그인 회원의 피드 목록을 조회합니다.
     *
     * @param loginMember 인증 회원 정보
     * @param cursor 조회 커서
     * @param size 페이지 크기
     * @return 커서 페이지 응답
     */
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        method = "GET",
        summary = "사용자 피드 목록 조회",
        description = "로그인된 사용자의 피드 목록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "피드 목록 조회 성공")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    fun getUserFeeds(
        @LoginMember loginMember: MemberInfo,
        @Parameter(description = "커서 feed id")
        @RequestParam(required = false)
        cursor: String?,
        @RequestParam(defaultValue = "20")
        size: Int
    ): ListResponse<UserFeedResponse> {
        val feedResponses = getUserFeedsUseCase.execute(
            GetUserFeedsCommand(
                memberId = loginMember.memberId,
                cursorId = cursor,
                size = size + 1
            )
        ).map(UserFeedResponse::from)

        return ListResponse.of(
            size = size,
            items = feedResponses,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { size }
            }
        )
    }

    /**
     * 로그인 회원 소유의 피드를 삭제합니다.
     *
     * @param loginMember 인증 회원 정보
     * @param feedId 삭제할 피드 식별자
     */
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        method = "DELETE",
        summary = "사용자 피드 삭제",
        description = "특정 피드를 삭제합니다. 본인의 피드만 삭제할 수 있습니다."
    )
    @ApiResponse(responseCode = "204", description = "피드 삭제 성공")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{feedId}")
    @RolesAllowed("MEMBER", "ADMIN")
    fun deleteUserFeed(
        @LoginMember loginMember: MemberInfo,
        @PathVariable feedId: String
    ) {
        deleteUserFeedUseCase.execute(
            DeleteUserFeedCommand(
                memberId = loginMember.memberId,
                feedId = feedId
            )
        )
    }

    /**
     * 로그인 회원 소유의 피드를 읽음 처리합니다.
     *
     * @param loginMember 인증 회원 정보
     * @param feedId 읽음 처리할 피드 식별자
     */
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        method = "PATCH",
        summary = "사용자 피드 읽음 처리",
        description = "특정 피드를 읽음 상태로 변경합니다. 본인의 피드만 처리할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "피드 읽음 처리 성공")
    @PatchMapping("/{feedId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    fun markFeedAsRead(
        @LoginMember loginMember: MemberInfo,
        @PathVariable feedId: String
    ) {
        markUserFeedAsReadUseCase.execute(
            MarkUserFeedAsReadCommand(
                memberId = loginMember.memberId,
                feedId = feedId
            )
        )
    }
}
