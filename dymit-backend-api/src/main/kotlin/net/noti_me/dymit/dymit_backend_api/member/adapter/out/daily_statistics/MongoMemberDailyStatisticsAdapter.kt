package net.noti_me.dymit.dymit_backend_api.member.adapter.`out`.daily_statistics

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.daily_statistics.MemberDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.daily_statistics.MemberDailyStatisticsPort
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.Instant

/**
 * Collects member metrics and atomically writes only the member daily-statistics section.
 */
@Repository
class MongoMemberDailyStatisticsAdapter(
    private val mongoTemplate: MongoTemplate
) : MemberDailyStatisticsPort {

    /**
     * Counts joins by creation time and withdrawals/visitors by last access time.
     */
    override fun collect(
        windowStart: Instant,
        windowEnd: Instant
    ): MemberDailyStatisticsDto {
        val createdAtCriteria = Criteria.where("createdAt").gte(windowStart).lt(windowEnd)
        val lastAccessCriteria = Criteria.where("lastAccessAt").gte(windowStart).lt(windowEnd)
        return MemberDailyStatisticsDto(
            joinedCount = mongoTemplate.count(Query(createdAtCriteria), Member::class.java),
            withdrawnCount = mongoTemplate.count(
                Query(lastAccessCriteria.and("isDeleted").`is`(true)),
                Member::class.java
            ),
            visitorCount = mongoTemplate.count(
                Query(
                    Criteria.where("lastAccessAt").gte(windowStart).lt(windowEnd)
                        .and("isDeleted").`is`(false)
                ),
                Member::class.java
            )
        )
    }

    /**
     * Upserts member fields and retries a concurrent first-insert collision with a normal update.
     */
    override fun upsert(
        statisticDate: LocalDate,
        windowStart: Instant,
        windowEnd: Instant,
        statistics: MemberDailyStatisticsDto
    ): Boolean {
        val now = Instant.now()
        val query = Query(Criteria.where("statisticDate").`is`(statisticDate))
        val update = Update()
            .set("member.joinedCount", statistics.joinedCount)
            .set("member.withdrawnCount", statistics.withdrawnCount)
            .set("member.visitorCount", statistics.visitorCount)
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
                    .set("member.joinedCount", statistics.joinedCount)
                    .set("member.withdrawnCount", statistics.withdrawnCount)
                    .set("member.visitorCount", statistics.visitorCount)
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
