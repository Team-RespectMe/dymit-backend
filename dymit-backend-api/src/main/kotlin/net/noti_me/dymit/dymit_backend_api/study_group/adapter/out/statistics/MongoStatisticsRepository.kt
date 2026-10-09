package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.statistics

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import org.bson.types.ObjectId
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/** MongoDB 단일 문서 CAS로 회차 통계 원장과 투영을 저장합니다. */
@Repository
class MongoStatisticsRepository(
    private val mongoTemplate: MongoTemplate
) : StatisticsRepository {

    override fun findLedger(membershipId: ObjectId): MemberSessionStatisticsLedger? {
        return mongoTemplate.findOne(
            Query(Criteria.where("membershipId").`is`(membershipId)),
            MemberSessionStatisticsLedger::class.java
        )
    }

    override fun compareAndSetLedger(
        expectedVersion: Long?,
        ledger: MemberSessionStatisticsLedger
    ): Boolean {
        if (expectedVersion == null) {
            return try {
                mongoTemplate.insert(ledger)
                true
            } catch (_: DuplicateKeyException) {
                false
            }
        }
        return mongoTemplate.findAndReplace(
            Query(
                Criteria.where("membershipId").`is`(ledger.membershipId)
                    .and("version").`is`(expectedVersion)
            ),
            ledger,
            FindAndReplaceOptions.options(),
            MemberSessionStatisticsLedger::class.java,
            "member_session_statistics_ledgers"
        ) != null
    }

    override fun saveMemberSessions(snapshots: List<MemberSessionStatistics>) {
        snapshots.forEach(::saveMemberSession)
    }

    override fun findMemberSessions(membershipId: ObjectId): List<MemberSessionStatistics> {
        return mongoTemplate.find(
            Query(Criteria.where("membershipId").`is`(membershipId)).with(
                Sort.by(
                    Sort.Order.asc("scheduleAt"),
                    Sort.Order.asc("scheduleId")
                )
            ),
            MemberSessionStatistics::class.java
        )
    }

    override fun findLedgersByGroupId(groupId: ObjectId): List<MemberSessionStatisticsLedger> {
        return mongoTemplate.find(
            Query(Criteria.where("groupId").`is`(groupId)),
            MemberSessionStatisticsLedger::class.java
        )
    }

    override fun sumMemberCounts(
        groupId: ObjectId,
        scheduleId: ObjectId,
        ledgers: List<MemberSessionStatisticsLedger>
    ): StatisticsCounts? {
        val snapshots = mongoTemplate.find(
            Query(
                Criteria.where("groupId").`is`(groupId)
                    .and("scheduleId").`is`(scheduleId)
            ),
            MemberSessionStatistics::class.java
        ).associateBy { it.membershipId }
        return ledgers.fold(StatisticsCounts()) { total, ledger ->
            val snapshot = snapshots[ledger.membershipId]
                ?.takeIf { isValid(it, ledger) }
                ?: return null
            total + snapshot.counts
        }
    }

    override fun findGroupSession(
        groupId: ObjectId,
        scheduleId: ObjectId
    ): GroupSessionStatistics? {
        return mongoTemplate.findOne(
            Query(
                Criteria.where("groupId").`is`(groupId)
                    .and("scheduleId").`is`(scheduleId)
            ),
            GroupSessionStatistics::class.java
        )
    }

    override fun findGroupSessions(groupId: ObjectId): List<GroupSessionStatistics> {
        return mongoTemplate.find(
            Query(Criteria.where("groupId").`is`(groupId)).with(
                Sort.by(
                    Sort.Order.asc("scheduleAt"),
                    Sort.Order.asc("scheduleId")
                )
            ),
            GroupSessionStatistics::class.java
        )
    }

    override fun compareAndSetGroupSession(
        expectedVersion: Long?,
        snapshot: GroupSessionStatistics
    ): Boolean {
        if (expectedVersion == null) {
            return try {
                mongoTemplate.insert(snapshot)
                true
            } catch (_: DuplicateKeyException) {
                false
            }
        }
        return mongoTemplate.findAndReplace(
            Query(
                Criteria.where("groupId").`is`(snapshot.groupId)
                    .and("scheduleId").`is`(snapshot.scheduleId)
                    .and("version").`is`(expectedVersion)
            ),
            snapshot,
            FindAndReplaceOptions.options(),
            GroupSessionStatistics::class.java,
            "group_session_statistics"
        ) != null
    }

    private fun saveMemberSession(snapshot: MemberSessionStatistics) {
        val key = Criteria.where("membershipId").`is`(snapshot.membershipId)
            .and("scheduleId").`is`(snapshot.scheduleId)
        val current = mongoTemplate.findOne(Query(key), MemberSessionStatistics::class.java)
        if (current == null) {
            try {
                mongoTemplate.insert(snapshot)
                return
            } catch (_: DuplicateKeyException) {
                saveMemberSession(snapshot)
                return
            }
        }
        if (current.ledgerVersion > snapshot.ledgerVersion) {
            return
        }
        mongoTemplate.findAndReplace(
            Query(
                Criteria.where("_id").`is`(current.id)
                    .and("ledgerVersion").lte(snapshot.ledgerVersion)
            ),
            snapshot.copy(id = current.id),
            FindAndReplaceOptions.options(),
            MemberSessionStatistics::class.java,
            "member_session_statistics"
        )
    }

    private fun isValid(
        snapshot: MemberSessionStatistics,
        ledger: MemberSessionStatisticsLedger
    ): Boolean {
        val beforeProjection = compareBoundary(
            snapshot.scheduleAt,
            snapshot.scheduleId,
            ledger.projectionStartAt,
            ledger.projectionStartScheduleId
        ) < 0
        return beforeProjection && snapshot.ledgerVersion < ledger.version ||
            snapshot.ledgerVersion == ledger.version && snapshot.projectionId == ledger.projectionId
    }

    private fun compareBoundary(
        leftAt: java.time.Instant,
        leftId: ObjectId,
        rightAt: java.time.Instant,
        rightId: ObjectId
    ): Int {
        val timeComparison = leftAt.compareTo(rightAt)
        return if (timeComparison != 0) timeComparison else leftId.compareTo(rightId)
    }
}
