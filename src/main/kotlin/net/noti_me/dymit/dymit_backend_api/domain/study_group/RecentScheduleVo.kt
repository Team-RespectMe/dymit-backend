package net.noti_me.dymit.dymit_backend_api.domain.study_group

import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import org.bson.types.ObjectId
import java.time.LocalDateTime

class RecentScheduleVo(
    val scheduleId: ObjectId,
    val title: String,
    val session: Long,
    val scheduleAt: LocalDateTime
) {

    companion object {
        fun from(schedule: StudySchedule): RecentScheduleVo {
            assert(schedule.id!=null) { "Schedule ID cannot be null" }

            return RecentScheduleVo(
                scheduleId = schedule.id!!,
                title = schedule.title,
                session = schedule.session,
                scheduleAt = schedule.scheduleAt
            )
        }
    }
}