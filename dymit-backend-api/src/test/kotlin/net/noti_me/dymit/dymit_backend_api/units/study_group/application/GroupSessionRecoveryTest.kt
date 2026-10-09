package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.supports.GroupStatisticsQueryFixture

internal class GroupSessionRecoveryTest : BehaviorSpec({

    Given("모든 회차 투영이 일치한 그룹의 변경 없는 재조회") {
        Then("과거 회차를 매번 합산하거나 저장하지 않으면서 현재 활성 인원은 반영한다") {
            val f = GroupStatisticsQueryFixture()
            val first = f.execute()
            f.activeCount = 5
            f.groupName = "새 그룹 이름"
            val second = f.execute()
            second.counts shouldBe first.counts
            second.activeMemberCount shouldBe 5
            first.group.name shouldBe "현재 그룹 이름"
            second.group.id shouldBe f.group.toHexString()
            second.group.name shouldBe "새 그룹 이름"
            verify(exactly = 2) { f.groups.loadByGroupId(f.group.toHexString()) }
            verify(exactly = 0) { f.groups.loadByGroupIds(any()) }
            verify(exactly = f.boundaries.size) { f.repository.sumMemberCounts(f.group, any(), any()) }
            verify(exactly = f.boundaries.size) { f.repository.compareAndSetGroupSession(any(), any()) }
        }
    }

    Given("구성원 projection 버전이 바뀐 뒤 그룹 투영 저장 중 실패") {
        Then("다음 요청이 저장된 fingerprint로 오래된 그룹 회차를 탐지하고 모든 회차를 복구한다") {
            val f = GroupStatisticsQueryFixture()
            f.execute()
            f.counts = StatisticsCounts(2, 2, 2, 4)
            f.ledgers = f.ledgers.map { it.copy(version = it.version + 1, projectionId = "corrected") }
            var failed = false
            every { f.repository.compareAndSetGroupSession(any(), any()) } answers {
                val snapshot = secondArg<GroupSessionStatistics>()
                if (snapshot.scheduleId == f.boundaries.last().scheduleId && !failed) {
                    failed = true
                    throw IllegalStateException("그룹 회차 저장 실패")
                }
                f.stored[snapshot.scheduleId] = snapshot
                true
            }
            shouldThrow<IllegalStateException> { f.execute() }
            val repaired = f.execute()
            repaired.counts shouldBe f.counts
            repaired.taskSubmissionRate shouldBe 100.0
            repaired.previousTaskSubmissionRate shouldBe 100.0
            f.stored.values.map { it.sourceProjectionToken }.distinct().size shouldBe 1
            f.stored.values.all { it.counts == f.counts } shouldBe true
        }
    }

    Given("그룹의 첫 실제 회차") {
        Then("직전 회차는 null이며 이전 제출률과 참석률은 0으로 반환한다") {
            val f = GroupStatisticsQueryFixture()
            f.boundaries = f.boundaries.take(1)
            val result = f.execute()
            result.latestSession shouldBe 3
            result.previousSession shouldBe null
            result.previousTaskSubmissionRate shouldBe 0.0
            result.previousScheduleAttendanceRate shouldBe 0.0
        }
    }

    Given("다른 요청이 구성원 원장 CAS를 완료했지만 회차 투영 저장은 지연됨") {
        Then("불완전한 합계로 그룹 토큰을 확정하지 않고 이후 완전한 투영으로 복구한다") {
            val f = GroupStatisticsQueryFixture()
            f.execute()
            val originalTokens = f.stored.mapValues { it.value.sourceProjectionToken }
            f.ledgers = f.ledgers.map { it.copy(version = it.version + 1, projectionId = "pending") }
            every { f.repository.sumMemberCounts(f.group, any(), any()) } returns null
            shouldThrow<net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException> { f.execute() }
            f.stored.mapValues { it.value.sourceProjectionToken } shouldBe originalTokens
            f.counts = StatisticsCounts(2, 2, 2, 4)
            every { f.repository.sumMemberCounts(f.group, any(), any()) } answers { f.counts }
            val repaired = f.execute()
            repaired.taskSubmissionRate shouldBe 100.0
            repaired.previousTaskSubmissionRate shouldBe 100.0
            f.stored.values.all { it.counts == f.counts } shouldBe true
        }
    }

    Given("그룹 저장 또는 warm 조회 도중 구성원 projection 토큰이 바뀜") {
        Then("서로 다른 원장 세대의 응답을 확정하지 않고 최신 세대로 재시도한다") {
            val f = GroupStatisticsQueryFixture()
            f.execute()
            var loads = 0
            every { f.repository.findLedgersByGroupId(f.group) } answers {
                loads += 1
                if (loads == 2) {
                    f.ledgers = f.ledgers.map { it.copy(version = it.version + 1, projectionId = "racing") }
                    f.counts = StatisticsCounts(2, 2, 2, 4)
                }
                f.ledgers
            }
            val result = f.execute()
            result.taskSubmissionRate shouldBe 100.0
            result.previousTaskSubmissionRate shouldBe 100.0
            f.stored.values.all { it.counts == f.counts } shouldBe true
            (loads >= 3) shouldBe true
        }
    }

    listOf("누락", "삭제").forEach { state ->
        Given("현재 그룹 정보가 $state 상태인 그룹 통계 상세") {
            Then("통계 갱신 전에 존재하지 않는 그룹으로 처리한다") {
                val f = GroupStatisticsQueryFixture()
                every { f.groups.loadByGroupId(f.group.toHexString()) } returns
                    if (state == "누락") null else StudyGroup(id = f.group, name = "삭제 그룹", isDeleted = true)
                shouldThrow<NotFoundException> { f.execute() }
                verify(exactly = 1) { f.groups.loadByGroupId(f.group.toHexString()) }
                verify(exactly = 0) { f.schedules.loadBoundaries(any(), any()) }
                verify(exactly = 0) { f.refresh.execute(any()) }
            }
        }
    }
})
