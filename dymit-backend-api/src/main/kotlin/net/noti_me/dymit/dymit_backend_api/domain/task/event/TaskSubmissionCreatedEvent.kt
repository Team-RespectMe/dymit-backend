package net.noti_me.dymit.dymit_backend_api.domain.task.event

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import org.bson.types.ObjectId

/**
 * 과제 제출 생성 이벤트입니다.
 *
 * @property taskId 제출이 생성된 과제 ID
 * @property groupId 과제가 속한 스터디 그룹 ID
 * @property scheduleId 과제가 연결된 일정 ID
 * @property task 제출 대상 과제 aggregate
 * @property group 조회된 스터디 그룹 aggregate
 * @property member 제출한 스터디 그룹 멤버 aggregate
 */
class TaskSubmissionCreatedEvent(
    val taskId: ObjectId,
    val groupId: ObjectId,
    val scheduleId: ObjectId,
    val task: Task,
    val group: StudyGroup,
    val member: StudyGroupMember
)
