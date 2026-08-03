package net.noti_me.dymit.dymit_backend_api.task.adapter.`out`.daily_statistics

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.daily_statistics.TaskDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.daily_statistics.TaskDailyStatisticsPort
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmission
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Collects task metrics and atomically writes only the task daily-statistics section.
 */
@Repository
class MongoTaskDailyStatisticsAdapter(
    private val mongoTemplate: MongoTemplate
) : TaskDailyStatisticsPort {

    /**
     * Counts every task and submission creation record in the window.
     */
    override fun collect(
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime
    ): TaskDailyStatisticsDto {
        return TaskDailyStatisticsDto(
            createdCount = mongoTemplate.count(
                Query(Criteria.where("createdAt").gte(windowStart).lt(windowEnd)),
                Task::class.java
            ),
            submittedCount = mongoTemplate.count(
                Query(Criteria.where("createdAt").gte(windowStart).lt(windowEnd)),
                TaskSubmission::class.java
            )
        )
    }

    /**
     * Upserts task fields and retries a concurrent first-insert collision with a normal update.
     */
    override fun upsert(
        statisticDate: LocalDate,
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime,
        statistics: TaskDailyStatisticsDto
    ): Boolean {
        val now = LocalDateTime.now(DailyStatisticsWindowCalculator.KOREA_ZONE)
        val query = Query(Criteria.where("statisticDate").`is`(statisticDate))
        val update = Update()
            .set("task.createdCount", statistics.createdCount)
            .set("task.submittedCount", statistics.submittedCount)
            .set("updatedAt", now)
            .setOnInsert("statisticDate", statisticDate)
            .setOnInsert("windowStart", windowStart)
            .setOnInsert("windowEnd", windowEnd)
            .setOnInsert("createdAt", now)
        return try {
            mongoTemplate.upsert(query, update, DAILY_STATISTICS_COLLECTION).upsertedId != null
        } catch (_: DuplicateKeyException) {
            mongoTemplate.updateFirst(
                query,
                Update()
                    .set("task.createdCount", statistics.createdCount)
                    .set("task.submittedCount", statistics.submittedCount)
                    .set("updatedAt", now),
                DAILY_STATISTICS_COLLECTION
            )
            false
        }
    }

    private companion object {
        const val DAILY_STATISTICS_COLLECTION = "daily_stats"
    }
}
