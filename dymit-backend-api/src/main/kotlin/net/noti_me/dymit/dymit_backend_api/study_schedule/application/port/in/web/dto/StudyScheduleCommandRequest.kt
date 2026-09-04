package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleCreateCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import java.time.Instant

@Schema(description = "스터디 그룹 일정 생성 요청")
@Sanitize
class StudyScheduleCommandRequest(
    @field: Schema(description = "스터디 일정 제목", example = "1회차 스터디 모임")
    @field: NotEmpty(message = "스터디 일정 제목은 비어있을 수 없습니다.")
    val title: String,
    @field: Schema(description = "스터디 일정 설명", example = "이번 주 스터디 모임입니다.")
    val description: String,
    @field: Schema(description = "스터디 일정 장소", example = "{ \"type\": \"ONLINE\", \"value\": \"줌 미팅\", \"link\": \"https://zoom.us/j/1234567890\" }")
    val location: LocationVo,
    @field: Schema(description = "스터디 일정 시작 시간", example = "2030-10-01T10:00:00")
    val scheduleAt: Instant,
    @field: Schema(description = "스터디 일정 역할 목록")
    val scheduleRoles: List<RoleAssignment>,
) {

    fun toCreateCommand(): StudyScheduleCreateCommand {
        return StudyScheduleCreateCommand(
            title = title,
            description = description,
            location = location,
            scheduleAt = scheduleAt,
            roles = scheduleRoles
        )
    }

    fun toUpdateCommand(): StudyScheduleUpdateCommand {
        return StudyScheduleUpdateCommand(
            title = title,
            description = description,
            location = location,
            scheduleAt = scheduleAt,
            roles = scheduleRoles
        )
    }
}
