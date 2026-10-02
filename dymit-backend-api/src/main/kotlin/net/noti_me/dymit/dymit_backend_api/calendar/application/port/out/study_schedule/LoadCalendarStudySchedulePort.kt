package net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule

import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule.dto.CalendarStudyScheduleDto
import java.time.Instant

/**
 * 캘린더 조회에 필요한 스터디 일정을 불러오는 출력 포트입니다.
 */
interface LoadCalendarStudySchedulePort {

    /**
     * 회원이 참가 등록한 일정 중 지정한 그룹과 반개구간에 속한 활성 일정을 불러옵니다.
     *
     * @param memberId 회원 식별자
     * @param groupIds 그룹 식별자 목록
     * @param startInclusive 조회 시작 시각
     * @param endExclusive 조회 종료 시각
     * @return 스터디 일정 목록
     */
    fun loadActiveSchedules(
        memberId: String,
        groupIds: List<String>,
        startInclusive: Instant,
        endExclusive: Instant
    ): List<CalendarStudyScheduleDto>
}
