package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionAttachmentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.GetTaskSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskProfileImageType as ProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmitAttachment
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId
import java.time.Instant

internal class GetTaskSubmissionUseCaseImplTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>()
    private val useCase = GetTaskSubmissionUseCaseImpl(support)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 제출 단건 조회 요청이 주어지면") {
            When("요청 회원과 대상 회원이 같은 그룹의 멤버이고 제출이 존재하면") {
                Then("taskId와 memberId 기준으로 제출을 조회해 DTO를 반환한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val requesterMemberId = ObjectId.get()
                    val assigneeMemberId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val requester = createMemberInfo(
                        createMemberEntity(
                            id = requesterMemberId,
                            nickname = "requester"
                        )
                    )
                    val task = Task(
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.POST,
                        title = "과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = Instant.now().plusSeconds(2L * 86400L),
                        id = taskId
                    )
                    val submission = TaskSubmission(
                        taskId = taskId,
                        memberId = assigneeMemberId,
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = listOf(
                            TaskSubmitAttachment(
                                type = TaskSubmitAttachmentType.FILE,
                                title = "첨부",
                                fileId = ObjectId.get()
                            )
                        ),
                        id = submissionId,
                        createdAt = Instant.now()
                    )
                    val dto = TaskSubmissionDto(
                        submissionId = submissionId.toHexString(),
                        taskId = taskId.toHexString(),
                        memberId = assigneeMemberId.toHexString(),
                        memberNickname = "submitter",
                        memberProfileImageUrl = "https://example.com/profile.png",
                        memberProfileImageType = ProfileImageType.PRESET,
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = listOf(
                            TaskSubmissionAttachmentDto(
                                type = TaskSubmitAttachmentType.FILE,
                                title = "첨부",
                                url = null,
                                fileId = ObjectId.get().toHexString(),
                                fileUrl = "https://cdn.example.com/attachment.pdf",
                                originalFileName = "attachment.pdf"
                            )
                        ),
                        createdAt = submission.createdAt
                    )
                    val requesterMember = mockk<StudyGroupMember>()
                    val assigneeMember = mockk<StudyGroupMember>()

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.requireGroupMember(groupId, requesterMemberId) } returns requesterMember
                    every { support.requireGroupMember(groupId, assigneeMemberId) } returns assigneeMember
                    every { support.loadSubmissionByTaskAndMember(taskId, assigneeMemberId) } returns submission
                    every { support.toSubmissionDto(submission, groupId) } returns dto

                    val result = useCase.getTaskSubmission(
                        memberInfo = requester,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        memberId = assigneeMemberId.toHexString()
                    )

                    verify(exactly = 1) { support.loadTask(taskId.toHexString()) }
                    verify(exactly = 1) { support.checkTaskInGroup(task, groupId) }
                    verify(exactly = 1) { support.requireGroupMember(groupId, requesterMemberId) }
                    verify(exactly = 1) { support.requireGroupMember(groupId, assigneeMemberId) }
                    verify(exactly = 1) { support.loadSubmissionByTaskAndMember(taskId, assigneeMemberId) }
                    verify(exactly = 1) { support.toSubmissionDto(submission, groupId) }
                    result shouldBe dto
                }
            }

            When("taskId와 memberId로 제출을 찾지 못하면") {
                Then("NotFoundException이 전파된다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val requesterMemberId = ObjectId.get()
                    val assigneeMemberId = ObjectId.get()
                    val requester = createMemberInfo(
                        createMemberEntity(
                            id = requesterMemberId,
                            nickname = "requester"
                        )
                    )
                    val task = Task(
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.POST,
                        title = "과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = Instant.now().plusSeconds(2L * 86400L),
                        id = taskId
                    )

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.requireGroupMember(groupId, requesterMemberId) } returns mockk<StudyGroupMember>()
                    every { support.requireGroupMember(groupId, assigneeMemberId) } returns mockk<StudyGroupMember>()
                    every { support.loadSubmissionByTaskAndMember(taskId, assigneeMemberId) } throws
                        NotFoundException(message = "존재하지 않는 제출입니다.")

                    shouldThrow<NotFoundException> {
                        useCase.getTaskSubmission(
                            memberInfo = requester,
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            memberId = assigneeMemberId.toHexString()
                        )
                    }

                    verify(exactly = 1) { support.loadTask(taskId.toHexString()) }
                    verify(exactly = 1) { support.checkTaskInGroup(task, groupId) }
                    verify(exactly = 1) { support.requireGroupMember(groupId, requesterMemberId) }
                    verify(exactly = 1) { support.requireGroupMember(groupId, assigneeMemberId) }
                    verify(exactly = 1) { support.loadSubmissionByTaskAndMember(taskId, assigneeMemberId) }
                    verify(exactly = 0) { support.toSubmissionDto(any(), any()) }
                }
            }

            When("체크형 과제의 대상자가 제출 상태이면") {
                Then("실제 TaskSubmission 조회 없이 synthetic DTO를 반환한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val requesterMemberId = ObjectId.get()
                    val assigneeMemberId = ObjectId.get()
                    val requester = createMemberInfo(
                        createMemberEntity(
                            id = requesterMemberId,
                            nickname = "requester"
                        )
                    )
                    val task = Task(
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.POST,
                        title = "체크 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = Instant.now().plusSeconds(2L * 86400L),
                        id = taskId,
                        submissionType = TaskSubmissionType.CHECK
                    )
                    val assigneeMember = StudyGroupMember(
                        groupId = groupId,
                        memberId = assigneeMemberId,
                        nickname = "submitter"
                    )
                    val assignee = TaskAssignee(
                        id = ObjectId.get(),
                        taskId = taskId,
                        memberId = assigneeMemberId,
                        status = TaskAssigneeStatus.SUBMITTED
                    )

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.requireGroupMember(groupId, requesterMemberId) } returns mockk()
                    every { support.requireGroupMember(groupId, assigneeMemberId) } returns assigneeMember
                    every { support.requireTaskAssignee(taskId, assigneeMemberId) } returns assignee

                    val result = useCase.getTaskSubmission(
                        memberInfo = requester,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        memberId = assigneeMemberId.toHexString()
                    )

                    verify(exactly = 1) { support.requireTaskAssignee(taskId, assigneeMemberId) }
                    verify(exactly = 0) { support.loadSubmissionByTaskAndMember(any(), any()) }
                    result.submissionId shouldBe assignee.identifier
                    result.taskId shouldBe taskId.toHexString()
                    result.memberId shouldBe assigneeMemberId.toHexString()
                    result.title shouldBe ""
                    result.content shouldBe ""
                }
            }

            When("체크형 과제의 대상자가 미제출 상태이면") {
                Then("실제 TaskSubmission 조회 없이 NotFoundException이 발생한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val requesterMemberId = ObjectId.get()
                    val assigneeMemberId = ObjectId.get()
                    val requester = createMemberInfo(
                        createMemberEntity(
                            id = requesterMemberId,
                            nickname = "requester"
                        )
                    )
                    val task = Task(
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.POST,
                        title = "체크 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = Instant.now().plusSeconds(2L * 86400L),
                        id = taskId,
                        submissionType = TaskSubmissionType.CHECK
                    )
                    val assignee = TaskAssignee(
                        taskId = taskId,
                        memberId = assigneeMemberId,
                        status = TaskAssigneeStatus.NOT_SUBMITTED
                    )

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } just runs
                    every { support.requireGroupMember(groupId, requesterMemberId) } returns mockk()
                    every { support.requireGroupMember(groupId, assigneeMemberId) } returns mockk()
                    every { support.requireTaskAssignee(taskId, assigneeMemberId) } returns assignee

                    shouldThrow<NotFoundException> {
                        useCase.getTaskSubmission(
                            memberInfo = requester,
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            memberId = assigneeMemberId.toHexString()
                        )
                    }.message shouldBe "존재하지 않는 제출입니다."

                    verify(exactly = 1) { support.requireTaskAssignee(taskId, assigneeMemberId) }
                    verify(exactly = 0) { support.loadSubmissionByTaskAndMember(any(), any()) }
                    verify(exactly = 0) { support.toSubmissionDto(any(), any()) }
                }
            }
        }
    }
}
