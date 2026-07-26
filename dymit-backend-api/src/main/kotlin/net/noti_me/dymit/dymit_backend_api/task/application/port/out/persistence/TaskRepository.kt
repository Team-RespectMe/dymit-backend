package net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence

import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import org.bson.types.ObjectId

/**
 * 과제 영속성 포트입니다.
 */
interface TaskRepository {

    fun save(task: Task): Task

    fun findById(id: ObjectId): Task?

    fun findByRelatedScheduleId(scheduleId: ObjectId): List<Task>

    fun findByRelatedScheduleIdAndType(
        scheduleId: ObjectId,
        type: TaskType
    ): List<Task>

    fun findByRelatedScheduleIds(scheduleIds: List<ObjectId>): List<Task>

    fun deleteById(id: ObjectId): Boolean

    fun findAttachedFileIds(fileIds: List<ObjectId>): Set<ObjectId>

    fun findAttachedFileIdsExcludingTask(
        fileIds: List<ObjectId>,
        taskId: ObjectId
    ): Set<ObjectId>
}
