package net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.`out`.daily_statistics

import jakarta.annotation.PostConstruct
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`out`.daily_statistics.StudyScheduleDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`out`.daily_statistics.StudyScheduleDailyStatisticsPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.types.ObjectId
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Collects schedule metrics and atomically writes only the study-schedule section.
 */
@Repository
class MongoStudyScheduleDailyStatisticsAdapter(
    private val mongoTemplate: MongoTemplate
) : StudyScheduleDailyStatisticsPort {

    /**
     * Ensures participant-window distinct member aggregation has a supporting compound index.
     */
    @PostConstruct
    fun ensureParticipantStatisticsIndex() {
        mongoTemplate.indexOps(SCHEDULE_PARTICIPANT_COLLECTION).createIndex(
            Index()
                .on("createdAt", Sort.Direction.ASC)
                .on("memberId", Sort.Direction.ASC)
                .named("schedule_participant_created_at_member_id_idx")
        )
    }

    /**
     * Counts schedule creations and distinct members with participant records created in the window.
     */
    override fun collect(
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime
    ): StudyScheduleDailyStatisticsDto {
        val windowQuery = Query(Criteria.where("createdAt").gte(windowStart).lt(windowEnd))
        val participantMemberIds = mongoTemplate.findDistinct(
            windowQuery,
            "memberId",
            ScheduleParticipant::class.java,
            ObjectId::class.java
        )
        return StudyScheduleDailyStatisticsDto(
            createdCount = mongoTemplate.count(
                Query(Criteria.where("createdAt").gte(windowStart).lt(windowEnd)),
                StudySchedule::class.java
            ),
            participantMemberCount = participantMemberIds.size.toLong()
        )
    }

    /**
     * Upserts schedule fields and retries a concurrent first-insert collision with a normal update.
     */
    override fun upsert(
        statisticDate: LocalDate,
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime,
        statistics: StudyScheduleDailyStatisticsDto
    ): Boolean {
        val now = LocalDateTime.now(DailyStatisticsWindowCalculator.KOREA_ZONE)
        val query = Query(Criteria.where("statisticDate").`is`(statisticDate))
        val update = Update()
            .set("studySchedule.createdCount", statistics.createdCount)
            .set("studySchedule.participantMemberCount", statistics.participantMemberCount)
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
                    .set("studySchedule.createdCount", statistics.createdCount)
                    .set("studySchedule.participantMemberCount", statistics.participantMemberCount)
                    .set("updatedAt", now),
                DAILY_STATISTICS_COLLECTION
            )
            false
        }
    }

    private companion object {
        const val DAILY_STATISTICS_COLLECTION = "daily_stats"
        const val SCHEDULE_PARTICIPANT_COLLECTION = "study_schedule_participants"
    }
}
