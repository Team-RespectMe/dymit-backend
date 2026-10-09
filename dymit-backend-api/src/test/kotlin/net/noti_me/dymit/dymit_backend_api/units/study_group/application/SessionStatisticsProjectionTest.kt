package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSourceChanges
import net.noti_me.dymit.dymit_backend_api.supports.StatisticsTestFixture
import org.bson.types.ObjectId

internal class SessionStatisticsProjectionTest : BehaviorSpec({

    Given("불규칙 간격의 실제 일정 여러 회차를 건너뛴 첫 조회") {
        Then("표시 번호 차이와 무관하게 모든 회차 시작 시각의 누적값을 별도로 저장한다") {
            val f = StatisticsTestFixture()
            f.boundaries = listOf(f.boundary(f.cutoff.minusSeconds(90), 3),
                f.boundary(f.cutoff.minusSeconds(17), 41), f.boundary(f.cutoff, 6))
            f.allTasks = f.boundaries.map { f.task(it.scheduleAt, it.scheduleAt) }
            f.allSchedules = f.boundaries.map { f.schedule(it.scheduleAt, attendedAt = it.scheduleAt, id = it.scheduleId) }
            val result = f.service.execute(f.command())
            result.counts shouldBe StatisticsCounts(3, 3, 3, 3)
            result.latestSession shouldBe 6
            result.statisticsAt shouldBe f.cutoff
            f.sessions.map { it.scheduleId }.toSet() shouldBe f.boundaries.map { it.scheduleId }.toSet()
            f.sessions.sortedBy { it.scheduleAt }.map { it.counts } shouldBe
                listOf(StatisticsCounts(1, 1, 1, 1), StatisticsCounts(2, 2, 2, 2), StatisticsCounts(3, 3, 3, 3))
        }
    }

    Given("동일한 시작 시각과 중복 표시 회차를 가진 두 일정") {
        Then("일정 ID별 스냅샷을 만들고 순서상 뒤 일정 참석은 앞 회차에 포함하지 않는다") {
            val f = StatisticsTestFixture()
            val first = f.boundary(f.cutoff, 7, ObjectId("000000000000000000000001"))
            val last = f.boundary(f.cutoff, 7, ObjectId("000000000000000000000002"))
            f.boundaries = listOf(first, last)
            f.allSchedules = f.boundaries.map { f.schedule(it.scheduleAt, attendedAt = it.scheduleAt, id = it.scheduleId) }
            f.service.execute(f.command()).counts shouldBe StatisticsCounts(0, 0, 2, 2)
            f.sessions.single { it.scheduleId == first.scheduleId }.counts shouldBe StatisticsCounts(0, 0, 1, 1)
            f.sessions.single { it.scheduleId == last.scheduleId }.counts shouldBe StatisticsCounts(0, 0, 2, 2)
            f.sessions.map { it.scheduleId }.distinct().size shouldBe 2
        }
    }

    Given("현재 회차 시작 이후 마감되는 후속 과제") {
        Then("현재 회차에서는 제외하고 이후 실제 회차에 마감 도래 시 포함한다") {
            val f = StatisticsTestFixture()
            f.boundaries = listOf(f.boundary(f.cutoff, 1))
            val later = f.cutoff.plusSeconds(900)
            val postTask = f.task(f.cutoff.plusSeconds(300), f.cutoff.plusSeconds(100))
            f.allTasks = listOf(postTask)
            f.service.execute(f.command()).counts shouldBe StatisticsCounts()
            f.taskChanges = StatisticsSourceChanges(listOf(postTask), false)
            f.service.execute(f.command(later, f.observedAt.plusSeconds(900))).counts shouldBe StatisticsCounts(1, 1)
            f.sessions.single { it.scheduleAt == f.cutoff }.counts shouldBe StatisticsCounts()
        }
    }

    listOf("최근", "중간").forEach { position ->
        Given("계산 완료 후 $position 진행 회차가 삭제됨") {
            Then("현재 유효 경계로 재투영하고 이전 상한으로 돌아가도 거부하지 않는다") {
                val f = StatisticsTestFixture()
                f.allTasks = listOf(f.task(f.cutoff, f.cutoff))
                f.service.execute(f.command())
                val deleted = if (position == "최근") f.boundaries.last() else f.boundaries[1]
                f.boundaries = f.boundaries.filterNot { it.scheduleId == deleted.scheduleId }
                val latest = f.boundaries.last()
                val result = f.service.execute(f.command(latest.scheduleAt, f.observedAt.plusSeconds(1)))
                result.latestSession shouldBe latest.session
                result.statisticsAt shouldBe latest.scheduleAt
                f.ledger!!.latestScheduleId shouldBe latest.scheduleId
                result.counts shouldBe if (position == "최근") StatisticsCounts() else StatisticsCounts(1, 1)
                f.sessions.filter { it.ledgerVersion == f.ledger!!.version }.map { it.scheduleId }.toSet() shouldBe
                    f.boundaries.map { it.scheduleId }.toSet()
            }
        }
    }

    Given("최근 회차는 같지만 경계 사이에 과거 일정이 추가됨") {
        Then("누락된 일정 ID를 탐지하여 그 회차와 이후 투영을 복구한다") {
            val f = StatisticsTestFixture()
            f.service.execute(f.command())
            val inserted = f.boundary(f.cutoff.minusSeconds(300), 99)
            f.boundaries = (f.boundaries + inserted).sortedBy { it.scheduleAt }
            f.service.execute(f.command(observed = f.observedAt.plusSeconds(1)))
            f.sessions.single { it.scheduleId == inserted.scheduleId }.session shouldBe 99
            f.sessions.single { it.scheduleId == inserted.scheduleId }.ledgerVersion shouldBe f.ledger!!.version
            f.ledger!!.latestSession shouldBe 12
        }
    }

    Given("가입 시각이 그룹의 최근 진행 회차보다 늦은 구성원") {
        Then("개인 통계는 최근 회차를 기준으로 0을 반환한다") {
            val f = StatisticsTestFixture()
            every { f.members.findByIdIncludingDeleted(f.membershipId) } returns StudyGroupMember(
                id = f.membershipId, groupId = f.groupId, memberId = f.memberId, createdAt = f.cutoff.plusSeconds(1)
            )
            f.allTasks = listOf(f.task(f.cutoff, f.cutoff, assignedAt = f.cutoff.minusSeconds(1)))
            val result = f.service.execute(f.command())
            result.counts shouldBe StatisticsCounts()
            result.latestSession shouldBe 12
            result.statisticsAt shouldBe f.cutoff
        }
    }

    Given("진행 회차가 없는 그룹의 구성원") {
        Then("회차와 기준 시각을 null로 반환하고 원장이나 스냅샷을 생성하지 않는다") {
            val f = StatisticsTestFixture()
            val result = f.service.execute(f.command().copy(boundaries = emptyList()))
            result.latestSession shouldBe null
            result.statisticsAt shouldBe null
            result.counts shouldBe StatisticsCounts()
            f.sessions shouldBe emptyList()
            f.ledger shouldBe null
            verify(exactly = 0) { f.repository.compareAndSetLedger(any(), any()) }
        }
    }

    Given("모든 CAS 재시도가 경쟁에 실패") {
        Then("불완전한 투영을 저장하지 않고 충돌 오류를 반환한다") {
            val f = StatisticsTestFixture()
            every { f.repository.compareAndSetLedger(any(), any()) } returns false
            shouldThrow<ConflictException> { f.service.execute(f.command()) }
            f.sessions shouldBe emptyList()
            verify(exactly = 0) { f.repository.saveMemberSessions(any()) }
        }
    }

    Given("같은 사용자가 탈퇴 후 새 가입 관계로 재가입") {
        Then("탈퇴 가입 구간의 실적은 동결하고 재가입 구간은 이전 할당을 다시 계산하지 않는다") {
            val former = StatisticsTestFixture()
            val leftAt = former.cutoff.minusSeconds(10)
            every { former.members.findByIdIncludingDeleted(former.membershipId) } returns StudyGroupMember(
                id = former.membershipId, groupId = former.groupId, memberId = former.memberId,
                createdAt = former.joinedAt, isDeleted = true, deletedAt = leftAt
            )
            val oldTask = former.task(leftAt.minusSeconds(1), leftAt.minusSeconds(2))
            former.allTasks = listOf(oldTask)
            former.service.execute(former.command()).counts shouldBe StatisticsCounts(1, 1)
            former.ledger!!.taskCalculatedThrough shouldBe leftAt
            val rejoined = StatisticsTestFixture(groupId = former.groupId, memberId = former.memberId)
            every { rejoined.members.findByIdIncludingDeleted(rejoined.membershipId) } returns StudyGroupMember(
                id = rejoined.membershipId, groupId = former.groupId, memberId = former.memberId,
                createdAt = leftAt.plusSeconds(1)
            )
            rejoined.allTasks = listOf(oldTask, rejoined.task(rejoined.cutoff, rejoined.cutoff, assignedAt = leftAt.plusSeconds(1)))
            rejoined.service.execute(rejoined.command()).counts shouldBe StatisticsCounts(1, 1)
            former.sessions.all { it.membershipId == former.membershipId } shouldBe true
            rejoined.sessions.all { it.membershipId == rejoined.membershipId } shouldBe true
            (former.membershipId == rejoined.membershipId) shouldBe false
        }
    }

    Given("계산된 일정 ID는 같지만 시작 시각이 변경됨") {
        Then("과거 스냅샷의 시각을 그대로 반환하지 않고 변경된 경계로 재투영한다") {
            val f = StatisticsTestFixture()
            f.allTasks = listOf(f.task(f.cutoff.plusSeconds(30), f.cutoff.plusSeconds(20)))
            f.service.execute(f.command()).counts shouldBe StatisticsCounts()
            val latest = f.boundaries.last().copy(scheduleAt = f.cutoff.plusSeconds(60))
            f.boundaries = f.boundaries.dropLast(1) + latest
            val result = f.service.execute(f.command(latest.scheduleAt, f.observedAt.plusSeconds(1)))
            result.statisticsAt shouldBe latest.scheduleAt
            result.counts shouldBe StatisticsCounts(1, 1)
            f.sessions.single { it.scheduleId == latest.scheduleId }.scheduleAt shouldBe latest.scheduleAt
        }
    }

    Given("가입 후 생성됐지만 시작 시각은 가입 이전인 과거 일정") {
        Then("일정 생성 시각만으로 가입 이전 참석을 개인 통계에 포함하지 않는다") {
            val f = StatisticsTestFixture()
            f.allSchedules = listOf(f.schedule(f.joinedAt.minusSeconds(1), created = f.joinedAt.plusSeconds(1)))
            f.service.execute(f.command()).counts shouldBe StatisticsCounts()
        }
    }
})
