package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import java.time.LocalDateTime

@Schema(
    description = "스터디 그룹 일정 생성/수정 응답",
)
class StudyScheduleCommandResponse(
    @field:Schema(
        description = "스터디 그룹 일정 ID",
        example = "688c25eb2f3a71dcf291aac9",
    )
    val scheduleId: String,
    @field:Schema(
        description = "스터디 그룹 일정 회차",
        example = "1",
    )
    val session: Long,
    @field:Schema(
        description = "스터디 그룹 일정 제목",
        example = "1회차 스터디 모임",
    )
    val title: String,
    @field:Schema(
        description = "스터디 그룹 일정 설명",
        example = "이번 주 스터디 모임입니다.",
    )
    val description: String,
    @field:Schema(
        description = "스터디 그룹 일정 장소",
        example = "{ \"type\": \"ONLINE\", \"value\": \"https://example.com/meeting\" }",
    )
    val location: LocationVo,
    @field:Schema(
        description = "스터디 그룹 일정 시작 시간",
        example = "2030-10-01T10:00:00",
    )
    val scheduleAt: LocalDateTime,
): BaseResponse() {

    companion object {
        fun from(dto: StudyScheduleDto): StudyScheduleCommandResponse {
            return StudyScheduleCommandResponse(
                scheduleId = dto.id,
                session = dto.session,
                title = dto.title,
                description = dto.description,
                location = dto.location,
                scheduleAt = dto.scheduleAt
            )
        }
    }
}