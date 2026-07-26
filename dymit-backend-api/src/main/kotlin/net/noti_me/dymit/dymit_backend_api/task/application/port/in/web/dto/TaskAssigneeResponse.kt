package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeMemberDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.common.response.HateoasLink

/**
 * 과제 제출 대상 응답입니다.
 */
@Schema(description = "과제 제출 대상 응답")
class TaskAssigneeResponse(
    @field:Schema(description = "과제 ID")
    val taskId: String,
    @field:Schema(description = "회원 정보")
    val member: TaskAssigneeMemberResponse
) : BaseResponse() {

    companion object {
        /**
         * 과제 제출 대상 응답으로 변환합니다.
         *
         * @param dto 과제 제출 대상 조회 DTO
         * @return 과제 제출 대상 응답
         */
        fun from(dto: TaskAssigneeDto): TaskAssigneeResponse {
            return TaskAssigneeResponse(
                taskId = dto.taskId,
                member = TaskAssigneeMemberResponse.from(dto.member)
            ).also { response ->
                response._links["self"] = HateoasLink(
                    "/api/v1/study-groups/${dto.groupId}/tasks/${dto.taskId}/submissions?assigneeId=${dto.member.id}"
                )
            }
        }
    }
}

/**
 * 과제 제출 대상 회원 응답입니다.
 */
@Schema(description = "과제 제출 대상 회원 정보")
class TaskAssigneeMemberResponse(
    @field:Schema(description = "회원 ID")
    val id: String,
    @field:Schema(description = "회원 닉네임")
    val nickname: String,
    @field:Schema(description = "프로필 이미지 정보")
    val profileImage: TaskProfileImageResponse
) {
    companion object {
        /**
         * 과제 제출 대상 회원 응답으로 변환합니다.
         *
         * @param dto 과제 제출 대상 회원 조회 DTO
         * @return 과제 제출 대상 회원 응답
         */
        fun from(dto: TaskAssigneeMemberDto): TaskAssigneeMemberResponse {
            return TaskAssigneeMemberResponse(
                id = dto.id,
                nickname = dto.nickname,
                profileImage = TaskProfileImageResponse.of(dto.profileImage.type, dto.profileImage.url)
            )
        }
    }
}
