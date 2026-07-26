package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudyScheduleProfileImageType
import org.bson.types.ObjectId

data class ScheduleCommentWriterUpdateDto(
    val memberId: ObjectId,
    val nickname: String,
    val profileImageType: StudyScheduleProfileImageType,
    val profileImageUrl: String
)
