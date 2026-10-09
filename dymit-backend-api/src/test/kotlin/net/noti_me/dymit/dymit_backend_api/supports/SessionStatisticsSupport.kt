package net.noti_me.dymit.dymit_backend_api.supports

import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import org.bson.types.ObjectId
import java.time.Instant

/** 회차 원장 테스트 데이터를 생성합니다. */
fun sessionLedger(
    groupId: ObjectId = ObjectId(),
    membershipId: ObjectId = ObjectId(),
    scheduleId: ObjectId = ObjectId(),
    at: Instant = Instant.parse("2026-10-05T10:00:00Z")
) = MemberSessionStatisticsLedger(
    groupId = groupId, membershipId = membershipId, memberId = ObjectId(), counts = StatisticsCounts(1, 2),
    latestScheduleId = scheduleId, latestSession = 7, latestScheduleAt = at,
    taskCalculatedThrough = at, scheduleCalculatedThrough = at,
    taskMutationWatermark = at, scheduleMutationWatermark = at,
    version = 2, projectionId = "current", projectionStartScheduleId = scheduleId,
    projectionStartAt = at, updatedAt = at
)

/** 지정 원장과 일치하는 회차 투영 데이터를 생성합니다. */
fun memberSessionSnapshot(ledger: MemberSessionStatisticsLedger) = MemberSessionStatistics(
    groupId = ledger.groupId, membershipId = ledger.membershipId, memberId = ledger.memberId,
    scheduleId = ledger.latestScheduleId, session = ledger.latestSession, scheduleAt = ledger.latestScheduleAt,
    statisticsAt = ledger.latestScheduleAt, counts = ledger.counts, ledgerVersion = ledger.version,
    projectionId = ledger.projectionId, updatedAt = ledger.updatedAt
)
