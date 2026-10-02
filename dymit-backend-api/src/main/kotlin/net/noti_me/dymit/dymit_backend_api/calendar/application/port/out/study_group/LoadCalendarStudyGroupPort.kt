package net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group

import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group.dto.CalendarStudyGroupDto

/**
 * 캘린더 조회에 필요한 현재 소속 스터디 그룹을 불러오는 출력 포트입니다.
 */
interface LoadCalendarStudyGroupPort {

    /**
     * 회원이 현재 소속된 삭제되지 않은 그룹을 불러옵니다.
     *
     * @param memberId 회원 식별자
     * @return 현재 소속 그룹 목록
     */
    fun loadActiveGroupsByMemberId(memberId: String): List<CalendarStudyGroupDto>
}
