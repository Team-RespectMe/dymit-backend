package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldBeEmpty
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.SyncParticipatedScheduleTasksUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class SyncParticipatedScheduleTasksUseCaseImplTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val useCase = SyncParticipatedScheduleTasksUseCaseImpl(support)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("일정 참여자 사전 과제 동기화") {
            When("PRE 과제가 여러 개 있고 일부 과제에만 신규 assignee가 추가되면") {
                Then("실제로 추가된 과제만 반환한다") {
                    val scheduleId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val addedTask1 = createTask(scheduleId, "추가된 과제 1")
                    val skippedTask = createTask(scheduleId, "기존 대상 과제")
                    val addedTask2 = createTask(scheduleId, "추가된 과제 2")

                    every {
                        support.loadTasksBySchedule(scheduleId, TaskType.PRE)
                    } returns listOf(addedTask1, skippedTask, addedTask2)
                    every { support.addAssigneeIfAbsent(addedTask1.id!!, memberId) } returns true
                    every { support.addAssigneeIfAbsent(skippedTask.id!!, memberId) } returns false
                    every { support.addAssigneeIfAbsent(addedTask2.id!!, memberId) } returns true

                    val result = useCase.syncParticipatedScheduleTasks(
                        scheduleId = scheduleId.toHexString(),
                        memberId = memberId.toHexString()
                    )

                    result shouldContainExactly listOf(addedTask1, addedTask2)
                }
            }

            When("PRE 과제가 없으면") {
                Then("빈 목록을 반환하고 assignee 추가를 시도하지 않는다") {
                    val scheduleId = ObjectId.get()
                    val memberId = ObjectId.get()

                    every { support.loadTasksBySchedule(scheduleId, TaskType.PRE) } returns emptyList()

                    val result = useCase.syncParticipatedScheduleTasks(
                        scheduleId = scheduleId.toHexString(),
                        memberId = memberId.toHexString()
                    )

                    result.shouldBeEmpty()
                    verify(exactly = 0) { support.addAssigneeIfAbsent(any(), any()) }
                }
            }

            When("PRE 과제가 있어도 이미 assignee가 존재하면") {
                Then("중복 추가 없이 빈 목록을 반환한다") {
                    val scheduleId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val existingTask1 = createTask(scheduleId, "기존 대상 과제 1")
                    val existingTask2 = createTask(scheduleId, "기존 대상 과제 2")

                    every {
                        support.loadTasksBySchedule(scheduleId, TaskType.PRE)
                    } returns listOf(existingTask1, existingTask2)
                    every { support.addAssigneeIfAbsent(existingTask1.id!!, memberId) } returns false
                    every { support.addAssigneeIfAbsent(existingTask2.id!!, memberId) } returns false

                    val result = useCase.syncParticipatedScheduleTasks(
                        scheduleId = scheduleId.toHexString(),
                        memberId = memberId.toHexString()
                    )

                    result.shouldBeEmpty()
                }
            }
        }
    }

    private fun createTask(scheduleId: ObjectId, title: String): Task {
        return Task(
            id = ObjectId.get(),
            relatedScheduleId = scheduleId,
            type = TaskType.PRE,
            title = title,
            description = "설명",
            attachments = emptyList(),
            expireAt = LocalDateTime.now().plusDays(1)
        )
    }
}
