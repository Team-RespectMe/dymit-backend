package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.statistics

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupWeeklyStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberWeeklyStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import org.bson.types.ObjectId
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * MongoDB 단일 문서 CAS로 통계 원장과 투영을 저장합니다.
 */
@Repository
class MongoStatisticsRepository(
    private val mongoTemplate: MongoTemplate
) : StatisticsRepository {

    override fun findLedger(membershipId: ObjectId): MemberStatisticsLedger? {
        return mongoTemplate.findOne(
            Query(Criteria.where("membershipId").`is`(membershipId)),
            MemberStatisticsLedger::class.java
        )
    }

    override fun compareAndSetLedger(
        expectedVersion: Long?,
        ledger: MemberStatisticsLedger
    ): Boolean {
        if (expectedVersion == null) {
            return try {
                mongoTemplate.insert(ledger)
                true
            } catch (_: DuplicateKeyException) {
                false
            }
        }
        val query = Query(
            Criteria.where("membershipId").`is`(ledger.membershipId)
                .and("version").`is`(expectedVersion)
        )
        return mongoTemplate.findAndReplace(
            query,
            ledger,
            FindAndReplaceOptions.options(),
            MemberStatisticsLedger::class.java,
            "member_statistics_ledgers"
        ) != null
    }

    override fun saveMemberWeeks(snapshots: List<MemberWeeklyStatistics>) {
        snapshots.forEach(::saveMemberWeek)
    }

    override fun findMemberWeeks(
        membershipId: ObjectId,
        through: Instant
    ): List<MemberWeeklyStatistics> {
        return mongoTemplate.find(
            Query(
                Criteria.where("membershipId").`is`(membershipId)
                    .and("weekEnd").lte(through)
            ).with(Sort.by(Sort.Direction.ASC, "weekEnd")),
            MemberWeeklyStatistics::class.java
        )
    }

    override fun sumLatestMemberCounts(groupId: ObjectId, cutoff: Instant): StatisticsCounts {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("groupId").`is`(groupId)
                    .and("weekEnd").lte(cutoff)
            ),
            Aggregation.sort(Sort.Direction.DESC, "weekEnd"),
            Aggregation.group("membershipId").first(Aggregation.ROOT).`as`("snapshot"),
            Aggregation.replaceRoot("snapshot")
        )
        val snapshots = mongoTemplate.aggregate(
            aggregation,
            "member_weekly_statistics",
            MemberWeeklyStatistics::class.java
        ).mappedResults
        if (snapshots.isEmpty()) {
            return StatisticsCounts()
        }
        val ledgers = mongoTemplate.find(
            Query(Criteria.where("membershipId").`in`(snapshots.map { it.membershipId })),
            MemberStatisticsLedger::class.java
        ).associateBy { it.membershipId }
        return snapshots
            .filter { snapshot ->
                ledgers[snapshot.membershipId]?.let { ledger ->
                    snapshot.weekEnd < ledger.projectionStartWeekEnd &&
                        snapshot.ledgerVersion < ledger.version ||
                        snapshot.ledgerVersion == ledger.version &&
                        snapshot.projectionId == ledger.projectionId
                } == true
            }
            .fold(StatisticsCounts()) { total, snapshot -> total + snapshot.counts }
    }

    override fun findGroupWeek(groupId: ObjectId, weekEnd: Instant): GroupWeeklyStatistics? {
        return mongoTemplate.findOne(
            Query(Criteria.where("groupId").`is`(groupId).and("weekEnd").`is`(weekEnd)),
            GroupWeeklyStatistics::class.java
        )
    }

    override fun findLatestGroupWeek(groupId: ObjectId, through: Instant): GroupWeeklyStatistics? {
        return mongoTemplate.findOne(
            Query(
                Criteria.where("groupId").`is`(groupId)
                    .and("weekEnd").lte(through)
            ).with(Sort.by(Sort.Direction.DESC, "weekEnd")),
            GroupWeeklyStatistics::class.java
        )
    }

    override fun compareAndSetGroupWeek(
        expectedVersion: Long?,
        snapshot: GroupWeeklyStatistics
    ): Boolean {
        if (expectedVersion == null) {
            return try {
                mongoTemplate.insert(snapshot)
                true
            } catch (_: DuplicateKeyException) {
                false
            }
        }
        val query = Query(
            Criteria.where("groupId").`is`(snapshot.groupId)
                .and("weekEnd").`is`(snapshot.weekEnd)
                .and("version").`is`(expectedVersion)
        )
        return mongoTemplate.findAndReplace(
            query,
            snapshot,
            FindAndReplaceOptions.options(),
            GroupWeeklyStatistics::class.java,
            "group_weekly_statistics"
        ) != null
    }

    private fun saveMemberWeek(snapshot: MemberWeeklyStatistics) {
        val key = Criteria.where("membershipId").`is`(snapshot.membershipId)
            .and("weekEnd").`is`(snapshot.weekEnd)
        val current = mongoTemplate.findOne(Query(key), MemberWeeklyStatistics::class.java)
        if (current == null) {
            try {
                mongoTemplate.insert(snapshot)
                return
            } catch (_: DuplicateKeyException) {
                saveMemberWeek(snapshot)
                return
            }
        }
        if (current.ledgerVersion > snapshot.ledgerVersion) {
            return
        }
        val replacement = snapshot.copy(id = current.id)
        mongoTemplate.findAndReplace(
            Query(
                Criteria.where("_id").`is`(current.id)
                    .and("ledgerVersion").lte(snapshot.ledgerVersion)
            ),
            replacement,
            FindAndReplaceOptions.options(),
            MemberWeeklyStatistics::class.java,
            "member_weekly_statistics"
        )
    }
}
