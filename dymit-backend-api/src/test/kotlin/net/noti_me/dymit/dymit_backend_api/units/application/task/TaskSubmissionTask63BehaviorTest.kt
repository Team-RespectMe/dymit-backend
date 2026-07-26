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
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionAttachmentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.UpdateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.WithdrawCheckSubmissionByAssigneeUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskSubmissionTask63BehaviorTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val taskSubmissionRepository = mockk<TaskSubmissionRepository>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val createSubmissionUseCase = CreateSubmissionUseCaseImpl(support, taskSubmissionRepository, eventPublisher)
    private val updateSubmissionUseCase = UpdateSubmissionUseCaseImpl(support)
    private val withdrawCheckSubmissionUseCase = WithdrawCheckSubmissionByAssigneeUseCaseImpl(support)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("체크형 과제 제출 생성") {
            When("대상자가 처음 제출하면") {
                Then("assignee 상태만 저장하고 실제 submission 저장이나 파일 처리 없이 synthetic DTO를 반환한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = MemberInfo(
                        memberId = memberId.toHexString(),
                        nickname = "submitter",
                        roles = listOf(MemberRole.ROLE_MEMBER.name)
                    )
                    val member = StudyGroupMember(
                        groupId = groupId,
                        memberId = memberId,
                        nickname = "submitter"
                    )
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.POST,
                        title = "체크 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(1),
                        submissionType = TaskSubmissionType.CHECK
                    )
                    val assignee = TaskAssignee(
                        id = ObjectId.get(),
                        taskId = taskId,
                        memberId = memberId,
                        status = TaskAssigneeStatus.NOT_SUBMITTED
                    )
                    val command = CreateTaskSubmissionCommand(
                        title = "무시되는 제목",
                        content = "무시되는 본문",
                        attachments = listOf(
                            TaskSubmissionAttachmentCommand(
                                type = TaskSubmitAttachmentType.FILE,
                                title = "첨부",
                                fileId = ObjectId.get().toHexString()
                            )
                        )
                    )

                    every { support.requireGroupMember(groupId, memberId) } returns member
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(taskId, memberId) } returns assignee
                    every { support.saveAssignee(any()) } answers { firstArg() }

                    val result = createSubmissionUseCase.createSubmission(
                        memberInfo = memberInfo,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        command = command
                    )

                    verify(exactly = 1) {
                        support.saveAssignee(match { it.status == TaskAssigneeStatus.SUBMITTED })
                    }
                    verify(exactly = 0) { taskSubmissionRepository.findByTaskIdAndMemberId(any(), any()) }
                    verify(exactly = 0) { support.toSubmissionAttachments(any()) }
                    verify(exactly = 0) { support.validateSubmissionAttachmentFiles(any()) }
                    verify(exactly = 0) { support.saveSubmission(any()) }
                    verify(exactly = 0) { support.updateFileStatuses(any(), any()) }
                    verify(exactly = 0) { eventPublisher.publishEvent(any()) }
                    result.submissionId shouldBe assignee.identifier
                    result.taskId shouldBe taskId.toHexString()
                    result.memberId shouldBe memberId.toHexString()
                    result.memberProfileImageType shouldBe ProfileImageType.PRESET
                    result.attachments shouldBe emptyList()
                }
            }
        }

        Given("체크형 과제 제출 수정") {
            When("수정을 시도하면") {
                Then("deterministic BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.POST,
                        title = "체크 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(1),
                        submissionType = TaskSubmissionType.CHECK
                    )

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(taskId, memberId) } returns mockk()

                    shouldThrow<BadRequestException> {
                        updateSubmissionUseCase.updateSubmission(
                            memberInfo = MemberInfo(
                                memberId = memberId.toHexString(),
                                nickname = "submitter",
                                roles = listOf(MemberRole.ROLE_MEMBER.name)
                            ),
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            submissionId = submissionId.toHexString(),
                            command = UpdateTaskSubmissionCommand("제목", "본문", emptyList())
                        )
                    }.message shouldBe "체크형 과제 제출은 수정할 수 없습니다."

                    verify(exactly = 0) { support.loadSubmission(any()) }
                    verify(exactly = 0) { support.saveSubmission(any()) }
                }
            }
        }

        Given("체크형 과제 제출 철회") {
            When("assigneeId 쿼리 경로로 본인 제출을 철회하면") {
                Then("assignee 상태만 저장하고 제출 삭제 로직은 호출하지 않는다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.POST,
                        title = "체크 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(1),
                        submissionType = TaskSubmissionType.CHECK
                    )
                    val assignee = TaskAssignee(
                        taskId = taskId,
                        memberId = memberId,
                        status = TaskAssigneeStatus.SUBMITTED
                    )

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(taskId, memberId) } returns assignee
                    every { support.saveAssignee(any()) } answers { firstArg() }

                    withdrawCheckSubmissionUseCase.withdrawCheckSubmissionByAssignee(
                        memberInfo = MemberInfo(
                            memberId = memberId.toHexString(),
                            nickname = "submitter",
                            roles = listOf(MemberRole.ROLE_MEMBER.name)
                        ),
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        assigneeId = memberId.toHexString()
                    )

                    verify(exactly = 1) {
                        support.saveAssignee(match { it.status == TaskAssigneeStatus.NOT_SUBMITTED })
                    }
                    verify(exactly = 0) { support.removeCommentsBySubmission(any()) }
                    verify(exactly = 0) { support.removeSubmissionById(any()) }
                    verify(exactly = 0) { support.downgradeOrphanedFiles(any()) }
                }
            }
        }

        Given("출력형 과제 제출 수정") {
            When("첨부 파일을 교체하면") {
                Then("기존 submission 저장과 파일 링크/언링크 처리 회귀가 없다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val oldFileId = ObjectId.get()
                    val newFileId = ObjectId.get()
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.POST,
                        title = "산출물 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(1),
                        submissionType = TaskSubmissionType.OUTPUT
                    )
                    val submission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = memberId,
                        title = "이전 제목",
                        content = "이전 본문",
                        attachments = listOf(
                            TaskSubmitAttachment(
                                type = TaskSubmitAttachmentType.FILE,
                                title = "이전 첨부",
                                fileId = oldFileId
                            )
                        )
                    )
                    val newAttachments = listOf(
                        TaskSubmitAttachment(
                            type = TaskSubmitAttachmentType.FILE,
                            title = "새 첨부",
                            fileId = newFileId
                        )
                    )
                    val expectedDto = TaskSubmissionDto(
                        submissionId = submissionId.toHexString(),
                        taskId = taskId.toHexString(),
                        memberId = memberId.toHexString(),
                        memberNickname = "submitter",
                        memberProfileImageUrl = "",
                        memberProfileImageType = ProfileImageType.PRESET,
                        title = "새 제목",
                        content = "새 본문",
                        attachments = emptyList(),
                        createdAt = submission.createdAt
                    )

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(taskId, memberId) } returns mockk()
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.submissionAttachmentFileIds(submission.attachments) } returns listOf(oldFileId)
                    every { support.toSubmissionAttachments(any()) } returns newAttachments
                    every { support.submissionAttachmentFileIds(newAttachments) } returns listOf(newFileId)
                    every { support.validateSubmissionAttachmentFiles(listOf(newFileId)) } just runs
                    every { support.saveSubmission(any()) } returns submission
                    every { support.updateFileStatuses(listOf(newFileId), UserFileStatus.LINKED) } just runs
                    every { support.downgradeOrphanedFiles(listOf(oldFileId)) } just runs
                    every { support.toSubmissionDto(submission, groupId) } returns expectedDto

                    val result = updateSubmissionUseCase.updateSubmission(
                        memberInfo = MemberInfo(
                            memberId = memberId.toHexString(),
                            nickname = "submitter",
                            roles = listOf(MemberRole.ROLE_MEMBER.name)
                        ),
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        command = UpdateTaskSubmissionCommand(
                            title = "새 제목",
                            content = "새 본문",
                            attachments = listOf(
                                TaskSubmissionAttachmentCommand(
                                    type = TaskSubmitAttachmentType.FILE,
                                    title = "새 첨부",
                                    fileId = newFileId.toHexString()
                                )
                            )
                        )
                    )

                    verify(exactly = 1) { support.saveSubmission(any()) }
                    verify(exactly = 1) { support.updateFileStatuses(listOf(newFileId), UserFileStatus.LINKED) }
                    verify(exactly = 1) { support.downgradeOrphanedFiles(listOf(oldFileId)) }
                    result shouldBe expectedDto
                }
            }
        }
    }
}
