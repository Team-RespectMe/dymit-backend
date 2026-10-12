package net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import java.time.Instant

/**
 * 캘린더가 사용하는 스터디 일정 조회 결과입니다.
 *
 * @property id 일정 식별자
 * @property groupId 그룹 식별자
 * @property title 일정 제목
 * @property eventAt 일정 시각
 * @property location 일정 장소
 * @property participantCount 활성 참가 등록 회원 수
 * @property isParticipant 조회 회원의 활성 참가 등록 여부
 */
data class CalendarStudyScheduleDto(
    val id: String,
    val groupId: String,
    val title: String,
    val eventAt: Instant,
    val location: LocationVo = LocationVo(),
    val participantCount: Long = 0L,
    val isParticipant: Boolean = false
)
