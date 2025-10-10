package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentScheduleVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import java.time.LocalDateTime

data class SchedulePreview(
    val title: String,
    val session: Long,
    val startAt: LocalDateTime
) {

    companion object {

        fun from(schedule: StudySchedule): SchedulePreview {
            return SchedulePreview(
                title = schedule.title,
                session = schedule.session,
                startAt = schedule.scheduleAt
            )
        }

        fun from(vo: RecentScheduleVo): SchedulePreview {
            return SchedulePreview(
                title = vo.title,
                session = vo.session,
                startAt = vo.scheduleAt
            )
        }
    }

}