package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import java.time.LocalDateTime

@Schema(description = "스터디 그룹 일정 생성 요청")
class StudyScheduleCommandRequest(
    @field: Schema(description = "스터디 일정 제목", example = "1회차 스터디 모임")
    @field: NotEmpty(message = "스터디 일정 제목은 비어있을 수 없습니다.")
    val title: String,
    @field: Schema(description = "스터디 일정 설명", example = "이번 주 스터디 모임입니다.")
    val description: String,
    @field: Schema(description = "스터디 일정 장소", example = "{ \"type\": \"ONLINE\", \"value\": \"https://example.com/meeting\" }")
    val location: LocationVo,
    @field: Schema(description = "스터디 일정 시작 시간", example = "2030-10-01T10:00:00")
    val scheduleAt: LocalDateTime,
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