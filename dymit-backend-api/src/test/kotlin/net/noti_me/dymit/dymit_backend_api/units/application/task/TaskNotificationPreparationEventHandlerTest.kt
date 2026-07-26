package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.TaskNotificationPreparationEventHandler
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskCreatedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskDeletedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskDeletedEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskModifiedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskModifiedEvent
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskNotificationPreparationEventHandlerTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val handler = TaskNotificationPreparationEventHandler(support, eventPublisher)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 생성 이벤트 핸들러") {
            When("생성 이벤트를 받으면") {
                Then("현재 assignee 기준으로 TaskCreatedBroadcastEvent를 발행한다") {
                    val group = createGroup("스터디")
                    val task = createTask("새 과제")
                    val memberId1 = ObjectId.get()
                    val memberId2 = ObjectId.get()
                    val event = TaskCreatedEvent(
                        taskId = task.id!!,
                        groupId = group.id!!,
                        scheduleId = task.relatedScheduleId,
                        task = task,
                        group = group
                    )
                    val slot = slot<TaskCreatedBroadcastEvent>()

                    every { support.loadAssigneeMemberIdsByTask(task.id!!) } returns listOf(memberId1, memberId1, memberId2)
                    justRun { eventPublisher.publishEvent(any<TaskCreatedBroadcastEvent>()) }

                    handler.onTaskCreated(event)

                    verify(exactly = 1) { support.loadAssigneeMemberIdsByTask(task.id!!) }
                    verify(exactly = 1) { eventPublisher.publishEvent(capture(slot)) }
                    val published = slot.captured
                    published.memberIds shouldContainExactly listOf(memberId1, memberId2)
                    published.toPersonalFeedData().first().eventName shouldBe "TASK_CREATED"
                }
            }
        }

        Given("과제 수정 이벤트 핸들러") {
            When("수정 이벤트를 받으면") {
                Then("현재 assignee 기준으로 TaskModifiedBroadcastEvent를 발행한다") {
                    val group = createGroup("스터디")
                    val task = createTask("수정 과제")
                    val memberId = ObjectId.get()
                    val event = TaskModifiedEvent(
                        taskId = task.id!!,
                        groupId = group.id!!,
                        scheduleId = task.relatedScheduleId,
                        task = task,
                        group = group
                    )
                    val slot = slot<TaskModifiedBroadcastEvent>()

                    every { support.loadAssigneeMemberIdsByTask(task.id!!) } returns listOf(memberId, memberId)
                    justRun { eventPublisher.publishEvent(any<TaskModifiedBroadcastEvent>()) }

                    handler.onTaskModified(event)

                    verify(exactly = 1) { eventPublisher.publishEvent(capture(slot)) }
                    val published = slot.captured
                    published.memberIds shouldContainExactly listOf(memberId)
                    published.toPushMessages().single().eventName shouldBe "TASK_MODIFIED"
                }
            }
        }

        Given("과제 삭제 이벤트 핸들러") {
            When("삭제 이벤트에 group이 없으면") {
                Then("group을 다시 조회해 TaskDeletedBroadcastEvent를 발행한다") {
                    val group = createGroup("스터디")
                    val task = createTask("삭제 과제")
                    val memberId1 = ObjectId.get()
                    val memberId2 = ObjectId.get()
                    val event = TaskDeletedEvent(
                        taskId = task.id!!,
                        groupId = group.id!!,
                        scheduleId = task.relatedScheduleId,
                        task = task,
                        group = null,
                        assigneeMemberIds = listOf(memberId1, memberId1, memberId2),
                        deletedByScheduleEvent = true
                    )
                    val slot = slot<TaskDeletedBroadcastEvent>()

                    every { support.loadGroup(group.id!!.toHexString()) } returns group
                    justRun { eventPublisher.publishEvent(any<TaskDeletedBroadcastEvent>()) }

                    handler.onTaskDeleted(event)

                    verify(exactly = 1) { support.loadGroup(group.id!!.toHexString()) }
                    verify(exactly = 1) { eventPublisher.publishEvent(capture(slot)) }
                    val published = slot.captured
                    published.memberIds shouldContainExactly listOf(memberId1, memberId2)
                    published.toPersonalFeedData().first().resources.single().resourceId shouldBe group.identifier
                }
            }
        }
    }

    private fun createGroup(name: String): StudyGroup {
        return StudyGroup(
            id = ObjectId.get(),
            ownerId = ObjectId.get(),
            name = name,
            description = "설명"
        )
    }

    private fun createTask(title: String): Task {
        return Task(
            id = ObjectId.get(),
            relatedScheduleId = ObjectId.get(),
            type = TaskType.PRE,
            title = title,
            description = "설명",
            attachments = emptyList(),
            expireAt = LocalDateTime.now().plusDays(2)
        )
    }
}
