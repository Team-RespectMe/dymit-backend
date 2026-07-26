package net.noti_me.dymit.dymit_backend_api.study_schedule.domain

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupProfileImageDto
import org.bson.types.ObjectId

class ScheduleCommentWriter(
    val id: ObjectId,
    var nickname: String,
    var image: StudyScheduleGroupProfileImageDto
)
