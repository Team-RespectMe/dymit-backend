package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics

import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupWeeklyStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberWeeklyStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import org.bson.types.ObjectId
import java.time.Instant

/**
 * 통계 원장과 주간 투영을 저장하고 조회하는 포트입니다.
 */
interface StatisticsRepository {

    /** 가입 관계의 최신 원장을 조회합니다. */
    fun findLedger(membershipId: ObjectId): MemberStatisticsLedger?

    /** 예상 버전이 일치할 때만 원장을 저장합니다. */
    fun compareAndSetLedger(expectedVersion: Long?, ledger: MemberStatisticsLedger): Boolean

    /** 더 최신 원장 버전을 덮어쓰지 않도록 주간 스냅샷을 저장합니다. */
    fun saveMemberWeeks(snapshots: List<MemberWeeklyStatistics>)

    /** 가입 관계의 계산 상한까지 저장된 주간 스냅샷을 조회합니다. */
    fun findMemberWeeks(membershipId: ObjectId, through: Instant): List<MemberWeeklyStatistics>

    /** 지정 상한 이하의 가입 관계별 최신 스냅샷 건수를 합산합니다. */
    fun sumLatestMemberCounts(groupId: ObjectId, cutoff: Instant): StatisticsCounts

    /** 그룹의 주간 스냅샷을 조회합니다. */
    fun findGroupWeek(groupId: ObjectId, weekEnd: Instant): GroupWeeklyStatistics?

    /** 그룹에서 지정 상한 이하의 가장 최근 스냅샷을 조회합니다. */
    fun findLatestGroupWeek(groupId: ObjectId, through: Instant): GroupWeeklyStatistics?

    /** 예상 버전이 일치할 때만 그룹 주간 스냅샷을 저장합니다. */
    fun compareAndSetGroupWeek(expectedVersion: Long?, snapshot: GroupWeeklyStatistics): Boolean
}
