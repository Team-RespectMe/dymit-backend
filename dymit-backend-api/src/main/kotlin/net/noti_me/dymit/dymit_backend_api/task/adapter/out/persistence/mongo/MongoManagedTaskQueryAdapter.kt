package net.noti_me.dymit.dymit_backend_api.task.adapter.out.persistence.mongo

import net.noti_me.dymit.dymit_backend_api.task.application.ManagedTaskCursor
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.ManagedTaskQueryPort
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.ManagedTaskQueryResult
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository
import java.time.Instant

/** MongoDB 집계로 소유 그룹의 오늘 마감 과제를 조회합니다. */
@Repository
class MongoManagedTaskQueryAdapter(
    private val mongoTemplate: MongoTemplate
) : ManagedTaskQueryPort {

    override fun load(
        ownerId: ObjectId,
        startInclusive: Instant,
        endExclusive: Instant,
        cursor: ManagedTaskCursor?,
        limit: Int
    ): List<ManagedTaskQueryResult> {
        val dateCriteria = Criteria.where("expireAt").gte(startInclusive).lt(endExclusive)
            .and("isDeleted").ne(true)
        val taskCriteria = cursor?.let {
            Criteria().andOperator(dateCriteria, cursorCriteria(it))
        } ?: dateCriteria
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(taskCriteria),
            Aggregation.sort(
                Sort.by(
                    Sort.Order.asc("expireAt"),
                    Sort.Order.asc("_id")
                )
            ),
            Aggregation.lookup("study_schedules", "relatedScheduleId", "_id", "schedule"),
            Aggregation.unwind("schedule"),
            Aggregation.match(Criteria.where("schedule.isDeleted").ne(true)),
            Aggregation.lookup("study_groups", "schedule.groupId", "_id", "group"),
            Aggregation.unwind("group"),
            Aggregation.match(
                Criteria.where("group.ownerId").`is`(ownerId)
                    .and("group.isDeleted").ne(true)
            ),
            Aggregation.limit(limit.toLong()),
            Aggregation.project("_id", "title", "expireAt")
                .and("schedule.groupId").`as`("groupId")
                .and("group.name").`as`("groupName")
        )
        return mongoTemplate.aggregate(aggregation, TASK_COLLECTION, Document::class.java)
            .mappedResults
            .map { document ->
                ManagedTaskQueryResult(
                    taskId = requireNotNull(document.getObjectId("_id")),
                    title = requireNotNull(document.getString("title")),
                    expireAt = requireNotNull(document.getDate("expireAt")).toInstant(),
                    groupId = requireNotNull(document.getObjectId("groupId")),
                    groupName = requireNotNull(document.getString("groupName"))
                )
            }
    }

    override fun countNotSubmittedAssignees(taskIds: List<ObjectId>): Map<ObjectId, Long> {
        if (taskIds.isEmpty()) {
            return emptyMap()
        }
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("taskId").`in`(taskIds)
                    .and("status").`is`(TaskAssigneeStatus.NOT_SUBMITTED)
                    .and("isDeleted").ne(true)
            ),
            Aggregation.group("taskId").count().`as`("count")
        )
        return mongoTemplate.aggregate(aggregation, ASSIGNEE_COLLECTION, Document::class.java)
            .mappedResults
            .associate { document ->
                requireNotNull(document.getObjectId("_id")) to
                    requireNotNull(document.get("count", Number::class.java)).toLong()
            }
    }

    private fun cursorCriteria(cursor: ManagedTaskCursor): Criteria {
        return Criteria().orOperator(
            Criteria.where("expireAt").gt(cursor.expireAt),
            Criteria().andOperator(
                Criteria.where("expireAt").`is`(cursor.expireAt),
                Criteria.where("_id").gt(cursor.taskId)
            )
        )
    }

    private companion object {
        const val TASK_COLLECTION = "tasks"
        const val ASSIGNEE_COLLECTION = "task_assignees"
    }
}
