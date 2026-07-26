package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskDeletionSupport
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.RemoveTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.RemoveTasksByCanceledScheduleUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.UpdateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAttachment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskDeletedEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskModifiedEvent
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskEventPublicationTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val taskDeletionSupport = TaskDeletionSupport(support, eventPublisher)
    private val createTaskUseCase = CreateTaskUseCaseImpl(support, eventPublisher)
    private val updateTaskUseCase = UpdateTaskUseCaseImpl(support, eventPublisher)
    private val removeTaskUseCase = RemoveTaskUseCaseImpl(support, taskDeletionSupport)
    private val removeTasksByCanceledScheduleUseCase = RemoveTasksByCanceledScheduleUseCaseImpl(support, taskDeletionSupport)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 생성 이벤트 발행") {
            When("과제가 정상 생성되면") {
                Then("TaskCreatedEvent를 정확히 한 번 발행한다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = createGroup(ownerId, groupId)
                    val schedule = StudySchedule(
                        id = scheduleId,
                        groupId = groupId,
                        scheduleAt = LocalDateTime.now().minusDays(1)
                    )
                    val savedTask = createTask(
                        taskId = taskId,
                        scheduleId = scheduleId,
                        expireAt = schedule.scheduleAt
                    )
                    val expectedDto = createTaskDto(savedTask)
                    val command = CreateTaskCommand(
                        relatedScheduleId = scheduleId.toHexString(),
                        title = "과제 생성",
                        description = "과제 설명",
                        attachmentFileIds = emptyList(),
                        assigneeMemberIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(5)
                    )
                    val eventSlot = slot<Any>()

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadSchedule(scheduleId.toHexString()) } returns schedule
                    every { support.resolveTaskTypeBySchedule(schedule, any()) } returns TaskType.PRE
                    every { support.normalizeExpireAtForCreate(TaskType.PRE, command.expireAt, schedule) } returns schedule.scheduleAt
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.toObjectIds(emptyList(), "assigneeMemberIds") } returns emptyList()
                    every { support.saveTask(any()) } returns savedTask
                    every { support.toTaskDto(savedTask, groupId) } returns expectedDto
                    justRun { eventPublisher.publishEvent(any()) }

                    val result = createTaskUseCase.createTask(memberInfo, groupId.toHexString(), command)

                    verify(exactly = 1) { eventPublisher.publishEvent(capture(eventSlot)) }
                    val event = eventSlot.captured as TaskCreatedEvent
                    event.taskId shouldBe savedTask.id
                    event.groupId shouldBe groupId
                    event.scheduleId shouldBe scheduleId
                    event.task shouldBe savedTask
                    event.group shouldBe group
                    result shouldBe expectedDto
                }
            }
        }

        Given("과제 수정 이벤트 발행") {
            When("과제가 정상 수정되면") {
                Then("TaskModifiedEvent를 정확히 한 번 발행한다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = createGroup(ownerId, groupId)
                    val task = createTask(
                        taskId = taskId,
                        scheduleId = scheduleId,
                        expireAt = LocalDateTime.now().plusDays(3)
                    )
                    val updatedTask = createTask(
                        taskId = taskId,
                        scheduleId = scheduleId,
                        expireAt = LocalDateTime.now().plusDays(4),
                        title = "과제 수정",
                        description = "수정된 설명"
                    )
                    val expectedDto = createTaskDto(updatedTask)
                    val command = UpdateTaskCommand(
                        title = "과제 수정",
                        description = "수정된 설명",
                        attachmentFileIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(6)
                    )
                    val eventSlot = slot<Any>()

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } answers { Unit }
                    every { support.normalizeExpireAtForUpdate(TaskType.PRE, command.expireAt, task.expireAt) } returns task.expireAt
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.saveTask(task) } returns updatedTask
                    every { support.toTaskDto(updatedTask, groupId) } returns expectedDto
                    justRun { eventPublisher.publishEvent(any()) }

                    val result = updateTaskUseCase.updateTask(memberInfo, groupId.toHexString(), taskId.toHexString(), command)

                    verify(exactly = 1) { eventPublisher.publishEvent(capture(eventSlot)) }
                    val event = eventSlot.captured as TaskModifiedEvent
                    event.taskId shouldBe updatedTask.id
                    event.groupId shouldBe groupId
                    event.scheduleId shouldBe scheduleId
                    event.task shouldBe updatedTask
                    event.group shouldBe group
                    result shouldBe expectedDto
                }
            }
        }

        Given("과제 삭제 이벤트 발행") {
            When("과제가 정상 삭제되면") {
                Then("TaskDeletedEvent를 정확히 한 번 발행하고 삭제 전 대상자 ID와 그룹을 포함한다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val assigneeId1 = ObjectId.get()
                    val assigneeId2 = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = createGroup(ownerId, groupId)
                    val task = createTask(
                        taskId = taskId,
                        scheduleId = scheduleId,
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(3)
                    )
                    val eventSlot = slot<Any>()

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkOwner(memberInfo, group) } answers { Unit }
                    every { support.checkTaskInGroup(task, groupId) } answers { Unit }
                    every { support.checkTaskActionAllowedBySchedule(task) } answers { Unit }
                    every { support.loadAssigneeMemberIdsByTask(taskId) } returns listOf(assigneeId1, assigneeId1, assigneeId2)
                    every { support.loadSubmissionsByTask(taskId) } returns emptyList()
                    every { support.removeCommentsByTask(taskId) } answers { Unit }
                    every { support.removeSubmissionsByTask(taskId) } answers { Unit }
                    every { support.removeAssigneesByTask(taskId) } answers { Unit }
                    every { support.removeTask(taskId) } answers { Unit }
                    every { support.downgradeOrphanedFiles(emptyList()) } answers { Unit }
                    justRun { eventPublisher.publishEvent(any()) }

                    removeTaskUseCase.removeTask(memberInfo, groupId.toHexString(), taskId.toHexString())

                    verify(exactly = 1) { eventPublisher.publishEvent(capture(eventSlot)) }
                    val event = eventSlot.captured as TaskDeletedEvent
                    event.taskId shouldBe taskId
                    event.groupId shouldBe groupId
                    event.scheduleId shouldBe scheduleId
                    event.task shouldBe task
                    event.group shouldBe group
                    event.assigneeMemberIds shouldBe listOf(assigneeId1, assigneeId2)
                    event.deletedByScheduleEvent shouldBe false
                }
            }
        }

        Given("일정 취소로 인한 과제 삭제 이벤트 발행") {
            When("취소된 일정의 과제가 삭제되면") {
                Then("TaskDeletedEvent를 정확히 한 번 발행하고 group은 null이다") {
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val assigneeId = ObjectId.get()
                    val task = createTask(
                        taskId = taskId,
                        scheduleId = scheduleId,
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(3)
                    )
                    val eventSlot = slot<Any>()

                    every { support.loadTasksBySchedule(scheduleId) } returns listOf(task)
                    every { support.loadAssigneeMemberIdsByTask(taskId) } returns listOf(assigneeId, assigneeId)
                    every { support.loadSubmissionsByTask(taskId) } returns emptyList()
                    every { support.removeCommentsByTask(taskId) } answers { Unit }
                    every { support.removeSubmissionsByTask(taskId) } answers { Unit }
                    every { support.removeAssigneesByTask(taskId) } answers { Unit }
                    every { support.removeTask(taskId) } answers { Unit }
                    every { support.downgradeOrphanedFiles(emptyList()) } answers { Unit }
                    justRun { eventPublisher.publishEvent(any()) }

                    removeTasksByCanceledScheduleUseCase.removeTasksByCanceledSchedule(
                        scheduleId = scheduleId.toHexString(),
                        groupId = groupId.toHexString()
                    )

                    verify(exactly = 1) { eventPublisher.publishEvent(capture(eventSlot)) }
                    val event = eventSlot.captured as TaskDeletedEvent
                    event.taskId shouldBe taskId
                    event.groupId shouldBe groupId
                    event.scheduleId shouldBe scheduleId
                    event.task shouldBe task
                    event.group shouldBe null
                    event.assigneeMemberIds shouldBe listOf(assigneeId)
                    event.deletedByScheduleEvent shouldBe true
                }
            }
        }
    }

    private fun createMemberInfo(memberId: ObjectId): MemberInfo {
        return MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "tester",
            roles = listOf(MemberRole.ROLE_MEMBER.name)
        )
    }

    private fun createGroup(ownerId: ObjectId, groupId: ObjectId): StudyGroup {
        return StudyGroup(
            id = groupId,
            ownerId = ownerId,
            name = "그룹",
            description = "설명"
        )
    }

    private fun createTask(
        taskId: ObjectId,
        scheduleId: ObjectId,
        expireAt: LocalDateTime,
        type: TaskType = TaskType.PRE,
        title: String = "과제",
        description: String = "설명",
        attachments: List<TaskAttachment> = emptyList()
    ): Task {
        return Task(
            id = taskId,
            relatedScheduleId = scheduleId,
            type = type,
            title = title,
            description = description,
            attachments = attachments,
            expireAt = expireAt
        )
    }

    private fun createTaskDto(task: Task): TaskDto {
        return TaskDto(
            taskId = task.identifier,
            relatedScheduleId = task.relatedScheduleId.toHexString(),
            type = task.type,
            title = task.title,
            description = task.description,
            attachments = emptyList(),
            expireAt = task.expireAt,
            submittedAssigneeCount = 0,
            notSubmittedAssigneeCount = 0,
            assignees = emptyList()
        )
    }
}
