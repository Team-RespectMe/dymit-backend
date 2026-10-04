package net.noti_me.dymit.dymit_backend_api.task.domain

import net.noti_me.dymit.dymit_backend_api.common.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * 과제 제출 대상자 엔티티입니다.
 */
@Document(collection = "task_assignees")
class TaskAssignee(
    @Indexed(name = "task_assignee_task_id_idx")
    val taskId: ObjectId,
    @Indexed(name = "task_assignee_member_id_idx")
    val memberId: ObjectId,
    status: TaskAssigneeStatus = TaskAssigneeStatus.NOT_SUBMITTED,
    createdAt: Instant? = null,
    updatedAt: Instant? = null,
    isDeleted: Boolean = false,
    id: ObjectId? = null,
    deletedAt: Instant? = null,
    statusHistory: List<TaskAssigneeStatusChange> = emptyList()
) : BaseAggregateRoot<TaskAssignee>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
) {

    var status: TaskAssigneeStatus = status
        private set

    var statusHistory: MutableList<TaskAssigneeStatusChange> = statusHistory.toMutableList()
        private set

    /**
     * 제출 완료 상태로 변경합니다.
     */
    fun markSubmitted() {
        if ( status == TaskAssigneeStatus.SUBMITTED ) {
            return
        }
        appendLegacyBaselineIfNecessary()
        status = TaskAssigneeStatus.SUBMITTED
        statusHistory.add(
            TaskAssigneeStatusChange(
                status = TaskAssigneeStatus.SUBMITTED,
                changedAt = Instant.now()
            )
        )
        modified = true
    }

    /**
     * 미제출 상태로 변경합니다.
     */
    fun markNotSubmitted() {
        if ( status == TaskAssigneeStatus.NOT_SUBMITTED ) {
            return
        }
        appendLegacyBaselineIfNecessary()
        status = TaskAssigneeStatus.NOT_SUBMITTED
        statusHistory.add(
            TaskAssigneeStatusChange(
                status = TaskAssigneeStatus.NOT_SUBMITTED,
                changedAt = Instant.now()
            )
        )
        modified = true
    }

    /**
     * 지정 시각 직전의 제출 상태를 반환합니다.
     *
     * @param cutoff 배타적 조회 상한
     * @return 상한 직전의 제출 상태
     */
    fun statusAt(cutoff: Instant): TaskAssigneeStatus {
        return statusHistory.withIndex()
            .filter { it.value.changedAt < cutoff }
            .maxWithOrNull(compareBy({ it.value.changedAt }, { it.index }))
            ?.value
            ?.status
            ?: if (statusHistory.isEmpty() && updatedAt?.let { it < cutoff } == true) {
                status
            } else {
                TaskAssigneeStatus.NOT_SUBMITTED
            }
    }

    private fun appendLegacyBaselineIfNecessary() {
        if (statusHistory.isNotEmpty()) {
            return
        }
        statusHistory.add(
            TaskAssigneeStatusChange(
                status = status,
                changedAt = updatedAt ?: createdAt ?: Instant.EPOCH
            )
        )
    }
}
