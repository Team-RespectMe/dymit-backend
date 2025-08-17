package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule.StudySchedule
import java.time.LocalDateTime

/**
 * Study 일정 서비스 레이어 반환 객체
 * @param id 일정 ID
 * @param title 일정 제목
 * @param description 일정 설명
 * @param scheduleAt 일정 시작 시간
 * @param location 일정 장소 정보
 * @param roles 일정에 할당된 역할 목록
 * @param createdAt 일정 생성 시간
 * @param updatedAt 일정 수정 시간
 */
class StudyScheduleDto(
    val id: String,
    val session: Long,
    val title: String,
    val description: String,
    val scheduleAt: LocalDateTime,
    val location: LocationVo,
    val roles: List<ScheduleRoleDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {

        fun from(entity: StudySchedule): StudyScheduleDto {
            return StudyScheduleDto(
                id = entity.identifier,
                session = entity.session,
                title = entity.title,
                description = entity.description,
                scheduleAt = entity.scheduleAt,
                location = LocationVo.from(entity.location),
                roles = entity.roles.map { ScheduleRoleDto.from(it) },
                createdAt = entity.createdAt ?: LocalDateTime.now(),
                updatedAt = entity.updatedAt ?: LocalDateTime.now(),
            )
        }
    }
}