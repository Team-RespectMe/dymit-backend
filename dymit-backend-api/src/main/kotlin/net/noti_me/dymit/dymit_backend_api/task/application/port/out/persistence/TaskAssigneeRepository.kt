package net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence

import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssignee
import org.bson.types.ObjectId

/**
 * 과제 대상자 영속성 포트입니다.
 */
interface TaskAssigneeRepository {

    fun save(taskAssignee: TaskAssignee): TaskAssignee

    fun saveAll(taskAssignees: List<TaskAssignee>): List<TaskAssignee>

    fun findByTaskId(taskId: ObjectId): List<TaskAssignee>

    fun findByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): TaskAssignee?

    fun existsByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): Boolean

    fun deleteByTaskId(taskId: ObjectId): Long

    fun deleteByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): Boolean
}
