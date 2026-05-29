package net.noti_me.dymit.dymit_backend_api.domain.task.event

import org.bson.types.ObjectId

/**
 * 과제 생성 이벤트입니다.
 */
class TaskCreatedEvent(
    val taskId: ObjectId,
    val groupId: ObjectId,
    val scheduleId: ObjectId
)
