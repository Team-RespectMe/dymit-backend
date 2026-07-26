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
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceImpl
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionComment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionCommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskServiceImplTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val taskSubmissionRepository = mockk<TaskSubmissionRepository>(relaxed = true)
    private val taskSubmissionCommentRepository = mockk<TaskSubmissionCommentRepository>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val service = TaskServiceImpl(
        support = support,
        taskSubmissionRepository = taskSubmissionRepository,
        taskSubmissionCommentRepository = taskSubmissionCommentRepository,
        eventPublisher = eventPublisher
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("제출 CUD 권한 검증") {
            When("대상자가 아닌 멤버가 제출 생성을 시도하면") {
                Then("ForbiddenException이 발생한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(taskId)
                    val command = CreateTaskSubmissionCommand(
                        title = "제출",
                        content = "본문",
                        attachments = emptyList()
                    )

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { taskSubmissionRepository.findByTaskIdAndMemberId(taskId, memberId) } returns null
                    every { support.requireTaskAssignee(taskId, memberId) } throws ForbiddenException("과제 대상자만 제출/댓글을 변경할 수 있습니다.")

                    shouldThrow<ForbiddenException> {
                        service.createSubmission(memberInfo, groupId.toHexString(), taskId.toHexString(), command)
                    }
                }
            }

            When("대상자가 아닌 멤버가 제출 수정을 시도하면") {
                Then("ForbiddenException이 발생한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(taskId)

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.requireTaskAssignee(taskId, memberId) } throws ForbiddenException("과제 대상자만 제출/댓글을 변경할 수 있습니다.")

                    shouldThrow<ForbiddenException> {
                        service.updateSubmission(
                            memberInfo = memberInfo,
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            submissionId = submissionId.toHexString(),
                            command = UpdateTaskSubmissionCommand("제목", "본문", emptyList())
                        )
                    }
                }
            }

            When("대상자가 아닌 멤버가 제출 철회를 시도하면") {
                Then("ForbiddenException이 발생한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val task = createTask(taskId)

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.requireTaskAssignee(taskId, memberId) } throws ForbiddenException("과제 대상자만 제출/댓글을 변경할 수 있습니다.")

                    shouldThrow<ForbiddenException> {
                        service.withdrawSubmission(
                            memberInfo = memberInfo,
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            submissionId = submissionId.toHexString()
                        )
                    }
                }
            }
        }

        Given("제출 마감 시각 이후 CUD 제한") {
            When("마감된 과제에 제출 생성을 시도하면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val memberInfo = createMemberInfo(ObjectId.get())
                    val task = createTask(taskId)

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkSubmissionUpdatable(task) } throws BadRequestException(message = "마감된 과제는 제출/수정/철회할 수 없습니다.")

                    shouldThrow<BadRequestException> {
                        service.createSubmission(
                            memberInfo,
                            groupId.toHexString(),
                            taskId.toHexString(),
                            CreateTaskSubmissionCommand("제목", "본문", emptyList())
                        )
                    }
                }
            }

            When("마감된 과제에 제출 수정을 시도하면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberInfo = createMemberInfo(ObjectId.get())
                    val task = createTask(taskId)

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkSubmissionUpdatable(task) } throws BadRequestException(message = "마감된 과제는 제출/수정/철회할 수 없습니다.")

                    shouldThrow<BadRequestException> {
                        service.updateSubmission(
                            memberInfo,
                            groupId.toHexString(),
                            taskId.toHexString(),
                            submissionId.toHexString(),
                            UpdateTaskSubmissionCommand("제목", "본문", emptyList())
                        )
                    }
                }
            }

            When("마감된 과제에 제출 철회를 시도하면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberInfo = createMemberInfo(ObjectId.get())
                    val task = createTask(taskId)

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkSubmissionUpdatable(task) } throws BadRequestException(message = "마감된 과제는 제출/수정/철회할 수 없습니다.")

                    shouldThrow<BadRequestException> {
                        service.withdrawSubmission(
                            memberInfo,
                            groupId.toHexString(),
                            taskId.toHexString(),
                            submissionId.toHexString()
                        )
                    }
                }
            }
        }

        Given("제출 철회 처리") {
            When("본인 제출 과제를 철회하면") {
                Then("댓글/제출을 물리 삭제하고 첨부 파일 강등 처리를 수행한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val fileId = ObjectId.get()
                    val task = createTask(taskId)
                    val assignee = TaskAssignee(
                        taskId = taskId,
                        memberId = memberId,
                        status = TaskAssigneeStatus.SUBMITTED
                    )
                    val submission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = memberId,
                        title = "제출",
                        content = "본문",
                        attachments = listOf(
                            TaskSubmitAttachment(
                                type = TaskSubmitAttachmentType.FILE,
                                title = "첨부",
                                fileId = fileId
                            )
                        )
                    )

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.requireTaskAssignee(taskId, memberId) } returns assignee
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.submissionAttachmentFileIds(submission.attachments) } returns listOf(fileId)
                    every { support.removeCommentsBySubmission(submissionId) } just runs
                    every { support.removeSubmissionById(submissionId) } just runs
                    every { support.saveAssignee(any()) } answers { firstArg() }
                    every { support.downgradeOrphanedFiles(listOf(fileId)) } just runs

                    service.withdrawSubmission(memberInfo, groupId.toHexString(), taskId.toHexString(), submissionId.toHexString())

                    verify(exactly = 1) { support.removeCommentsBySubmission(submissionId) }
                    verify(exactly = 1) { support.removeSubmissionById(submissionId) }
                    verify(exactly = 1) { support.downgradeOrphanedFiles(listOf(fileId)) }
                    verify(exactly = 1) { support.saveAssignee(match { it.status == TaskAssigneeStatus.NOT_SUBMITTED }) }
                }
            }
        }

        Given("댓글 권한 검증") {
            When("대상자가 아닌 멤버가 댓글 생성을 시도하면") {
                Then("그룹 멤버라면 댓글을 생성하고 대상자 검증을 호출하지 않는다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val assigneeMemberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val member = mockk<StudyGroupMember>(relaxed = true)
                    val task = createTask(taskId)
                    val submission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = assigneeMemberId,
                        title = "제출",
                        content = "본문",
                        attachments = emptyList()
                    )
                    val savedComment = TaskSubmissionComment(
                        id = ObjectId.get(),
                        taskId = taskId,
                        submissionId = submissionId,
                        writerId = memberId,
                        content = "댓글"
                    )
                    val commentDto = TaskSubmissionCommentDto(
                        commentId = savedComment.identifier,
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        writerId = memberId.toHexString(),
                        writerNickname = "tester",
                        writerProfileImageUrl = "https://example.com/profile.png",
                        writerProfileImageType = ProfileImageType.PRESET,
                        content = "댓글",
                        createdAt = savedComment.createdAt
                    )

                    every { support.requireGroupMember(groupId, memberId) } returns member
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.saveComment(any()) } returns savedComment
                    every { support.toCommentDto(savedComment, groupId) } returns commentDto

                    val result = service.createSubmissionComment(
                        memberInfo,
                        groupId.toHexString(),
                        taskId.toHexString(),
                        submissionId.toHexString(),
                        CreateTaskSubmissionCommentCommand("댓글")
                    )

                    result shouldBe commentDto
                    verify(exactly = 0) { support.requireTaskAssignee(any(), any()) }
                }
            }

            When("대상자가 아닌 멤버가 댓글 수정을 시도하면") {
                Then("본인 댓글이면 수정하고 대상자 검증을 호출하지 않는다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val commentId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val member = mockk<StudyGroupMember>(relaxed = true)
                    val task = createTask(taskId)
                    val comment = TaskSubmissionComment(
                        id = commentId,
                        taskId = taskId,
                        submissionId = submissionId,
                        writerId = memberId,
                        content = "댓글"
                    )
                    val commentDto = TaskSubmissionCommentDto(
                        commentId = comment.identifier,
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        writerId = memberId.toHexString(),
                        writerNickname = "tester",
                        writerProfileImageUrl = "https://example.com/profile.png",
                        writerProfileImageType = ProfileImageType.PRESET,
                        content = "수정",
                        createdAt = comment.createdAt
                    )

                    every { support.requireGroupMember(groupId, memberId) } returns member
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.loadComment(commentId.toHexString()) } returns comment
                    every { support.saveComment(comment) } returns comment
                    every { support.toCommentDto(comment, groupId) } returns commentDto

                    val result = service.updateSubmissionComment(
                        memberInfo,
                        groupId.toHexString(),
                        taskId.toHexString(),
                        submissionId.toHexString(),
                        commentId.toHexString(),
                        UpdateTaskSubmissionCommentCommand("수정")
                    )

                    result shouldBe commentDto
                    verify(exactly = 0) { support.requireTaskAssignee(any(), any()) }
                }
            }

            When("대상자가 아닌 멤버가 댓글 삭제를 시도하면") {
                Then("본인 댓글이면 삭제하고 대상자 검증을 호출하지 않는다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val commentId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = createMemberInfo(memberId)
                    val member = mockk<StudyGroupMember>(relaxed = true)
                    val task = createTask(taskId)
                    val comment = TaskSubmissionComment(
                        id = commentId,
                        taskId = taskId,
                        submissionId = submissionId,
                        writerId = memberId,
                        content = "댓글"
                    )

                    every { support.requireGroupMember(groupId, memberId) } returns member
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.loadComment(commentId.toHexString()) } returns comment
                    every { support.deleteComment(commentId) } just runs

                    service.deleteSubmissionComment(
                        memberInfo,
                        groupId.toHexString(),
                        taskId.toHexString(),
                        submissionId.toHexString(),
                        commentId.toHexString()
                    )

                    verify(exactly = 1) { support.deleteComment(commentId) }
                    verify(exactly = 0) { support.requireTaskAssignee(any(), any()) }
                }
            }
        }

        Given("댓글 조회 권한") {
            When("그룹 멤버가 댓글을 조회하면") {
                Then("대상자가 아니어도 조회 가능하다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val writerId = ObjectId.get()
                    val memberInfo = createMemberInfo(ObjectId.get())
                    val task = createTask(taskId)
                    val submission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = writerId,
                        title = "제출",
                        content = "본문",
                        attachments = emptyList()
                    )
                    val comment = TaskSubmissionComment(
                        id = ObjectId.get(),
                        taskId = taskId,
                        submissionId = submissionId,
                        writerId = writerId,
                        content = "댓글"
                    )
                    val commentDto = TaskSubmissionCommentDto(
                        commentId = comment.identifier,
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        writerId = writerId.toHexString(),
                        writerNickname = "writer",
                        writerProfileImageUrl = "https://example.com/profile.png",
                        writerProfileImageType = ProfileImageType.PRESET,
                        content = "댓글",
                        createdAt = LocalDateTime.now()
                    )

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.loadCommentsBySubmission(submissionId) } returns listOf(comment)
                    every { support.toCommentDto(comment, groupId) } returns commentDto

                    val result = service.getSubmissionComments(
                        memberInfo,
                        groupId.toHexString(),
                        taskId.toHexString(),
                        submissionId.toHexString()
                    )

                    result.size shouldBe 1
                    result[0].commentId shouldBe comment.identifier
                    verify(exactly = 0) { support.requireTaskAssignee(any(), any()) }
                }
            }
        }

        Given("요청 ID 형식 검증") {
            When("groupId가 ObjectId 형식이 아니면") {
                Then("BadRequestException이 발생한다") {
                    val memberInfo = createMemberInfo(ObjectId.get())

                    val exception = shouldThrow<BadRequestException> {
                        service.getGroupTasks(memberInfo, "invalid-group-id")
                    }

                    exception.message shouldBe "groupId 형식이 올바르지 않습니다."
                    verify(exactly = 0) { support.requireGroupMember(any(), any()) }
                }
            }

            When("memberId가 ObjectId 형식이 아니면") {
                Then("BadRequestException이 발생한다") {
                    val memberInfo = MemberInfo(
                        memberId = "invalid-member-id",
                        nickname = "tester",
                        roles = listOf(MemberRole.ROLE_MEMBER.name)
                    )

                    val exception = shouldThrow<BadRequestException> {
                        service.getGroupTasks(memberInfo, ObjectId.get().toHexString())
                    }

                    exception.message shouldBe "memberId 형식이 올바르지 않습니다."
                    verify(exactly = 0) { support.requireGroupMember(any(), any()) }
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

    private fun createTask(taskId: ObjectId): Task {
        return Task(
            id = taskId,
            relatedScheduleId = ObjectId.get(),
            type = TaskType.PRE,
            title = "과제",
            description = "설명",
            attachments = emptyList(),
            expireAt = LocalDateTime.now().plusDays(2)
        )
    }
}
