package net.noti_me.dymit.dymit_backend_api.units.study_group.adapter.out.statistics

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.statistics.MongoStatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.supports.memberSessionSnapshot
import net.noti_me.dymit.dymit_backend_api.supports.sessionLedger
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

internal class MongoSessionStatisticsRepositoryTest : BehaviorSpec({

    Given("원장 CAS는 완료됐지만 현재 회차 투영 저장은 부분 완료") {
        Then("하나라도 현재 projection을 충족하지 않으면 불완전한 합계를 반환하지 않는다") {
            val mongo = mockk<MongoTemplate>()
            val current = sessionLedger()
            val stale = sessionLedger(groupId = current.groupId, scheduleId = current.latestScheduleId)
            val wrong = sessionLedger(groupId = current.groupId, scheduleId = current.latestScheduleId)
            val missing = sessionLedger(groupId = current.groupId, scheduleId = current.latestScheduleId)
            val query = slot<Query>()
            every { mongo.find(capture(query), MemberSessionStatistics::class.java) } returns listOf(
                memberSessionSnapshot(current), memberSessionSnapshot(stale).copy(ledgerVersion = 1),
                memberSessionSnapshot(wrong).copy(projectionId = "loser"), memberSessionSnapshot(missing)
            )
            every { mongo.find(any<Query>(), MemberSessionStatisticsLedger::class.java) } returns listOf(current, stale, wrong)
            val repository = MongoStatisticsRepository(mongo)
            repository.sumMemberCounts(current.groupId, current.latestScheduleId, listOf(current, stale, wrong)) shouldBe null
            every { mongo.find(capture(query), MemberSessionStatistics::class.java) } returns listOf(memberSessionSnapshot(current))
            repository.sumMemberCounts(current.groupId, current.latestScheduleId, listOf(current)) shouldBe current.counts
            query.captured.queryObject["groupId"] shouldBe current.groupId
            query.captured.queryObject["scheduleId"] shouldBe current.latestScheduleId
        }
    }

    Given("같은 시각의 두 일정 중 뒤 일정부터 새 projection이 시작됨") {
        Then("앞 일정의 기존 버전은 유지하고 뒤 일정의 오래된 투영은 불완전한 합계로 판단한다") {
            val mongo = mockk<MongoTemplate>()
            val first = ObjectId("000000000000000000000001")
            val last = ObjectId("000000000000000000000002")
            val ledger = sessionLedger(scheduleId = last)
            every { mongo.find(any<Query>(), MemberSessionStatisticsLedger::class.java) } returns listOf(ledger)
            every { mongo.find(any<Query>(), MemberSessionStatistics::class.java) } returns
                listOf(memberSessionSnapshot(ledger).copy(scheduleId = first, ledgerVersion = 1, projectionId = "old"))
            val repository = MongoStatisticsRepository(mongo)
            repository.sumMemberCounts(ledger.groupId, first, listOf(ledger)) shouldBe ledger.counts
            every { mongo.find(any<Query>(), MemberSessionStatistics::class.java) } returns
                listOf(memberSessionSnapshot(ledger).copy(ledgerVersion = 1, projectionId = "old"))
            repository.sumMemberCounts(ledger.groupId, last, listOf(ledger)) shouldBe null
        }
    }

    Given("현재 원장은 있지만 정확한 회차 스냅샷이 아직 없음") {
        Then("원장 실적을 0으로 오인하지 않고 합계 미완료를 반환한다") {
            val mongo = mockk<MongoTemplate>()
            val ledger = sessionLedger()
            every { mongo.find(any<Query>(), MemberSessionStatistics::class.java) } returns emptyList()
            MongoStatisticsRepository(mongo).sumMemberCounts(ledger.groupId, ledger.latestScheduleId, listOf(ledger)) shouldBe null
        }
    }
})
