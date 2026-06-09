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
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
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

internal class TaskUseCaseTask61ValidationTest : BehaviorSpec() {

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

        Given("TASK 61 생성/수정 24시간 제약") {
            When("생성 시 최종 마감일이 현재 시각 기준 24시간 미만이면") {
                Then("BadRequestException이 발생하고 저장되지 않는다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val schedule = StudySchedule(
                        id = scheduleId,
                        groupId = groupId,
                        scheduleAt = LocalDateTime.now().plusDays(2)
                    )
                    val command = CreateTaskCommand(
                        relatedScheduleId = scheduleId.toHexString(),
                        type = TaskType.POST,
                        title = "과제 제목",
                        description = "과제 설명",
                        attachmentFileIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(1)
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadSchedule(scheduleId.toHexString()) } returns schedule
                    every { support.normalizeExpireAtForCreate(TaskType.POST, command.expireAt, schedule) } returns LocalDateTime.now().plusHours(23)
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs

                    shouldThrow<BadRequestException> {
                        createTaskUseCase.createTask(memberInfo, groupId.toHexString(), command)
                    }.message shouldBe "마감일은 현재 시각 기준 24시간 이후여야 합니다."

                    verify(exactly = 0) { support.saveTask(any()) }
                }
            }

            When("PRE 생성에서 요청 expireAt이 달라도 최종 마감일(scheduleAt)이 24시간 미만이면") {
                Then("BadRequestException이 발생하고 저장되지 않는다") {
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
                        type = TaskType.PRE,
                        title = "과제 제목",
                        description = "과제 설명",
                        attachmentFileIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(7)
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadSchedule(scheduleId.toHexString()) } returns schedule
                    every { support.normalizeExpireAtForCreate(TaskType.PRE, command.expireAt, schedule) } returns schedule.scheduleAt
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs

                    shouldThrow<BadRequestException> {
                        createTaskUseCase.createTask(memberInfo, groupId.toHexString(), command)
                    }.message shouldBe "마감일은 현재 시각 기준 24시간 이후여야 합니다."

                    verify(exactly = 0) { support.saveTask(any()) }
                }
            }

            When("POST 과제 수정 시 정규화된 마감일이 현재 시각 기준 24시간 미만이면") {
                Then("BadRequestException이 발생하고 저장되지 않는다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val task = createTask(type = TaskType.POST, expireAt = LocalDateTime.now().plusDays(3))
                    val command = UpdateTaskCommand(
                        title = "수정 제목",
                        description = "수정 설명",
                        attachmentFileIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(1)
                    )

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(task.identifier) } returns task
                    every {
                        support.normalizeExpireAtForUpdate(task.type, command.expireAt, task.expireAt)
                    } returns LocalDateTime.now().plusHours(23)
                    every { support.toObjectIds(emptyList(), "attachmentFileIds") } returns emptyList()
                    every { support.validateTaskAttachmentFiles(emptyList()) } just runs

                    shouldThrow<BadRequestException> {
                        updateTaskUseCase.updateTask(memberInfo, groupId.toHexString(), task.identifier, command)
                    }.message shouldBe "마감일은 현재 시각 기준 24시간 이후여야 합니다."

                    verify(exactly = 0) { support.saveTask(any()) }
                }
            }
        }

        Given("TASK 61 일정 만료 잠금") {
            val scheduleLockedMessage = "이미 지난 일정의 과제는 수정/삭제/제출/철회할 수 없습니다."

            When("과제 수정 시 연관 일정이 만료되었으면") {
                Then("BadRequestException이 발생한다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val task = createTask(type = TaskType.PRE, expireAt = LocalDateTime.now().plusDays(2))

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } throws BadRequestException(message = scheduleLockedMessage)

                    shouldThrow<BadRequestException> {
                        updateTaskUseCase.updateTask(
                            memberInfo = memberInfo,
                            groupId = groupId.toHexString(),
                            taskId = task.identifier,
                            command = UpdateTaskCommand("제목", "설명", emptyList(), LocalDateTime.now().plusDays(3))
                        )
                    }.message shouldBe scheduleLockedMessage
                }
            }

            When("과제 삭제 시 연관 일정이 만료되었으면") {
                Then("BadRequestException이 발생하고 삭제 처리를 수행하지 않는다") {
                    val ownerId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val memberInfo = createMemberInfo(ownerId)
                    val group = StudyGroup(id = groupId, ownerId = ownerId)
                    val task = createTask(type = TaskType.PRE, expireAt = LocalDateTime.now().plusDays(2))

                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } throws BadRequestException(message = scheduleLockedMessage)

                    shouldThrow<BadRequestException> {
                        removeTaskUseCase.removeTask(memberInfo, groupId.toHexString(), task.identifier)
                    }.message shouldBe scheduleLockedMessage

                    verify(exactly = 0) { taskDeletionSupport.cascadeDeleteTask(any(), any(), any(), any()) }
                }
            }

            When("제출 생성 시 연관 일정이 만료되었으면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(type = TaskType.PRE, expireAt = LocalDateTime.now().plusDays(2))
                    val command = CreateTaskSubmissionCommand("제출 제목", "제출 본문", emptyList())

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } throws BadRequestException(message = scheduleLockedMessage)

                    shouldThrow<BadRequestException> {
                        createSubmissionUseCase.createSubmission(memberInfo, groupId.toHexString(), task.identifier, command)
                    }.message shouldBe scheduleLockedMessage
                }
            }

            When("제출 수정 시 연관 일정이 만료되었으면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(type = TaskType.PRE, expireAt = LocalDateTime.now().plusDays(2))

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } throws BadRequestException(message = scheduleLockedMessage)

                    shouldThrow<BadRequestException> {
                        updateSubmissionUseCase.updateSubmission(
                            memberInfo = memberInfo,
                            groupId = groupId.toHexString(),
                            taskId = task.identifier,
                            submissionId = ObjectId.get().toHexString(),
                            command = UpdateTaskSubmissionCommand("수정 제목", "수정 본문", emptyList())
                        )
                    }.message shouldBe scheduleLockedMessage
                }
            }

            When("제출 철회 시 연관 일정이 만료되었으면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(type = TaskType.PRE, expireAt = LocalDateTime.now().plusDays(2))

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkTaskActionAllowedBySchedule(task) } throws BadRequestException(message = scheduleLockedMessage)

                    shouldThrow<BadRequestException> {
                        withdrawSubmissionUseCase.withdrawSubmission(
                            memberInfo = memberInfo,
                            groupId = groupId.toHexString(),
                            taskId = task.identifier,
                            submissionId = ObjectId.get().toHexString()
                        )
                    }.message shouldBe scheduleLockedMessage
                }
            }
        }

        Given("회귀 검증") {
            When("이미 제출한 과제를 다시 제출하면") {
                Then("기존 충돌 예외를 유지한다") {
                    val groupId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val task = createTask(type = TaskType.PRE, expireAt = LocalDateTime.now().plusDays(2))
                    val assignee = TaskAssignee(taskId = task.id!!, memberId = memberId)
                    val memberInfo = createMemberInfo(memberId)
                    val command = CreateTaskSubmissionCommand("제목", "본문", emptyList())

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(task.identifier) } returns task
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(task.id!!, memberId) } returns assignee
                    every { taskSubmissionRepository.findByTaskIdAndMemberId(task.id!!, memberId) } returns mockk()

                    shouldThrow<ConflictException> {
                        createSubmissionUseCase.createSubmission(memberInfo, groupId.toHexString(), task.identifier, command)
                    }.message shouldBe "이미 제출한 과제입니다."
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

    private fun createTask(type: TaskType, expireAt: LocalDateTime): Task {
        return Task(
            id = ObjectId.get(),
            relatedScheduleId = ObjectId.get(),
            type = type,
            title = "과제 제목",
            description = "과제 설명",
            attachments = emptyList(),
            expireAt = expireAt
        )
    }
}
