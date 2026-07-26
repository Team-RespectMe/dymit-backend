package net.noti_me.dymit.dymit_backend_api.task.domain.event

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import org.bson.types.ObjectId

/**
 * 과제 수정 이벤트입니다.
 *
 * @property taskId 수정된 과제 ID
 * @property groupId 과제가 속한 스터디 그룹 ID
 * @property scheduleId 과제가 연결된 일정 ID
 * @property task 수정된 과제 aggregate
 * @property group 조회된 스터디 그룹 aggregate
 */
class TaskModifiedEvent(
    val taskId: ObjectId,
    val groupId: ObjectId,
    val scheduleId: ObjectId,
    val task: Task,
    val group: StudyGroup
)
