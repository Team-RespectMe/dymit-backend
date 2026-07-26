package net.noti_me.dymit.dymit_backend_api.study_group.domain

import org.bson.types.ObjectId
import java.time.LocalDateTime

class RecentScheduleVo(
    val scheduleId: ObjectId,
    val title: String,
    val session: Long,
    val scheduleAt: LocalDateTime
) {

    companion object {
        fun of(
            scheduleId: String,
            title: String,
            session: Long,
            scheduleAt: LocalDateTime
        ): RecentScheduleVo {
            return RecentScheduleVo(
                scheduleId = ObjectId(scheduleId),
                title = title,
                session = session,
                scheduleAt = scheduleAt
            )
        }
    }
}
