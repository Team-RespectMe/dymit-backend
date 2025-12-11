package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleCommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleCommentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "스터디 일정 댓글", description = "스터디 일정 댓글 API")
@RequestMapping("/api/v1/study-groups")
@SecurityRequirement(name = "bearer-jwt")
interface ScheduleCommentApi {

    @Operation(summary = "댓글 생성", description = "스터디 일정에 댓글을 생성합니다.")
    @PostMapping("/{groupId}/schedules/{scheduleId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    fun createComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @RequestBody request: ScheduleCommentCommandRequest
    ): ScheduleCommentResponse

    @Operation(summary = "댓글 수정", description = "스터디 일정 댓글을 수정합니다.")
    @PutMapping("/{groupId}/schedules/{scheduleId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @PathVariable commentId: String,
        @RequestBody request: ScheduleCommentCommandRequest
    ): ScheduleCommentResponse

    @Operation(summary = "댓글 삭제", description = "스터디 일정 댓글을 삭제합니다.")
    @DeleteMapping("/{groupId}/schedules/{scheduleId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @PathVariable commentId: String
    )

    @Operation(summary = "댓글 목록 조회", description = "스터디 일정의 댓글 목록을 조회합니다.")
    @GetMapping("/{groupId}/schedules/{scheduleId}/comments")
    @ResponseStatus(HttpStatus.OK)
    fun getScheduleComments(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @RequestParam(required = false) cursor: String? = null,
        @RequestParam(defaultValue = "20") size: Int
    ): ListResponse<ScheduleCommentResponse>
}
