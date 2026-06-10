package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionAttachmentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionAttachmentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.UpdateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.WithdrawCheckSubmissionByAssigneeUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.WithdrawSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
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

internal class TaskSubmissionTask63BusinessRuleTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val taskSubmissionRepository = mockk<TaskSubmissionRepository>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val createSubmissionUseCase = CreateSubmissionUseCaseImpl(support, taskSubmissionRepository, eventPublisher)
    private val updateSubmissionUseCase = UpdateSubmissionUseCaseImpl(support)
    private val withdrawSubmissionUseCase = WithdrawSubmissionUseCaseImpl(support)
    private val withdrawCheckSubmissionByAssigneeUseCase = WithdrawCheckSubmissionByAssigneeUseCaseImpl(support)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("CHECK 제출 생성") {
            When("대상자가 체크형 과제를 제출하면") {
                Then("assignee만 submitted로 바꾸고 submission 저장이나 파일 링크 변경은 하지 않는다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val member = createStudyGroupMember(groupId, memberId)
                    val task = createTask(taskId = taskId, submissionType = TaskSubmissionType.CHECK)
                    val assignee = TaskAssignee(
                        id = ObjectId.get(),
                        taskId = taskId,
                        memberId = memberId
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
                        command = CreateTaskSubmissionCommand("무시될 제목", "무시될 본문", emptyList())
                    )

                    result.taskId shouldBe taskId.toHexString()
                    result.memberId shouldBe memberId.toHexString()
                    result.title shouldBe ""
                    result.content shouldBe ""
                    result.attachments.shouldBeEmpty()
                    verify(exactly = 1) { support.saveAssignee(match { it.status == TaskAssigneeStatus.SUBMITTED }) }
                    verify(exactly = 0) { support.saveSubmission(any()) }
                    verify(exactly = 0) { taskSubmissionRepository.save(any()) }
                    verify(exactly = 0) { support.updateFileStatuses(any<Collection<ObjectId>>(), any()) }
                    verify(exactly = 0) { eventPublisher.publishEvent(any()) }
                }
            }
        }

        Given("CHECK 제출 수정") {
            When("체크형 과제 제출을 수정하려고 하면") {
                Then("결정적인 예외 메시지와 함께 거절된다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(taskId = taskId, submissionType = TaskSubmissionType.CHECK)

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(taskId, memberId) } returns mockk()

                    shouldThrow<BadRequestException> {
                        updateSubmissionUseCase.updateSubmission(
                            memberInfo = memberInfo,
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            submissionId = ObjectId.get().toHexString(),
                            command = UpdateTaskSubmissionCommand("수정 제목", "수정 본문", emptyList())
                        )
                    }.message shouldBe "체크형 과제 제출은 수정할 수 없습니다."

                    verify(exactly = 0) { support.loadSubmission(any()) }
                    verify(exactly = 0) { support.saveSubmission(any()) }
                    verify(exactly = 0) { support.updateFileStatuses(any<Collection<ObjectId>>(), any()) }
                    verify(exactly = 0) { support.downgradeOrphanedFiles(any<Collection<ObjectId>>()) }
                }
            }
        }

        Given("CHECK 제출 철회 assignee query path") {
            When("assigneeId 기준 철회를 수행하면") {
                Then("assignee를 not submitted로 바꾸고 TaskSubmission 삭제 경로는 타지 않는다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(taskId = taskId, submissionType = TaskSubmissionType.CHECK)
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

                    withdrawCheckSubmissionByAssigneeUseCase.withdrawCheckSubmissionByAssignee(
                        memberInfo = memberInfo,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        assigneeId = memberId.toHexString()
                    )

                    verify(exactly = 1) { support.saveAssignee(match { it.status == TaskAssigneeStatus.NOT_SUBMITTED }) }
                    verify(exactly = 0) { support.loadSubmission(any()) }
                    verify(exactly = 0) { support.removeSubmissionById(any()) }
                    verify(exactly = 0) { support.removeCommentsBySubmission(any()) }
                    verify(exactly = 0) { support.downgradeOrphanedFiles(any<Collection<ObjectId>>()) }
                }
            }
        }

        Given("OUTPUT 제출 회귀") {
            When("출력형 과제를 생성하면") {
                Then("기존처럼 submission 저장과 파일 링크 갱신을 수행한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val fileId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val member = createStudyGroupMember(groupId, memberId)
                    val task = createTask(taskId = taskId, submissionType = TaskSubmissionType.OUTPUT)
                    val assignee = TaskAssignee(taskId = taskId, memberId = memberId)
                    val attachments = listOf(
                        TaskSubmitAttachment(
                            type = TaskSubmitAttachmentType.FILE,
                            title = "파일 첨부",
                            fileId = fileId
                        )
                    )
                    val savedSubmission = TaskSubmission(
                        id = ObjectId.get(),
                        taskId = taskId,
                        memberId = memberId,
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = attachments
                    )
                    val dto = createSubmissionDto(savedSubmission, fileId)

                    every { support.requireGroupMember(groupId, memberId) } returns member
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(taskId, memberId) } returns assignee
                    every { taskSubmissionRepository.findByTaskIdAndMemberId(taskId, memberId) } returns null
                    every { support.toSubmissionAttachments(any()) } returns attachments
                    every { support.submissionAttachmentFileIds(attachments) } returns listOf(fileId)
                    every { support.validateSubmissionAttachmentFiles(listOf(fileId)) } just runs
                    every { support.saveSubmission(any()) } returns savedSubmission
                    every { support.saveAssignee(any()) } answers { firstArg() }
                    every { support.updateFileStatuses(listOf(fileId), UserFileStatus.LINKED) } just runs
                    every { support.loadGroup(groupId.toHexString()) } returns mockk()
                    every { support.toSubmissionDto(savedSubmission, groupId) } returns dto

                    val result = createSubmissionUseCase.createSubmission(
                        memberInfo = memberInfo,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        command = CreateTaskSubmissionCommand(
                            title = "제출 제목",
                            content = "제출 본문",
                            attachments = listOf(
                                TaskSubmissionAttachmentCommand(
                                    type = TaskSubmitAttachmentType.FILE,
                                    title = "파일 첨부",
                                    fileId = fileId.toHexString()
                                )
                            )
                        )
                    )

                    result shouldBe dto
                    verify(exactly = 1) { support.saveSubmission(any()) }
                    verify(exactly = 1) { support.updateFileStatuses(listOf(fileId), UserFileStatus.LINKED) }
                    verify(exactly = 1) { support.saveAssignee(match { it.status == TaskAssigneeStatus.SUBMITTED }) }
                }
            }

            When("출력형 과제를 수정하면") {
                Then("기존처럼 submission 저장과 파일 링크/강등 처리를 유지한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val oldFileId = ObjectId.get()
                    val newFileId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(taskId = taskId, submissionType = TaskSubmissionType.OUTPUT)
                    val submission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = memberId,
                        title = "이전 제목",
                        content = "이전 본문",
                        attachments = listOf(
                            TaskSubmitAttachment(
                                type = TaskSubmitAttachmentType.FILE,
                                title = "기존 파일",
                                fileId = oldFileId
                            )
                        )
                    )
                    val updatedAttachments = listOf(
                        TaskSubmitAttachment(
                            type = TaskSubmitAttachmentType.FILE,
                            title = "새 파일",
                            fileId = newFileId
                        )
                    )
                    val dto = createSubmissionDto(submission, newFileId)

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(taskId, memberId) } returns mockk()
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.submissionAttachmentFileIds(submission.attachments) } returns listOf(oldFileId)
                    every { support.toSubmissionAttachments(any()) } returns updatedAttachments
                    every { support.submissionAttachmentFileIds(updatedAttachments) } returns listOf(newFileId)
                    every { support.validateSubmissionAttachmentFiles(listOf(newFileId)) } just runs
                    every { support.saveSubmission(submission) } returns submission
                    every { support.updateFileStatuses(listOf(newFileId), UserFileStatus.LINKED) } just runs
                    every { support.downgradeOrphanedFiles(listOf(oldFileId)) } just runs
                    every { support.toSubmissionDto(submission, groupId) } returns dto

                    val result = updateSubmissionUseCase.updateSubmission(
                        memberInfo = memberInfo,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        command = UpdateTaskSubmissionCommand(
                            title = "새 제목",
                            content = "새 본문",
                            attachments = listOf(
                                TaskSubmissionAttachmentCommand(
                                    type = TaskSubmitAttachmentType.FILE,
                                    title = "새 파일",
                                    fileId = newFileId.toHexString()
                                )
                            )
                        )
                    )

                    result shouldBe dto
                    verify(exactly = 1) { support.saveSubmission(submission) }
                    verify(exactly = 1) { support.updateFileStatuses(listOf(newFileId), UserFileStatus.LINKED) }
                    verify(exactly = 1) { support.downgradeOrphanedFiles(listOf(oldFileId)) }
                }
            }

            When("출력형 과제를 철회하면") {
                Then("기존처럼 submission 삭제와 파일 강등 처리를 수행한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val fileId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(taskId = taskId, submissionType = TaskSubmissionType.OUTPUT)
                    val assignee = TaskAssignee(
                        taskId = taskId,
                        memberId = memberId,
                        status = TaskAssigneeStatus.SUBMITTED
                    )
                    val submission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = memberId,
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = listOf(
                            TaskSubmitAttachment(
                                type = TaskSubmitAttachmentType.FILE,
                                title = "첨부 파일",
                                fileId = fileId
                            )
                        )
                    )

                    every { support.requireGroupMember(groupId, memberId) } returns mockk()
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.checkSubmissionUpdatable(task) } just runs
                    every { support.requireTaskAssignee(taskId, memberId) } returns assignee
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.submissionAttachmentFileIds(submission.attachments) } returns listOf(fileId)
                    every { support.removeCommentsBySubmission(submissionId) } just runs
                    every { support.removeSubmissionById(submissionId) } just runs
                    every { support.saveAssignee(any()) } answers { firstArg() }
                    every { support.downgradeOrphanedFiles(listOf(fileId)) } just runs

                    withdrawSubmissionUseCase.withdrawSubmission(
                        memberInfo = memberInfo,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString()
                    )

                    verify(exactly = 1) { support.removeCommentsBySubmission(submissionId) }
                    verify(exactly = 1) { support.removeSubmissionById(submissionId) }
                    verify(exactly = 1) { support.saveAssignee(match { it.status == TaskAssigneeStatus.NOT_SUBMITTED }) }
                    verify(exactly = 1) { support.downgradeOrphanedFiles(listOf(fileId)) }
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

    private fun createStudyGroupMember(groupId: ObjectId, memberId: ObjectId): StudyGroupMember {
        return StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "tester",
            profileImage = ProfileImageVo(
                type = ProfileImageType.PRESET,
                url = "https://example.com/profile.png"
            )
        )
    }

    private fun createTask(taskId: ObjectId, submissionType: TaskSubmissionType): Task {
        return Task(
            id = taskId,
            relatedScheduleId = ObjectId.get(),
            type = TaskType.POST,
            title = "과제 제목",
            description = "과제 설명",
            attachments = emptyList(),
            expireAt = LocalDateTime.now().plusDays(1),
            submissionType = submissionType
        )
    }

    private fun createSubmissionDto(submission: TaskSubmission, fileId: ObjectId): TaskSubmissionDto {
        return TaskSubmissionDto(
            submissionId = submission.identifier,
            taskId = submission.taskId.toHexString(),
            memberId = submission.memberId.toHexString(),
            memberNickname = "tester",
            memberProfileImageUrl = "https://example.com/profile.png",
            memberProfileImageType = ProfileImageType.PRESET,
            title = submission.title,
            content = submission.content,
            attachments = listOf(
                TaskSubmissionAttachmentDto(
                    type = TaskSubmitAttachmentType.FILE,
                    title = submission.attachments.first().title,
                    url = null,
                    fileId = fileId.toHexString(),
                    fileUrl = "https://cdn.example.com/file.pdf",
                    originalFileName = "file.pdf"
                )
            ),
            createdAt = submission.createdAt
        )
    }
}
