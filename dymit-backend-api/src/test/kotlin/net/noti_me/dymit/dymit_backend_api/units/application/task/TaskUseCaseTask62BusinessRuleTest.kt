package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskDeletionSupport
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.RemoveTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.UpdateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.UpdateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.WithdrawSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskUseCaseTask62BusinessRuleTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val taskDeletionSupport = mockk<TaskDeletionSupport>(relaxed = true)
    private val taskSubmissionRepository = mockk<TaskSubmissionRepository>(relaxed = true)

    private val createTaskUseCase = CreateTaskUseCaseImpl(support, eventPublisher)
    private val updateTaskUseCase = UpdateTaskUseCaseImpl(support, eventPublisher)
    private val removeTaskUseCase = RemoveTaskUseCaseImpl(support, taskDeletionSupport)
    private val createSubmissionUseCase = CreateSubmissionUseCaseImpl(support, taskSubmissionRepository, eventPublisher)
    private val updateSubmissionUseCase = UpdateSubmissionUseCaseImpl(support)
    private val withdrawSubmissionUseCase = WithdrawSubmissionUseCaseImpl(support)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("TASK-62 생성 규칙") {
            When("연관 일정이 요청 시점보다 이후면") {
                Then("요청 type을 무시하고 POST 과제로 저장하며 요청 대상자를 초기화한다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val assigneeId1 = ObjectId.get()
                    val assigneeId2 = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val schedule = StudySchedule(
                        id = scheduleId,
                        groupId = groupId,
                        scheduleAt = LocalDateTime.now().plusDays(2)
                    )
                    val command = CreateTaskCommand(
                        relatedScheduleId = scheduleId.toHexString(),
                        type = TaskType.PRE,
                        title = "과제 제목",
                        description = "과제 설명",
                        attachmentFileIds = emptyList(),
                        assigneeMemberIds = listOf(assigneeId1.toHexString(), assigneeId2.toHexString()),
                        expireAt = LocalDateTime.of(2026, 6, 11, 8, 0, 0)
                    )
                    val savedTask = createTask(
                        type = TaskType.POST,
                        expireAt = LocalDateTime.of(2026, 6, 11, 14, 59, 59),
                        scheduleId = scheduleId
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadSchedule(scheduleId.toHexString()) } returns schedule
                    every { support.resolveTaskTypeBySchedule(schedule) } returns TaskType.POST
                    every {
                        support.normalizeExpireAtForCreate(TaskType.POST, command.expireAt, schedule)
                    } returns savedTask.expireAt
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every {
                        support.toObjectIds(command.assigneeMemberIds, "assigneeMemberIds")
                    } returns listOf(assigneeId1, assigneeId2)
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs
                    every { support.validateAssigneeMembersInGroup(groupId, listOf(assigneeId1, assigneeId2)) } just runs
                    every {
                        support.saveTask(match { task ->
                            task.type == TaskType.POST && task.expireAt == savedTask.expireAt
                        })
                    } returns savedTask
                    every { support.toTaskDto(savedTask, groupId) } returns mockk()

                    createTaskUseCase.createTask(memberInfo, groupId.toHexString(), command)

                    verify(exactly = 1) { support.initializeAssignees(savedTask.id!!, listOf(assigneeId1, assigneeId2)) }
                    verify(exactly = 0) { support.initializeAssigneesForPreTask(any(), any()) }
                }
            }

            When("연관 일정이 요청 시점보다 이전이면") {
                Then("요청 type과 대상자 목록을 무시하고 PRE 과제로 저장하며 일정 참여자를 초기화한다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val schedule = StudySchedule(
                        id = scheduleId,
                        groupId = groupId,
                        scheduleAt = LocalDateTime.now().minusDays(1)
                    )
                    val command = CreateTaskCommand(
                        relatedScheduleId = scheduleId.toHexString(),
                        type = TaskType.POST,
                        title = "과제 제목",
                        description = "과제 설명",
                        attachmentFileIds = emptyList(),
                        assigneeMemberIds = listOf(ObjectId.get().toHexString()),
                        expireAt = LocalDateTime.now().plusDays(3)
                    )
                    val savedTask = createTask(
                        type = TaskType.PRE,
                        expireAt = schedule.scheduleAt,
                        scheduleId = scheduleId
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadSchedule(scheduleId.toHexString()) } returns schedule
                    every { support.resolveTaskTypeBySchedule(schedule) } returns TaskType.PRE
                    every {
                        support.normalizeExpireAtForCreate(TaskType.PRE, command.expireAt, schedule)
                    } returns schedule.scheduleAt
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.toObjectIds(command.assigneeMemberIds, "assigneeMemberIds") } returns listOf(ObjectId.get())
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs
                    every {
                        support.saveTask(match { task ->
                            task.type == TaskType.PRE && task.expireAt == schedule.scheduleAt
                        })
                    } returns savedTask
                    every { support.toTaskDto(savedTask, groupId) } returns mockk()

                    createTaskUseCase.createTask(memberInfo, groupId.toHexString(), command)

                    verify(exactly = 1) { support.initializeAssigneesForPreTask(savedTask.id!!, scheduleId) }
                    verify(exactly = 0) { support.initializeAssignees(any(), any()) }
                    verify(exactly = 0) { support.validateAssigneeMembersInGroup(any(), any()) }
                }
            }
        }

        Given("TASK-62 수정/삭제 잠금 규칙") {
            val expiredMessage = "마감된 과제는 수정/삭제할 수 없습니다."

            When("과제 수정 시 마감일이 지났으면") {
                Then("BadRequestException이 발생한다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val task = createTask(type = TaskType.POST, expireAt = LocalDateTime.now().minusMinutes(1))

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } throws BadRequestException(message = expiredMessage)

                    shouldThrow<BadRequestException> {
                        updateTaskUseCase.updateTask(
                            memberInfo,
                            groupId.toHexString(),
                            task.identifier,
                            UpdateTaskCommand("제목", "설명", emptyList(), LocalDateTime.now().plusDays(1))
                        )
                    }.message shouldBe expiredMessage
                }
            }

            When("과제 삭제 시 마감일이 지났으면") {
                Then("BadRequestException이 발생하고 cascade delete를 호출하지 않는다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val task = createTask(type = TaskType.POST, expireAt = LocalDateTime.now().minusMinutes(1))

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } throws BadRequestException(message = expiredMessage)

                    shouldThrow<BadRequestException> {
                        removeTaskUseCase.removeTask(memberInfo, groupId.toHexString(), task.identifier)
                    }.message shouldBe expiredMessage

                    verify(exactly = 0) { taskDeletionSupport.cascadeDeleteTask(any(), any(), any(), any()) }
                }
            }
        }

        Given("TASK-62 제출 잠금 규칙") {
            val expiredMessage = "마감된 과제는 제출/수정/철회할 수 없습니다."

            When("과제 제출 생성 시 마감일이 지났으면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(type = TaskType.POST, expireAt = LocalDateTime.now().minusMinutes(1))

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkSubmissionUpdatable(task) } throws BadRequestException(message = expiredMessage)

                    shouldThrow<BadRequestException> {
                        createSubmissionUseCase.createSubmission(
                            memberInfo,
                            groupId.toHexString(),
                            task.identifier,
                            CreateTaskSubmissionCommand("제목", "본문", emptyList())
                        )
                    }.message shouldBe expiredMessage
                }
            }

            When("과제 제출 수정 시 마감일이 지났으면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(type = TaskType.POST, expireAt = LocalDateTime.now().minusMinutes(1))

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkSubmissionUpdatable(task) } throws BadRequestException(message = expiredMessage)

                    shouldThrow<BadRequestException> {
                        updateSubmissionUseCase.updateSubmission(
                            memberInfo,
                            groupId.toHexString(),
                            task.identifier,
                            ObjectId.get().toHexString(),
                            UpdateTaskSubmissionCommand("제목", "본문", emptyList())
                        )
                    }.message shouldBe expiredMessage
                }
            }

            When("과제 제출 철회 시 마감일이 지났으면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(type = TaskType.POST, expireAt = LocalDateTime.now().minusMinutes(1))

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkSubmissionUpdatable(task) } throws BadRequestException(message = expiredMessage)

                    shouldThrow<BadRequestException> {
                        withdrawSubmissionUseCase.withdrawSubmission(
                            memberInfo,
                            groupId.toHexString(),
                            task.identifier,
                            ObjectId.get().toHexString()
                        )
                    }.message shouldBe expiredMessage
                }
            }
        }
    }

    private fun createMemberInfo(memberId: ObjectId): MemberInfo {
        return MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "tester",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    private fun createTask(
        type: TaskType,
        expireAt: LocalDateTime,
        scheduleId: ObjectId = ObjectId.get()
    ): Task {
        return Task(
            id = ObjectId.get(),
            relatedScheduleId = scheduleId,
            type = type,
            title = "과제 제목",
            description = "과제 설명",
            attachments = emptyList(),
            expireAt = expireAt
        )
    }
}
