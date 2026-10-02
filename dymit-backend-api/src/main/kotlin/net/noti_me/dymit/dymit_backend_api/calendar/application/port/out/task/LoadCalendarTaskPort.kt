package net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task

import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task.dto.CalendarTaskDto
import java.time.Instant

/**
 * 캘린더 조회에 필요한 회원별 과제를 불러오는 출력 포트입니다.
 */
interface LoadCalendarTaskPort {

    /**
     * 현재 소속 그룹에서 회원이 제출 대상인 과제를 불러옵니다.
     *
     * @param memberId 회원 식별자
     * @param groupIds 현재 소속 그룹 식별자 목록
     * @param startInclusive 조회 시작 시각
     * @param endExclusive 조회 종료 시각
     * @return 캘린더 과제 목록
     */
    fun loadActiveTasks(
        memberId: String,
        groupIds: List<String>,
        startInclusive: Instant,
        endExclusive: Instant
    ): List<CalendarTaskDto>
}
