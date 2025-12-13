package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleCommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleCommentResponse

@Tag(name = "스터디 일정 댓글", description = "스터디 일정 댓글 API")
@SecurityRequirement(name = "bearer-jwt")
interface ScheduleCommentApi {

    @Operation(summary = "댓글 생성", description = "스터디 일정에 댓글을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "댓글이 성공적으로 생성되었습니다.")
    fun createComment(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        request: ScheduleCommentCommandRequest
    ): ScheduleCommentResponse

    @Operation(summary = "댓글 수정", description = "스터디 일정 댓글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 수정되었습니다.")
    fun updateComment(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        commentId: String,
        request: ScheduleCommentCommandRequest
    ): ScheduleCommentResponse

    @Operation(summary = "댓글 삭제", description = "스터디 일정 댓글을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "댓글이 성공적으로 삭제되었습니다.")
    fun deleteComment(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        commentId: String
    )

    @Operation(summary = "댓글 목록 조회", description = "스터디 일정의 댓글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 목록이 성공적으로 조회되었습니다.")
    fun getScheduleComments(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        cursor: String? = null,
        size: Int
    ): ListResponse<ScheduleCommentResponse>
}
