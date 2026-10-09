package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics

import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import org.bson.types.ObjectId

/** 회차 통계 원장과 구성원·그룹 투영을 저장하는 포트입니다. */
interface StatisticsRepository {

    /** 가입 관계의 회차 통계 원장을 조회합니다. */
    fun findLedger(membershipId: ObjectId): MemberSessionStatisticsLedger?

    /** 예상 버전이 일치할 때만 회차 통계 원장을 저장합니다. */
    fun compareAndSetLedger(
        expectedVersion: Long?,
        ledger: MemberSessionStatisticsLedger
    ): Boolean

    /** 원장 버전을 보호하며 구성원 회차 투영을 저장합니다. */
    fun saveMemberSessions(snapshots: List<MemberSessionStatistics>)

    /** 가입 관계에 저장된 모든 회차 투영을 순서대로 조회합니다. */
    fun findMemberSessions(membershipId: ObjectId): List<MemberSessionStatistics>

    /** 그룹의 모든 가입 관계 회차 원장을 조회합니다. */
    fun findLedgersByGroupId(groupId: ObjectId): List<MemberSessionStatisticsLedger>

    /** 주어진 원장 투영과 모두 일치할 때 그룹 회차의 가입 관계별 기여를 합산합니다. */
    fun sumMemberCounts(
        groupId: ObjectId,
        scheduleId: ObjectId,
        ledgers: List<MemberSessionStatisticsLedger>
    ): StatisticsCounts?

    /** 그룹의 회차 투영을 조회합니다. */
    fun findGroupSession(groupId: ObjectId, scheduleId: ObjectId): GroupSessionStatistics?

    /** 그룹에 저장된 모든 회차 투영을 순서대로 조회합니다. */
    fun findGroupSessions(groupId: ObjectId): List<GroupSessionStatistics>

    /** 예상 버전이 일치할 때만 그룹 회차 투영을 저장합니다. */
    fun compareAndSetGroupSession(
        expectedVersion: Long?,
        snapshot: GroupSessionStatistics
    ): Boolean
}
