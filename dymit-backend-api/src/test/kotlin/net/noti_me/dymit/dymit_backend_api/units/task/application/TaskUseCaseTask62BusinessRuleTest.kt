package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldNotContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.TaskDeletionSupport
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.CreateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.CreateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.RemoveTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.UpdateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.UpdateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.WithdrawSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto as StudySchedule
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskSubmissionRepository
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

        Given("TASK-64.2 생성 규칙") {
            When("생성 커맨드 필드를 확인하면") {
                Then("type 필드는 없다") {
                    CreateTaskCommand::class.java.declaredFields.map { it.name } shouldNotContain "type"
                }
            }

            When("연관 일정이 24시간보다 더 남아 있으면") {
                Then("PRE 과제로 저장하고 일정 참여자를 대상자로 초기화한다") {
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
                        title = "과제 제목",
                        description = "과제 설명",
                        attachmentFileIds = emptyList(),
                        assigneeMemberIds = listOf(assigneeId1.toHexString(), assigneeId2.toHexString()),
                        expireAt = LocalDateTime.of(2026, 6, 11, 8, 0, 0)
                    )
                    val savedTask = createTask(
                        type = TaskType.PRE,
                        expireAt = schedule.scheduleAt,
                        scheduleId = scheduleId
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadSchedule(scheduleId.toHexString()) } returns schedule
                    every { support.resolveTaskTypeBySchedule(schedule, any()) } returns TaskType.PRE
                    every { support.validatePreTaskCreatable(schedule, any()) } just runs
                    every {
                        support.normalizeExpireAtForCreate(TaskType.PRE, command.expireAt, schedule)
                    } returns schedule.scheduleAt
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every {
                        support.toObjectIds(command.assigneeMemberIds, "assigneeMemberIds")
                    } returns listOf(assigneeId1, assigneeId2)
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs
                    every {
                        support.saveTask(match { task ->
                            task.type == TaskType.PRE && task.expireAt == schedule.scheduleAt
                        })
                    } returns savedTask
                    every { support.toTaskDto(savedTask, groupId) } returns mockk()

                    createTaskUseCase.createTask(memberInfo, groupId.toHexString(), command)

                    verify(exactly = 1) { support.validatePreTaskCreatable(schedule, any()) }
                    verify(exactly = 0) { support.toObjectIds(command.assigneeMemberIds, "assigneeMemberIds") }
                    verify(exactly = 1) { support.initializeAssigneesForPreTask(savedTask.id!!, scheduleId) }
                    verify(exactly = 0) { support.initializeAssignees(any(), any()) }
                    verify(exactly = 0) { support.validateAssigneeMembersInGroup(any(), any()) }
                }
            }

            When("연관 일정이 24시간 이내로 남아 있으면") {
                Then("BadRequestException이 발생하고 저장하지 않는다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val schedule = StudySchedule(
                        id = scheduleId,
                        groupId = groupId,
                        scheduleAt = LocalDateTime.now().plusHours(23)
                    )
                    val command = CreateTaskCommand(
                        relatedScheduleId = scheduleId.toHexString(),
                        title = "과제 제목",
                        description = "과제 설명",
                        attachmentFileIds = emptyList(),
                        assigneeMemberIds = listOf(ObjectId.get().toHexString()),
                        expireAt = LocalDateTime.now().plusDays(1)
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadSchedule(scheduleId.toHexString()) } returns schedule
                    every { support.resolveTaskTypeBySchedule(schedule, any()) } returns TaskType.PRE
                    every {
                        support.validatePreTaskCreatable(schedule, any())
                    } throws BadRequestException(message = "사전 과제는 일정 시작 24시간 이전에만 생성할 수 있습니다.")

                    shouldThrow<BadRequestException> {
                        createTaskUseCase.createTask(memberInfo, groupId.toHexString(), command)
                    }.message shouldBe "사전 과제는 일정 시작 24시간 이전에만 생성할 수 있습니다."

                    verify(exactly = 0) { support.saveTask(any()) }
                    verify(exactly = 0) { support.initializeAssigneesForPreTask(any(), any()) }
                    verify(exactly = 0) { support.initializeAssignees(any(), any()) }
                }
            }

            When("연관 일정이 이미 시작되었으면") {
                Then("POST 과제로 저장하고 요청 대상자를 검증해 초기화한다") {
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
                        scheduleAt = LocalDateTime.now().minusDays(1)
                    )
                    val command = CreateTaskCommand(
                        relatedScheduleId = scheduleId.toHexString(),
                        title = "과제 제목",
                        description = "과제 설명",
                        attachmentFileIds = emptyList(),
                        assigneeMemberIds = listOf(assigneeId1.toHexString(), assigneeId2.toHexString()),
                        expireAt = LocalDateTime.now().plusDays(3)
                    )
                    val savedTask = createTask(
                        type = TaskType.POST,
                        expireAt = LocalDateTime.now().plusDays(3),
                        scheduleId = scheduleId
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadSchedule(scheduleId.toHexString()) } returns schedule
                    every { support.resolveTaskTypeBySchedule(schedule, any()) } returns TaskType.POST
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

                    verify(exactly = 0) { support.validatePreTaskCreatable(any(), any()) }
                    verify(exactly = 1) { support.validateAssigneeMembersInGroup(groupId, listOf(assigneeId1, assigneeId2)) }
                    verify(exactly = 1) { support.initializeAssignees(savedTask.id!!, listOf(assigneeId1, assigneeId2)) }
                    verify(exactly = 0) { support.initializeAssigneesForPreTask(any(), any()) }
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
                            UpdateTaskCommand(
                                "제목",
                                "설명",
                                emptyList(),
                                LocalDateTime.now().plusDays(1)
                            )
                        )
                    }.message shouldBe expiredMessage
                }
            }

            When("PRE 과제를 수정하면서 대상자 목록을 보내면") {
                Then("대상자 목록을 무시하고 대상자를 변경하지 않는다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val assigneeId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val task = createTask(type = TaskType.PRE, expireAt = LocalDateTime.now().plusDays(1))
                    val command = UpdateTaskCommand(
                        title = "수정 제목",
                        description = "수정 설명",
                        attachmentFileIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(3),
                        assigneeMemberIds = listOf(assigneeId.toHexString())
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } just runs
                    every { support.normalizeExpireAtForUpdate(TaskType.PRE, command.expireAt, task.expireAt) } returns task.expireAt
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs
                    every { support.saveTask(task) } returns task
                    every { support.toTaskDto(task, groupId) } returns mockk()

                    updateTaskUseCase.updateTask(memberInfo, groupId.toHexString(), task.identifier, command)

                    verify(exactly = 0) { support.toObjectIds(command.assigneeMemberIds!!, "assigneeMemberIds") }
                    verify(exactly = 0) { support.validateAssigneeMembersInGroup(any(), any()) }
                    verify(exactly = 0) { support.loadAssigneeMemberIdsByTask(any()) }
                    verify(exactly = 0) { support.addAssigneeIfAbsent(any(), any()) }
                    verify(exactly = 0) { support.removeAssigneeWithSubmissionCleanup(any(), any()) }
                }
            }

            When("POST 과제를 수정하면서 assigneeMemberIds를 null로 보내면") {
                Then("기존 대상자를 유지하고 대상자 검증과 동기화를 수행하지 않는다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val task = createTask(type = TaskType.POST, expireAt = LocalDateTime.now().plusDays(1))
                    val command = UpdateTaskCommand("수정 제목", "수정 설명", emptyList(), LocalDateTime.now().plusDays(2), null)

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } just runs
                    every { support.normalizeExpireAtForUpdate(TaskType.POST, command.expireAt, task.expireAt) } returns command.expireAt
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs
                    every { support.saveTask(task) } returns task
                    every { support.toTaskDto(task, groupId) } returns mockk()

                    updateTaskUseCase.updateTask(memberInfo, groupId.toHexString(), task.identifier, command)

                    verify(exactly = 0) { support.validateAssigneeMembersInGroup(any(), any()) }
                    verify(exactly = 0) { support.loadAssigneeMemberIdsByTask(any()) }
                    verify(exactly = 0) { support.addAssigneeIfAbsent(any(), any()) }
                    verify(exactly = 0) { support.removeAssigneeWithSubmissionCleanup(any(), any()) }
                }
            }

            When("POST 과제를 수정하면서 대상자를 바꾸면") {
                Then("대상자를 검증하고 추가/삭제를 동기화하며 제거된 제출 데이터를 정리한다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val removedMemberId = ObjectId.get()
                    val keptMemberId = ObjectId.get()
                    val addedMemberId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val task = createTask(
                        type = TaskType.POST,
                        expireAt = LocalDateTime.now().plusDays(1)
                    )
                    val command = UpdateTaskCommand(
                        title = "수정 제목",
                        description = "수정 설명",
                        attachmentFileIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2),
                        assigneeMemberIds = listOf(keptMemberId.toHexString(), addedMemberId.toHexString())
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } just runs
                    every {
                        support.normalizeExpireAtForUpdate(TaskType.POST, command.expireAt, task.expireAt)
                    } returns command.expireAt
                    every {
                        support.toObjectIds(command.assigneeMemberIds!!, "assigneeMemberIds")
                    } returns listOf(keptMemberId, addedMemberId)
                    every { support.validateAssigneeMembersInGroup(groupId, listOf(keptMemberId, addedMemberId)) } just runs
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs
                    every { support.saveTask(task) } returns task
                    every { support.loadAssigneeMemberIdsByTask(task.id!!) } returns listOf(removedMemberId, keptMemberId)
                    every { support.removeAssigneeWithSubmissionCleanup(task.id!!, removedMemberId) } just runs
                    every { support.addAssigneeIfAbsent(task.id!!, addedMemberId) } returns true
                    every { support.toTaskDto(task, groupId) } returns mockk()

                    updateTaskUseCase.updateTask(memberInfo, groupId.toHexString(), task.identifier, command)

                    verify(exactly = 1) {
                        support.validateAssigneeMembersInGroup(groupId, listOf(keptMemberId, addedMemberId))
                    }
                    verify(exactly = 1) { support.removeAssigneeWithSubmissionCleanup(task.id!!, removedMemberId) }
                    verify(exactly = 1) { support.addAssigneeIfAbsent(task.id!!, addedMemberId) }
                    verify(exactly = 0) { support.removeAssigneeWithSubmissionCleanup(task.id!!, keptMemberId) }
                    verify(exactly = 0) { support.addAssigneeIfAbsent(task.id!!, keptMemberId) }
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
            roles = listOf(MemberRole.ROLE_MEMBER.name)
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
