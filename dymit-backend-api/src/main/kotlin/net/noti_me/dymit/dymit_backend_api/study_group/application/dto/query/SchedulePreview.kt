package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query

import net.noti_me.dymit.dymit_backend_api.study_group.domain.RecentScheduleVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.study_schedule.dto.StudyGroupScheduleData
import java.time.Instant

data class SchedulePreview(
    val title: String,
    val session: Long,
    val startAt: Instant
) {

    companion object {

        fun from(schedule: StudyGroupScheduleData): SchedulePreview {
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
