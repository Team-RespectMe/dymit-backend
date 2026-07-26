package net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.dto

import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * Reminder가 일정 알림을 생성할 때 사용하는 일정 정보입니다.
 *
 * @param id 일정 식별자
 * @param groupId 그룹 식별자
 * @param title 일정 제목
 * @param session 일정 회차
 * @param scheduleAt 예정 시각
 */
data class ReminderStudyScheduleDto(
    val id: ObjectId,
    val groupId: ObjectId,
    val title: String,
    val session: Long,
    val scheduleAt: LocalDateTime
)
