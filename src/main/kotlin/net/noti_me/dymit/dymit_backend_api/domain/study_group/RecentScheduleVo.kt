package net.noti_me.dymit.dymit_backend_api.domain.study_group

import org.bson.types.ObjectId
import java.time.LocalDateTime

class RecentScheduleVo(
    val scheduleId: ObjectId,
    val title: String,
    val session: Long,
    val scheduleAt: LocalDateTime
) {
}