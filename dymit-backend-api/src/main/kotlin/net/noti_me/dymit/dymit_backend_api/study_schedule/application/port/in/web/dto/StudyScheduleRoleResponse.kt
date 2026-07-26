package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.ScheduleRoleDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse

// TODO: image 관련 부분 수정 필요
@Schema(
    description = "스터디 그룹 일정 역할 응답",
)
class StudyScheduleRoleResponse(
    val memberId: String,
    val nickname: String,
    val image: StudyScheduleProfileImageResponse,
    val roles: List<String>,
    val color: String = "#FF3357"
): BaseResponse() {

    companion object {
        fun from(dto: ScheduleRoleDto) : StudyScheduleRoleResponse {
            return StudyScheduleRoleResponse(
                memberId = dto.memberId,
                nickname = dto.nickname,
                image = StudyScheduleProfileImageResponse.of(dto.image.type, dto.image.url),
                roles = dto.roles,
                color = dto.color
            )
        }
    }
}
