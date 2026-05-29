package net.noti_me.dymit.dymit_backend_api.application.task.impl

import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskDeletedEvent
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * 과제 연쇄 삭제를 조율하는 지원 컴포넌트입니다.
 *
 * @property support 과제 공통 지원 컴포넌트
 * @property eventPublisher 애플리케이션 이벤트 발행기
 */
@Component
class TaskDeletionSupport(
    private val support: TaskServiceSupport,
    private val eventPublisher: ApplicationEventPublisher
) {

    /**
     * 과제와 연관 데이터를 순차 삭제하고 삭제 이벤트를 발행합니다.
     *
     * @param task 삭제 대상 과제
     * @param groupId 스터디 그룹 ID
     * @param deletedByScheduleEvent 일정 취소 이벤트에 의한 삭제 여부
     */
    fun cascadeDeleteTask(task: Task, groupId: ObjectId, deletedByScheduleEvent: Boolean) {
        val taskFileIds = task.attachments.map { it.fileId }
        val submissions = support.loadSubmissionsByTask(task.id!!)
        val submissionFileIds = submissions.flatMap { support.submissionAttachmentFileIds(it.attachments) }

        support.removeCommentsByTask(task.id!!)
        support.removeSubmissionsByTask(task.id!!)
        support.removeAssigneesByTask(task.id!!)
        support.removeTask(task.id!!)
        support.downgradeOrphanedFiles((taskFileIds + submissionFileIds).distinct())

        eventPublisher.publishEvent(
            TaskDeletedEvent(
                taskId = task.id!!,
                groupId = groupId,
                scheduleId = task.relatedScheduleId,
                deletedByScheduleEvent = deletedByScheduleEvent
            )
        )
    }
}
