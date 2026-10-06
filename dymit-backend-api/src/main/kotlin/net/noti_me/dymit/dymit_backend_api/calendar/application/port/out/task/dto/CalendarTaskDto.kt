package net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task.dto

import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import java.time.Instant

/**
 * 캘린더가 사용하는 과제 조회 결과입니다.
 *
 * @property id 과제 식별자
 * @property groupId 관련 일정의 그룹 식별자
 * @property title 과제 제목
 * @property eventAt 과제 마감 시각
 */
data class CalendarTaskDto(
    val id: String,
    val groupId: String,
    val title: String,
    val eventAt: Instant,
    val submissionType: TaskSubmissionType = TaskSubmissionType.OUTPUT
)
