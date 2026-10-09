package net.noti_me.dymit.dymit_backend_api.task.adapter.out.statistics

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleStatisticsQueryPort
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.TaskStatisticsQueryPort
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsAssignmentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsChanges
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsExpireAtHistoryDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsStatusHistoryDto
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.time.Instant

/** MongoDB에서 과제 통계 원천을 증분 조회합니다. */
@Component
class MongoTaskStatisticsQueryAdapter(
    private val mongoTemplate: MongoTemplate,
    private val schedulePort: StudyScheduleStatisticsQueryPort
) : TaskStatisticsQueryPort {

    private val scheduleReferenceCache = object : LinkedHashMap<ObjectId, ScheduleReferenceCacheEntry>(
        MAX_GROUP_CACHE_SIZE,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<ObjectId, ScheduleReferenceCacheEntry>?
        ): Boolean = size > MAX_GROUP_CACHE_SIZE
    }

    override fun loadChanged(query: TaskStatisticsQuery): TaskStatisticsChanges {
        if (query.previousCutoff == null || query.mutationAfter == null) {
            return TaskStatisticsChanges(emptyList(), requiresReplay = true)
        }
        val scheduleReferences = loadScheduleReferences(query.groupId, query.mutationThrough)
        val scheduleIds = scheduleReferences.map { it.scheduleId }
        if (scheduleIds.isEmpty()) {
            return TaskStatisticsChanges(emptyList(), requiresReplay = false)
        }
        val overlap = query.mutationAfter.minusSeconds(CURSOR_OVERLAP_SECONDS)
        val changeLowerBound = if (query.cutoff > query.previousCutoff) {
            minOf(overlap, query.previousCutoff)
        } else {
            overlap
        }
        val matured = mongoTemplate.find(
            Query(
                Criteria.where("relatedScheduleId").`in`(scheduleIds)
                    .and("expireAt").gt(query.previousCutoff).lte(query.cutoff)
            ),
            Task::class.java
        )
        val changedTasks = mongoTemplate.find(
            Query(
                Criteria.where("relatedScheduleId").`in`(scheduleIds)
                    .and("updatedAt").gte(changeLowerBound).lte(query.mutationThrough)
            ),
            Task::class.java
        )
        val changedAssignments = mongoTemplate.find(
            Query(
                Criteria.where("memberId").`is`(query.memberId)
                    .and("updatedAt").gte(changeLowerBound).lte(query.mutationThrough)
            ),
            TaskAssignee::class.java
        )
        val assignmentTaskIds = changedAssignments.map { it.taskId }.distinct()
        val assignmentTasks = loadTasks(assignmentTaskIds, scheduleIds)
        val changedScheduleIds = scheduleReferences
            .filter {
                it.updatedAt?.let { updatedAt ->
                    updatedAt >= changeLowerBound && updatedAt <= query.mutationThrough
                } == true
            }
            .map { it.scheduleId }
        val scheduleChangedTasks = mongoTemplate.find(
            Query(Criteria.where("relatedScheduleId").`in`(changedScheduleIds)),
            Task::class.java
        )
        val maturedIds = matured.mapNotNull { it.id }.toSet()
        val correctionIds = (changedTasks + assignmentTasks + scheduleChangedTasks)
            .mapNotNull { it.id }
            .filterNot(maturedIds::contains)
        val tasks = (matured + changedTasks + assignmentTasks + scheduleChangedTasks).distinctBy { it.id }
        return TaskStatisticsChanges(
            sources = mapSources(tasks, query.memberId, scheduleReferences.associateBy { it.scheduleId }),
            requiresReplay = correctionIds.isNotEmpty()
        )
    }

    override fun loadAll(query: TaskStatisticsQuery): List<TaskStatisticsDto> {
        val scheduleReferences = loadScheduleReferences(query.groupId, query.mutationThrough)
        val scheduleIds = scheduleReferences.map { it.scheduleId }
        if (scheduleIds.isEmpty()) {
            return emptyList()
        }
        val assignments = mongoTemplate.find(
            Query(
                Criteria.where("memberId").`is`(query.memberId)
                    .and("createdAt").gte(query.joinedAt)
            ),
            TaskAssignee::class.java
        )
        return mapSources(
            loadTasks(assignments.map { it.taskId }.distinct(), scheduleIds),
            query.memberId,
            scheduleReferences.associateBy { it.scheduleId }
        )
    }

    private fun loadTasks(taskIds: List<ObjectId>, scheduleIds: List<ObjectId>): List<Task> {
        if (taskIds.isEmpty()) {
            return emptyList()
        }
        return mongoTemplate.find(
            Query(
                Criteria.where("_id").`in`(taskIds)
                    .and("relatedScheduleId").`in`(scheduleIds)
            ),
            Task::class.java
        )
    }

    private fun loadScheduleReferences(
        groupId: ObjectId,
        observedAt: Instant
    ): List<net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsReferenceDto> {
        val cached = synchronized(scheduleReferenceCache) { scheduleReferenceCache[groupId] }
        if (cached?.observedAt == observedAt) {
            return cached.references
        }
        val loaded = schedulePort.loadScheduleReferences(groupId)
        synchronized(scheduleReferenceCache) {
            val current = scheduleReferenceCache[groupId]
            if (current == null || current.observedAt <= observedAt) {
                scheduleReferenceCache[groupId] = ScheduleReferenceCacheEntry(observedAt, loaded)
            }
        }
        return loaded
    }

    private fun mapSources(
        tasks: List<Task>,
        memberId: ObjectId,
        scheduleReferences: Map<ObjectId, net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsReferenceDto>
    ): List<TaskStatisticsDto> {
        val taskIds = tasks.mapNotNull { it.id }
        val assignments = if (taskIds.isEmpty()) {
            emptyList()
        } else {
            mongoTemplate.find(
                Query(
                    Criteria.where("taskId").`in`(taskIds)
                        .and("memberId").`is`(memberId)
                ),
                TaskAssignee::class.java
            )
        }.groupBy { it.taskId }
        return tasks.map { task ->
            TaskStatisticsDto(
                taskId = requireNotNull(task.id),
                expireAt = task.expireAt,
                expireAtHistory = task.expireAtHistory.map {
                    TaskStatisticsExpireAtHistoryDto(
                        previousExpireAt = it.previousExpireAt,
                        expireAt = it.expireAt,
                        changedAt = it.changedAt
                    )
                },
                deletedAt = task.deletedAt,
                relatedScheduleDeletedAt = scheduleReferences[task.relatedScheduleId]?.deletedAt,
                assignments = assignments[task.id].orEmpty().mapNotNull { assignee ->
                    val assignedAt = assignee.createdAt ?: return@mapNotNull null
                    TaskStatisticsAssignmentDto(
                        assignedAt = assignedAt,
                        deletedAt = assignee.deletedAt,
                        statusHistory = assignee.statusHistory.map {
                            TaskStatisticsStatusHistoryDto(
                                submitted = it.status == TaskAssigneeStatus.SUBMITTED,
                                changedAt = it.changedAt
                            )
                        },
                        legacySubmitted = assignee.status == TaskAssigneeStatus.SUBMITTED,
                        legacyStatusUpdatedAt = assignee.updatedAt
                    )
                }
            )
        }
    }

    private companion object {
        const val CURSOR_OVERLAP_SECONDS = 1L
        const val MAX_GROUP_CACHE_SIZE = 128
    }

    private data class ScheduleReferenceCacheEntry(
        val observedAt: Instant,
        val references: List<net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsReferenceDto>
    )
}
