package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

import org.bson.types.ObjectId

data class CreateScheduleCommentCommand(
    val groupId: ObjectId,
    val scheduleId: ObjectId,
    val content: String
)

data class UpdateScheduleCommentCommand(
    val groupId: ObjectId,
    val scheduleId: ObjectId,
    val commentId: ObjectId,
    val content: String
)
