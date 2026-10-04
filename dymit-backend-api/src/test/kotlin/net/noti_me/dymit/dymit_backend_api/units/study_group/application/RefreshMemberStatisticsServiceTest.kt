package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.*
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.supports.StatisticsTestFixture
import java.time.Instant

internal class RefreshMemberStatisticsServiceTest : BehaviorSpec({
    Given("첫 조회에서 경계와 가입 이전 자료가 섞인 사용자") {
        Then("전주 종료 이전의 유효한 대상과 실적만 누적한다") {
            val f = StatisticsTestFixture()
            val old = f.cutoff.minusSeconds(604800)
            f.allTasks = listOf(
                f.task(old, old.minusSeconds(1)),
                f.task(old, f.cutoff),
                f.task(f.cutoff, old),
                f.task(f.cutoff.plusSeconds(1), old),
                f.task(old, old, assignedAt = f.joinedAt.minusSeconds(1))
            )
            f.allSchedules = listOf(
                f.schedule(old), f.schedule(f.cutoff), f.schedule(f.cutoff.plusSeconds(1)),
                f.schedule(old, created = f.joinedAt.minusSeconds(1))
            )
            f.service.execute(f.command()).counts shouldBe StatisticsCounts(1, 2, 1, 1)
            f.weeks.map { it.weekEnd }.sorted() shouldBe listOf(
                Instant.parse("2026-09-13T15:00:00Z"), old, f.cutoff
            )
            f.weeks.sortedBy { it.weekEnd }.map { it.counts.assignedTaskCount } shouldBe listOf(0L, 0L, 2L)
        }
    }

    Given("이미 계산한 동일 주를 변경 없이 다시 조회") {
        Then("같은 값을 반환하고 저장이나 전체 재계산을 반복하지 않는다") {
            val f = StatisticsTestFixture()
            f.allTasks = listOf(f.task(f.cutoff.minusSeconds(1), f.cutoff.minusSeconds(2)))
            val first = f.service.execute(f.command())
            val version = f.ledger!!.version
            f.service.execute(f.command(observed = f.observedAt.plusSeconds(1))) shouldBe first
            f.ledger!!.version shouldBe version
            verify(exactly = 1) { f.tasks.loadAll(any()) }
            verify(exactly = 1) { f.repository.compareAndSetLedger(any(), any()) }
        }
    }

    Given("수정 없이 시간이 흘러 새 과제와 일정의 기한이 도래") {
        Then("원천 전체를 읽지 않고 새로 닫힌 범위의 건수만 누적한다") {
            val f = StatisticsTestFixture()
            f.allTasks = listOf(f.task(f.cutoff.minusSeconds(1), f.cutoff.minusSeconds(2)))
            f.service.execute(f.command())
            val next = f.cutoff.plusSeconds(604800)
            f.taskChanges = StatisticsSourceChanges(listOf(f.task(next.minusSeconds(1), next.minusSeconds(2))), false)
            f.scheduleChanges = StatisticsSourceChanges(listOf(f.schedule(next.minusSeconds(1))), false)
            f.service.execute(f.command(next, f.observedAt.plusSeconds(604800))).counts shouldBe
                StatisticsCounts(2, 2, 1, 1)
            verify(exactly = 1) { f.tasks.loadAll(any()) }
            verify { f.tasks.loadChanged(match { it.previousCutoff == f.cutoff && it.cutoff == next }) }
        }
    }

    Given("기존 항목의 일반 수정이 새 주 증분 조회에 함께 포함됨") {
        Then("이전 상한 기여분을 빼므로 기존 건수를 다시 더하지 않는다") {
            val f = StatisticsTestFixture()
            val task = f.task(f.cutoff.minusSeconds(1), f.cutoff.minusSeconds(2))
            f.allTasks = listOf(task)
            f.service.execute(f.command())
            f.taskChanges = StatisticsSourceChanges(listOf(task), false)
            f.service.execute(f.command(f.cutoff.plusSeconds(604800), f.observedAt.plusSeconds(604800))).counts shouldBe
                StatisticsCounts(1, 1)
            verify(exactly = 1) { f.tasks.loadAll(any()) }
        }
    }

    Given("이번 주 철회가 같은 주 조회에서 확인된 뒤 다음 주로 넘어감") {
        Then("관측 커서가 전진했어도 새 상한에서 철회를 반영한다") {
            val f = StatisticsTestFixture()
            val task = f.task(f.cutoff.minusSeconds(1), f.cutoff.minusSeconds(2))
            f.allTasks = listOf(task)
            f.service.execute(f.command())
            val withdrawn = task.copy(assignments = task.assignments.map { assignment ->
                assignment.copy(statusHistory = assignment.statusHistory + TaskStatusHistoryData(false, f.cutoff.plusSeconds(1)))
            })
            f.taskChanges = StatisticsSourceChanges(listOf(withdrawn), false)
            f.service.execute(f.command(observed = f.observedAt.plusSeconds(1))).counts shouldBe StatisticsCounts(1, 1)
            f.service.execute(f.command(f.cutoff.plusSeconds(604800), f.observedAt.plusSeconds(604800))).counts shouldBe
                StatisticsCounts(0, 1)
            verify(exactly = 1) { f.tasks.loadAll(any()) }
        }
    }

    Given("지난주 제출 뒤 이번 주 철회와 삭제가 발생") {
        Then("재계산하더라도 지난주 누적 스냅샷을 감소시키지 않는다") {
            val f = StatisticsTestFixture()
            val submittedAt = f.cutoff.minusSeconds(2)
            f.allTasks = listOf(f.task(f.cutoff.minusSeconds(1), submittedAt))
            f.service.execute(f.command())
            f.allTasks = listOf(f.task(
                f.cutoff.minusSeconds(1), deletedAt = f.cutoff.plusSeconds(1),
                history = listOf(TaskStatusHistoryData(true, submittedAt), TaskStatusHistoryData(false, f.cutoff))
            ))
            f.taskChanges = StatisticsSourceChanges(emptyList(), true)
            f.service.execute(f.command(observed = f.observedAt.plusSeconds(1))).counts shouldBe StatisticsCounts(1, 1)
        }
    }

    Given("생성되고 제출된 과제가 마감 전에 삭제됨") {
        Then("분자와 분모를 모두 제외하며 이전 스냅샷을 중복 가산하지 않는다") {
            val f = StatisticsTestFixture()
            val expires = f.cutoff.minusSeconds(1)
            f.allTasks = listOf(f.task(expires, expires.minusSeconds(10), expires.minusSeconds(5)))
            f.service.execute(f.command()).counts shouldBe StatisticsCounts()
            f.taskChanges = StatisticsSourceChanges(emptyList(), true)
            f.service.execute(f.command(observed = f.observedAt.plusSeconds(1))).counts shouldBe StatisticsCounts()
        }
    }

    Given("관련 일정은 취소됐지만 비동기 동기화 실패로 과제는 활성 상태") {
        Then("일정 취소 시각을 확인하여 과제 분자와 분모를 제외한다") {
            val f = StatisticsTestFixture()
            val expires = f.cutoff.minusSeconds(1)
            f.allTasks = listOf(f.task(expires, expires.minusSeconds(10)).copy(
                relatedScheduleDeletedAt = expires.minusSeconds(5)
            ))
            f.service.execute(f.command()).counts shouldBe StatisticsCounts()
        }
    }

    Given("이번 주 마감일 변경으로 과거 마감일이 미래로 이동") {
        Then("이전 주 스냅샷에는 당시의 마감일로 계산한다") {
            val f = StatisticsTestFixture()
            val original = f.cutoff.minusSeconds(1)
            f.allTasks = listOf(f.task(f.cutoff.plusSeconds(604800), original.minusSeconds(1)).copy(
                expireAtHistory = listOf(TaskExpireAtHistoryData(original, f.cutoff.plusSeconds(604800), f.cutoff))
            ))
            f.service.execute(f.command()).counts shouldBe StatisticsCounts(1, 1)
        }
    }

    Given("사용자 원장은 저장됐지만 주간 스냅샷 저장이 실패") {
        Then("다음 조회에서 같은 건수로 복구하며 중복 누적하지 않는다") {
            val f = StatisticsTestFixture()
            f.allTasks = listOf(f.task(f.cutoff.minusSeconds(1), f.cutoff.minusSeconds(2)))
            every { f.repository.saveMemberWeeks(any()) } throws IllegalStateException("저장 실패")
            shouldThrow<IllegalStateException> { f.service.execute(f.command()) }
            f.ledger!!.counts shouldBe StatisticsCounts(1, 1)
            f.weeks shouldBe emptyList()
            every { f.repository.saveMemberWeeks(any()) } answers {
                f.weeks.addAll(firstArg<List<net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberWeeklyStatistics>>())
            }
            f.service.execute(f.command()).counts shouldBe StatisticsCounts(1, 1)
            f.weeks.count { it.weekEnd == f.cutoff } shouldBe 1
        }
    }

    Given("주간 스냅샷 일부가 누락된 사용자 원장") {
        Then("업무 상한이 같아도 누락을 복구한다") {
            val f = StatisticsTestFixture()
            f.allTasks = listOf(f.task(f.cutoff.minusSeconds(1), f.cutoff.minusSeconds(2)))
            f.service.execute(f.command())
            f.weeks.removeAll { it.weekEnd == f.cutoff }
            f.service.execute(f.command(observed = f.observedAt.plusSeconds(1))).counts shouldBe StatisticsCounts(1, 1)
            f.weeks.single { it.weekEnd == f.cutoff }.counts shouldBe StatisticsCounts(1, 1)
        }
    }

    Given("일정 종료 후 참가 기록을 정리한 사용자") {
        Then("일정 시작 당시 유효했던 참석 실적을 유지한다") {
            val f = StatisticsTestFixture()
            val starts = f.cutoff.minusSeconds(10)
            f.allSchedules = listOf(f.schedule(starts).copy(
                participations = listOf(
                    ScheduleParticipationData(starts.minusSeconds(1), starts.plusSeconds(1))
                )
            ))
            f.service.execute(f.command()).counts shouldBe StatisticsCounts(0, 0, 1, 1)
        }
    }

    Given("수요일 탈퇴 후 목요일 과제 철회와 삭제가 발생한 가입 구간") {
        Then("탈퇴 직전 실적을 유지하고 이후의 원천 변경은 반영하지 않는다") {
            val f = StatisticsTestFixture()
            val leftAt = f.cutoff.minusSeconds(86400 * 3)
            every { f.members.findByIdIncludingDeleted(f.membershipId) } returns StudyGroupMember(
                id = f.membershipId, groupId = f.groupId, memberId = f.memberId,
                createdAt = f.joinedAt, isDeleted = true, deletedAt = leftAt
            )
            f.allTasks = listOf(f.task(
                leftAt.minusSeconds(1), deletedAt = leftAt.plusSeconds(1),
                history = listOf(
                    TaskStatusHistoryData(true, leftAt.minusSeconds(2)),
                    TaskStatusHistoryData(false, leftAt)
                )
            ))
            f.service.execute(f.command()).counts shouldBe StatisticsCounts(1, 1)
            f.taskChanges = StatisticsSourceChanges(f.allTasks, false)
            f.service.execute(f.command(f.cutoff.plusSeconds(604800), f.observedAt.plusSeconds(604800))).counts shouldBe
                StatisticsCounts(1, 1)
        }
    }

    Given("동시 갱신으로 첫 원장 저장의 예상 버전이 충돌") {
        Then("최신 원장을 다시 읽고 중복 없이 완료한다") {
            val f = StatisticsTestFixture()
            f.allTasks = listOf(f.task(f.cutoff.minusSeconds(1), f.cutoff.minusSeconds(2)))
            var conflicted = false
            every { f.repository.compareAndSetLedger(any(), any()) } answers {
                if (!conflicted) {
                    conflicted = true
                    false
                } else {
                    f.ledger = secondArg()
                    true
                }
            }
            f.service.execute(f.command()).counts shouldBe StatisticsCounts(1, 1)
            f.weeks.count { it.weekEnd == f.cutoff } shouldBe 1
            verify(exactly = 2) { f.repository.findLedger(f.membershipId) }
            verify(exactly = 1) { f.repository.saveMemberWeeks(any()) }
        }
    }
})
