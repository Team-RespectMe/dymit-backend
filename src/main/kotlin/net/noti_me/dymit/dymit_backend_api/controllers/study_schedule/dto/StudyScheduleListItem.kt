package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleSummaryDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import java.time.LocalDateTime

@Schema(
    description = "스터디 그룹 일정 목록 항목",
)
class StudyScheduleListItem(
    @field: Schema(
        description = "스터디 그룹 일정 ID",
        example = "688c25eb2f3a71dcf291aac9",
    )
    val id: String,
    @field: Schema(
        description = "스터디 그룹 일정 제목",
        example = "1회차 스터디 모임",
    )
    val title: String,
    @field: Schema(
        description = "스터디 그룹 일정 설명",
        example = "이번 주 스터디 모임입니다.",
    )
    val description: String,
    @field: Schema(
        description = "스터디 그룹 회차 정보",
        example = "1"
    )
    val session: Long,
    @field: Schema(
        description = "스터디 그룹 일정 예정 시간",
        example = "2023-10-01T14:00:00",
    )
    val scheduleAt: LocalDateTime,
    @field: Schema(
        description = "스터디 그룹 일정 장소 정보",
        example = "{type: 'OFFLINE', value: '서울 강남구 역삼동 123-45', link: null}",
    )
    val location: LocationVo = LocationVo(),
    @field: Schema(
        description = "참여 인원 수",
        example = "5",
    )
    val participantCount: Long = 0L,
    @field: Schema(
        description = "스터디 그룹 일정 역할 목록",
    )
    val roles: List<StudyScheduleRoleResponse> = emptyList()
): BaseResponse() {

    companion object {
        fun from(dto: StudyScheduleSummaryDto): StudyScheduleListItem {
            return StudyScheduleListItem(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                session = dto.session,
                scheduleAt = dto.scheduleAt,
                participantCount = dto.participantCount,
                roles = dto.roles.map { StudyScheduleRoleResponse.from(it) }
            )
        }
    }
}