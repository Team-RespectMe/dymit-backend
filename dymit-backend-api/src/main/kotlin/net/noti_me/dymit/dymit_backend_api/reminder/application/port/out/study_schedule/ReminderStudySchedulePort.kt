package net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule

import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.dto.ReminderStudyScheduleDto
import org.bson.types.ObjectId
import java.time.Instant

/**
 * Reminder 모듈이 일정과 참여자를 조회하는 출력 포트입니다.
 */
interface ReminderStudySchedulePort {

    /**
     * 일정 시각과 커서 범위에 해당하는 일정을 조회합니다.
     *
     * @param start 조회 시작 시각
     * @param end 조회 종료 시각
     * @param cursor 페이지 커서
     * @param limit 최대 조회 수
     * @return Reminder 소유 일정 DTO 목록
     */
    fun findByScheduleAtBetween(
        start: Instant,
        end: Instant,
        cursor: ObjectId?,
        limit: Int
    ): List<ReminderStudyScheduleDto>

    /**
     * 일정 참여 회원 식별자를 조회합니다.
     *
     * @param scheduleId 일정 식별자
     * @return 참여 회원 식별자 목록
     */
    fun getParticipantMemberIds(scheduleId: ObjectId): List<ObjectId>
}
