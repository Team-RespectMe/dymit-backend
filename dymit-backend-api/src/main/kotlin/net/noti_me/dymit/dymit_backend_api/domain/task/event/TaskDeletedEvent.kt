package net.noti_me.dymit.dymit_backend_api.domain.task.event

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import org.bson.types.ObjectId

/**
 * 과제 삭제 이벤트입니다.
 *
 * @property taskId 삭제된 과제 ID
 * @property groupId 과제가 속했던 스터디 그룹 ID
 * @property scheduleId 과제가 연결됐던 일정 ID
 * @property task 삭제된 과제 aggregate
 * @property group 조회된 스터디 그룹 aggregate, 미조회 경로에서는 null
 * @property assigneeMemberIds 과제 담당 멤버 ID 목록
 * @property deletedByScheduleEvent 일정 취소 이벤트에 의한 삭제 여부
 */
class TaskDeletedEvent(
    val taskId: ObjectId,
    val groupId: ObjectId,
    val scheduleId: ObjectId,
    val task: Task,
    val group: StudyGroup? = null,
    val assigneeMemberIds: List<ObjectId>,
    val deletedByScheduleEvent: Boolean
)
