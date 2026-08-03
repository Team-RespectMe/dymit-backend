package net.noti_me.dymit.dymit_backend_api.study_group.adapter.`out`.daily_statistics

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`out`.daily_statistics.StudyGroupDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`out`.daily_statistics.StudyGroupDailyStatisticsPort
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Collects study-group metrics and atomically writes only the study-group section.
 */
@Repository
class MongoStudyGroupDailyStatisticsAdapter(
    private val mongoTemplate: MongoTemplate
) : StudyGroupDailyStatisticsPort {

    /**
     * Counts every study-group creation record in the window, including later soft deletions.
     */
    override fun collect(
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime
    ): StudyGroupDailyStatisticsDto {
        return StudyGroupDailyStatisticsDto(
            createdCount = mongoTemplate.count(
                Query(Criteria.where("createdAt").gte(windowStart).lt(windowEnd)),
                StudyGroup::class.java
            )
        )
    }

    /**
     * Upserts study-group fields and retries a concurrent first-insert collision with a normal update.
     */
    override fun upsert(
        statisticDate: LocalDate,
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime,
        statistics: StudyGroupDailyStatisticsDto
    ): Boolean {
        val now = LocalDateTime.now(DailyStatisticsWindowCalculator.KOREA_ZONE)
        val query = Query(Criteria.where("statisticDate").`is`(statisticDate))
        val update = Update()
            .set("studyGroup.createdCount", statistics.createdCount)
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
                    .set("studyGroup.createdCount", statistics.createdCount)
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
