package net.noti_me.dymit.dymit_backend_api.controllers.task

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskResponse
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskUpdateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionCommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionCommentResponse
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionResponse

@Tag(name = "과제 API", description = "스터디 그룹 과제 관리 API")
@SecurityRequirement(name = "bearer-jwt")
interface TaskApi {

    @Operation(summary = "과제 생성", description = "스터디 그룹 소유자가 과제를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "과제가 생성되었습니다.")
    fun createTask(
        memberInfo: MemberInfo,
        groupId: String,
        @Valid request: TaskCommandRequest
    ): TaskResponse

    @Operation(summary = "과제 수정", description = "스터디 그룹 소유자가 과제를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "과제가 수정되었습니다.")
    fun updateTask(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        @Valid request: TaskUpdateRequest
    ): TaskResponse

    @Operation(summary = "과제 삭제", description = "스터디 그룹 소유자가 과제를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "과제가 삭제되었습니다.")
    fun deleteTask(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String
    )

    @Operation(summary = "과제 목록 조회", description = "그룹 멤버가 과제 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "과제 목록 조회 성공")
    fun getTasks(
        memberInfo: MemberInfo,
        groupId: String
    ): ListResponse<TaskResponse>

    @Operation(summary = "과제 상세 조회", description = "그룹 멤버가 과제 상세를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "과제 상세 조회 성공")
    fun getTask(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String
    ): TaskResponse

    @Operation(summary = "과제 제출 생성", description = "과제 대상자가 제출을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "과제 제출이 생성되었습니다.")
    fun createSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        @Valid request: TaskSubmissionCommandRequest
    ): TaskSubmissionResponse

    @Operation(summary = "과제 제출 수정", description = "과제 대상자가 제출을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "과제 제출이 수정되었습니다.")
    fun updateSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        @Valid request: TaskSubmissionCommandRequest
    ): TaskSubmissionResponse

    @Operation(summary = "과제 제출 철회", description = "과제 대상자가 제출을 철회합니다.")
    @ApiResponse(responseCode = "204", description = "과제 제출이 철회되었습니다.")
    fun withdrawSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String
    )

    @Operation(summary = "체크형 과제 제출 철회", description = "과제 대상자가 assigneeId 기준으로 체크형 제출을 철회합니다.")
    @ApiResponse(responseCode = "204", description = "체크형 과제 제출이 철회되었습니다.")
    fun withdrawCheckSubmissionByAssignee(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        assigneeId: String
    )

    @Operation(summary = "과제 제출 단건 조회", description = "그룹 멤버가 대상자의 제출을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "과제 제출 조회 성공")
    fun getSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        assigneeId: String
    ): TaskSubmissionResponse

    // 요청에 따라 제출 목록 조회 엔드포인트 노출을 중단합니다.
    // @Operation(summary = "과제 제출 목록 조회", description = "그룹 멤버가 제출 목록을 조회합니다.")
    // @ApiResponse(responseCode = "200", description = "과제 제출 목록 조회 성공")
    // fun getSubmissions(
    //     memberInfo: MemberInfo,
    //     groupId: String,
    //     taskId: String
    // ): ListResponse<TaskSubmissionResponse>

    @Operation(summary = "과제 제출 댓글 생성", description = "과제 대상자가 댓글을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "댓글이 생성되었습니다.")
    fun createSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        @Valid request: TaskSubmissionCommentCommandRequest
    ): TaskSubmissionCommentResponse

    @Operation(summary = "과제 제출 댓글 수정", description = "과제 대상자가 댓글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "댓글이 수정되었습니다.")
    fun updateSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String,
        @Valid request: TaskSubmissionCommentCommandRequest
    ): TaskSubmissionCommentResponse

    @Operation(summary = "과제 제출 댓글 삭제", description = "과제 대상자가 댓글을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "댓글이 삭제되었습니다.")
    fun deleteSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String
    )

    @Operation(summary = "과제 제출 댓글 목록 조회", description = "그룹 멤버가 댓글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    fun getSubmissionComments(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String
    ): ListResponse<TaskSubmissionCommentResponse>
}
