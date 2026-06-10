package net.noti_me.dymit.dymit_backend_api.domain.task.event

import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import org.bson.types.ObjectId

/**
 * 과제 제출 댓글 생성 이벤트입니다.
 *
 * @property taskId 댓글 대상 과제 ID
 * @property groupId 과제가 속한 스터디 그룹 ID
 * @property submissionId 댓글 대상 제출 ID
 * @property assigneeMemberId 댓글 대상 제출의 작성자 멤버 ID
 * @property task 댓글 대상 과제 aggregate
 * @property group 조회된 스터디 그룹 aggregate
 * @property member 댓글을 작성한 스터디 그룹 멤버 aggregate
 */
class TaskSubmissionCommentCreatedEvent(
    val taskId: ObjectId,
    val groupId: ObjectId,
    val submissionId: ObjectId,
    val assigneeMemberId: ObjectId,
    val task: Task,
    val group: StudyGroup,
    val member: StudyGroupMember
)
