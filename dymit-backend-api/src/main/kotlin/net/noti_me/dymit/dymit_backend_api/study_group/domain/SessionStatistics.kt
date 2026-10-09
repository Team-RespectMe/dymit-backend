package net.noti_me.dymit.dymit_backend_api.study_group.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/** 가입 구간별 회차 통계와 처리 위치를 보관하는 원장입니다. */
@Document("member_session_statistics_ledgers")
@CompoundIndex(name = "member_session_statistics_ledger_membership_uq", def = "{'membershipId': 1}", unique = true)
data class MemberSessionStatisticsLedger(
    @Id val id: ObjectId? = null,
    val groupId: ObjectId,
    val membershipId: ObjectId,
    val memberId: ObjectId,
    val counts: StatisticsCounts,
    val latestScheduleId: ObjectId,
    val latestSession: Long,
    val latestScheduleAt: Instant,
    val taskCalculatedThrough: Instant,
    val scheduleCalculatedThrough: Instant,
    val taskMutationWatermark: Instant,
    val scheduleMutationWatermark: Instant,
    val version: Long,
    val projectionId: String,
    val projectionStartScheduleId: ObjectId,
    val projectionStartAt: Instant,
    val updatedAt: Instant
)

/** 가입 구간의 일정 회차 시작 시점 누적 통계입니다. */
@Document("member_session_statistics")
@CompoundIndex(
    name = "member_session_statistics_membership_schedule_uq",
    def = "{'membershipId': 1, 'scheduleId': 1}",
    unique = true
)
data class MemberSessionStatistics(
    @Id val id: ObjectId? = null,
    val groupId: ObjectId,
    val membershipId: ObjectId,
    val memberId: ObjectId,
    val scheduleId: ObjectId,
    val session: Long,
    val scheduleAt: Instant,
    val statisticsAt: Instant,
    val counts: StatisticsCounts,
    val ledgerVersion: Long,
    val projectionId: String,
    val updatedAt: Instant
)

/** 그룹의 일정 회차 시작 시점 누적 통계입니다. */
@Document("group_session_statistics")
@CompoundIndex(
    name = "group_session_statistics_group_schedule_uq",
    def = "{'groupId': 1, 'scheduleId': 1}",
    unique = true
)
data class GroupSessionStatistics(
    @Id val id: ObjectId? = null,
    val groupId: ObjectId,
    val scheduleId: ObjectId,
    val session: Long,
    val scheduleAt: Instant,
    val statisticsAt: Instant,
    val counts: StatisticsCounts,
    val sourceProjectionToken: String,
    val version: Long,
    val updatedAt: Instant
)
