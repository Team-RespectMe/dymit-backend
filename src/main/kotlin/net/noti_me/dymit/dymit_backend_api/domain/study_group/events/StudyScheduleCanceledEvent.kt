package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import org.springframework.context.ApplicationEvent

/**
 * 스터디 그룹 일정이 취소되는 경우 발행해야하는 이벤트
 * 이 이벤트를 발행하면, 해당 스터디 그룹의 모든 멤버들에게
 * 알림이 전송됩니다.
 */
class StudyScheduleCanceledEvent(
    val group: StudyGroup,
    val schedule: StudySchedule
) : ApplicationEvent(schedule) {
}