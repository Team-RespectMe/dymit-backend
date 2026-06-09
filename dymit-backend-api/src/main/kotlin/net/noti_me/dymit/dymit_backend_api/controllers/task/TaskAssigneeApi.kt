package net.noti_me.dymit.dymit_backend_api.controllers.task

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskAssigneeResponse

/**
 * 과제 제출 대상 조회 API입니다.
 */
@Tag(name = "과제 API", description = "과제 제출 대상 조회 API")
@SecurityRequirement(name = "bearer-jwt")
interface TaskAssigneeApi {

    /**
     * 과제 제출 대상 목록을 조회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param taskId 과제 ID
     * @return 과제 제출 대상 목록 응답
     */
    @Operation(summary = "과제 제출 대상 목록 조회", description = "과제에 등록된 제출 대상자 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "과제 제출 대상 목록 조회 성공")
    fun getTaskAssignees(
        memberInfo: MemberInfo,
        taskId: String
    ): ListResponse<TaskAssigneeResponse>
}
