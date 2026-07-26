package net.noti_me.dymit.dymit_backend_api.task.domain

import net.noti_me.dymit.dymit_backend_api.common.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

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
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false,
    id: ObjectId? = null
) : BaseAggregateRoot<TaskAssignee>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    var status: TaskAssigneeStatus = status
        private set

    /**
     * 제출 완료 상태로 변경합니다.
     */
    fun markSubmitted() {
        if ( status == TaskAssigneeStatus.SUBMITTED ) {
            return
        }
        status = TaskAssigneeStatus.SUBMITTED
        modified = true
    }

    /**
     * 미제출 상태로 변경합니다.
     */
    fun markNotSubmitted() {
        if ( status == TaskAssigneeStatus.NOT_SUBMITTED ) {
            return
        }
        status = TaskAssigneeStatus.NOT_SUBMITTED
        modified = true
    }
}
