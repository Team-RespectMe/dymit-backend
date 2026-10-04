package net.noti_me.dymit.dymit_backend_api.units.task.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatusChange
import org.bson.types.ObjectId
import java.time.Instant

internal class TaskAssigneeStatisticsHistoryTest : BehaviorSpec({
    val cutoff = Instant.parse("2026-09-27T15:00:00Z")

    Given("전주 제출 이후 이번 주에 철회하고 재제출한 대상자") {
        val assignee = TaskAssignee(
            taskId = ObjectId(),
            memberId = ObjectId(),
            status = TaskAssigneeStatus.SUBMITTED,
            statusHistory = listOf(
                TaskAssigneeStatusChange(TaskAssigneeStatus.SUBMITTED, cutoff.minusSeconds(1)),
                TaskAssigneeStatusChange(TaskAssigneeStatus.NOT_SUBMITTED, cutoff.plusSeconds(1)),
                TaskAssigneeStatusChange(TaskAssigneeStatus.SUBMITTED, cutoff.plusSeconds(2))
            )
        )
        Then("전주 종료 집계는 철회와 재제출의 영향을 받지 않는다") {
            assignee.statusAt(cutoff) shouldBe TaskAssigneeStatus.SUBMITTED
            assignee.statusAt(cutoff.plusSeconds(2)) shouldBe TaskAssigneeStatus.NOT_SUBMITTED
            assignee.statusAt(cutoff.plusSeconds(3)) shouldBe TaskAssigneeStatus.SUBMITTED
        }
    }

    Given("전주 종료 시각과 정확히 같은 시각에 제출한 대상자") {
        Then("제출은 다음 주 실적으로 남는다") {
            val assignee = TaskAssignee(
                taskId = ObjectId(),
                memberId = ObjectId(),
                status = TaskAssigneeStatus.SUBMITTED,
                statusHistory = listOf(TaskAssigneeStatusChange(TaskAssigneeStatus.SUBMITTED, cutoff))
            )
            assignee.statusAt(cutoff) shouldBe TaskAssigneeStatus.NOT_SUBMITTED
            assignee.statusAt(cutoff.plusNanos(1)) shouldBe TaskAssigneeStatus.SUBMITTED
        }
    }

    Given("이력이 저장 순서대로 정렬되지 않은 대상자") {
        Then("목록의 마지막 원소가 아니라 상한 이전의 최신 상태를 사용한다") {
            val assignee = TaskAssignee(
                taskId = ObjectId(),
                memberId = ObjectId(),
                statusHistory = listOf(
                    TaskAssigneeStatusChange(TaskAssigneeStatus.NOT_SUBMITTED, cutoff.plusSeconds(1)),
                    TaskAssigneeStatusChange(TaskAssigneeStatus.SUBMITTED, cutoff.minusSeconds(1)),
                    TaskAssigneeStatusChange(TaskAssigneeStatus.NOT_SUBMITTED, cutoff.minusSeconds(2))
                )
            )
            assignee.statusAt(cutoff) shouldBe TaskAssigneeStatus.SUBMITTED
        }
    }

    Given("동일 시각에 제출하고 이어서 철회한 대상자") {
        Then("마지막으로 기록한 철회 상태를 사용한다") {
            val changedAt = cutoff.minusSeconds(1)
            val assignee = TaskAssignee(
                taskId = ObjectId(),
                memberId = ObjectId(),
                status = TaskAssigneeStatus.NOT_SUBMITTED,
                statusHistory = listOf(
                    TaskAssigneeStatusChange(TaskAssigneeStatus.SUBMITTED, changedAt),
                    TaskAssigneeStatusChange(TaskAssigneeStatus.NOT_SUBMITTED, changedAt)
                )
            )
            assignee.statusAt(cutoff) shouldBe TaskAssigneeStatus.NOT_SUBMITTED
        }
    }

    Given("이력 도입 전에 제출되었고 마지막 수정일을 가진 대상자") {
        Then("확인 가능한 수정일 이전의 상태를 추측하지 않는다") {
            val assignee = TaskAssignee(
                taskId = ObjectId(),
                memberId = ObjectId(),
                status = TaskAssigneeStatus.SUBMITTED,
                updatedAt = cutoff
            )
            assignee.statusAt(cutoff) shouldBe TaskAssigneeStatus.NOT_SUBMITTED
            assignee.statusAt(cutoff.plusNanos(1)) shouldBe TaskAssigneeStatus.SUBMITTED
        }
    }
})
