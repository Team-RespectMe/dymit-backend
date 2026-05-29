package net.noti_me.dymit.dymit_backend_api.domain.task.event

import org.bson.types.ObjectId

/**
 * 과제 삭제 이벤트입니다.
 */
class TaskDeletedEvent(
    val taskId: ObjectId,
    val groupId: ObjectId,
    val scheduleId: ObjectId,
    val deletedByScheduleEvent: Boolean
)
