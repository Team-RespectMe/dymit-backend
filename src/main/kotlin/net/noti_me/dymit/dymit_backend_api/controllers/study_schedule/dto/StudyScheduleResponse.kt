package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleDetailDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import java.time.LocalDateTime

@Schema(
    description = "스터디 그룹 일정 단건 조회 응답",
)
class StudyScheduleResponse(
    @field: Schema(
        description = "일정 ID",
    )
    val id: String,
    @field: Schema(
        description = "일정 회차 정보",
    )
    val session: Long,
    @field: Schema(
        description = "일정 제목",
    )
    val title: String,
    @field: Schema(
        description = "일정 설명",
    )
    val description: String,
    @field: Schema(
        description = "일정 시작 시간",
    )
    val scheduleAt: LocalDateTime,
    @field: Schema(
        description = "일정 장소 정보",
    )
    val location: LocationVo,
    @field: Schema(
        description = "일정에 할당된 역할 목록",
    )
    val roles: List<StudyScheduleRoleResponse>,
    @field: Schema(
        description = "일정 참여자 목록",
    )
    val participants: List<ScheduleParticipantResponse>,
): BaseResponse() {

    companion object {
        fun from(dto: StudyScheduleDetailDto) : StudyScheduleResponse {
            return StudyScheduleResponse(
                id = dto.id,
                session = dto.session,
                title = dto.title,
                description = dto.description,
                scheduleAt = dto.scheduleAt,
                location = dto.location,
                roles = dto.roles.map { StudyScheduleRoleResponse.from(it) },
                participants = dto.participants.map{
                    ScheduleParticipantResponse.from(it)
                }
            )
        }
    }
}