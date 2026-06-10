package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateSubmissionCommentUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionComment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskSubmissionCommentCreatedEvent
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskSubmissionCommentEventPublicationTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val useCase = CreateSubmissionCommentUseCaseImpl(support, eventPublisher)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 제출 댓글 생성 유즈케이스") {
            When("다른 멤버의 제출물에 댓글을 생성하면") {
                Then("TaskSubmissionCommentCreatedEvent를 정확히 한 번 발행한다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val writerId = ObjectId.get()
                    val assigneeMemberId = ObjectId.get()
                    val memberInfo = MemberInfo(
                        memberId = writerId.toHexString(),
                        nickname = "댓글작성자",
                        roles = listOf(MemberRole.ROLE_MEMBER)
                    )
                    val group = StudyGroup(
                        id = groupId,
                        ownerId = ObjectId.get(),
                        name = "백엔드 스터디",
                        description = "설명"
                    )
                    val writer = StudyGroupMember(
                        groupId = groupId,
                        memberId = writerId,
                        nickname = "댓글작성자"
                    )
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.PRE,
                        title = "주차 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2)
                    )
                    val submission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = assigneeMemberId,
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = emptyList()
                    )
                    val savedComment = TaskSubmissionComment(
                        id = ObjectId.get(),
                        taskId = taskId,
                        submissionId = submissionId,
                        writerId = writerId,
                        content = "피드백"
                    )
                    val expectedDto = TaskSubmissionCommentDto(
                        commentId = savedComment.identifier,
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        writerId = writerId.toHexString(),
                        writerNickname = writer.nickname,
                        writerProfileImageUrl = "",
                        writerProfileImageType = ProfileImageType.PRESET,
                        content = "피드백",
                        createdAt = savedComment.createdAt
                    )
                    val eventSlot = slot<Any>()

                    every { support.requireGroupMember(groupId, writerId) } returns writer
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } answers { Unit }
                    every { support.requireTaskAssignee(taskId, writerId) } returns TaskAssignee(taskId, writerId)
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.saveComment(any()) } returns savedComment
                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.toCommentDto(savedComment, groupId) } returns expectedDto
                    justRun { eventPublisher.publishEvent(any()) }

                    val result = useCase.createSubmissionComment(
                        memberInfo = memberInfo,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        command = CreateTaskSubmissionCommentCommand(content = "피드백")
                    )

                    verify(exactly = 1) { eventPublisher.publishEvent(capture(eventSlot)) }
                    val event = eventSlot.captured as TaskSubmissionCommentCreatedEvent
                    event.taskId shouldBe taskId
                    event.groupId shouldBe groupId
                    event.submissionId shouldBe submissionId
                    event.assigneeMemberId shouldBe assigneeMemberId
                    event.task shouldBe task
                    event.group shouldBe group
                    event.member shouldBe writer
                    result shouldBe expectedDto
                }
            }

            When("자신의 제출물에 댓글을 생성하면") {
                Then("댓글 생성 이벤트는 발행하지 않는다") {
                    val groupId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val writerId = ObjectId.get()
                    val memberInfo = MemberInfo(
                        memberId = writerId.toHexString(),
                        nickname = "댓글작성자",
                        roles = listOf(MemberRole.ROLE_MEMBER)
                    )
                    val writer = StudyGroupMember(
                        groupId = groupId,
                        memberId = writerId,
                        nickname = "댓글작성자"
                    )
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.PRE,
                        title = "주차 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2)
                    )
                    val submission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = writerId,
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = emptyList()
                    )
                    val savedComment = TaskSubmissionComment(
                        id = ObjectId.get(),
                        taskId = taskId,
                        submissionId = submissionId,
                        writerId = writerId,
                        content = "메모"
                    )
                    val expectedDto = TaskSubmissionCommentDto(
                        commentId = savedComment.identifier,
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        writerId = writerId.toHexString(),
                        writerNickname = writer.nickname,
                        writerProfileImageUrl = "",
                        writerProfileImageType = ProfileImageType.PRESET,
                        content = "메모",
                        createdAt = savedComment.createdAt
                    )

                    every { support.requireGroupMember(groupId, writerId) } returns writer
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } answers { Unit }
                    every { support.requireTaskAssignee(taskId, writerId) } returns TaskAssignee(taskId, writerId)
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.saveComment(any()) } returns savedComment
                    every { support.toCommentDto(savedComment, groupId) } returns expectedDto
                    justRun { eventPublisher.publishEvent(any()) }

                    val result = useCase.createSubmissionComment(
                        memberInfo = memberInfo,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        submissionId = submissionId.toHexString(),
                        command = CreateTaskSubmissionCommentCommand(content = "메모")
                    )

                    verify(exactly = 0) { eventPublisher.publishEvent(any<TaskSubmissionCommentCreatedEvent>()) }
                    result shouldBe expectedDto
                }
            }
        }
    }
}
