package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskProfileImageType as ProfileImageType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskSubmissionCreatedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskSubmissionEventPublicationTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val taskSubmissionRepository = mockk<TaskSubmissionRepository>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val useCase = CreateSubmissionUseCaseImpl(support, taskSubmissionRepository, eventPublisher)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 제출 생성 유즈케이스") {
            When("제출을 정상 생성하면") {
                Then("TaskSubmissionCreatedEvent를 정확히 한 번 발행한다") {
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val memberInfo = MemberInfo(
                        memberId = memberId.toHexString(),
                        nickname = "제출자",
                        roles = listOf(MemberRole.ROLE_MEMBER.name)
                    )
                    val group = StudyGroup(
                        id = groupId,
                        ownerId = ObjectId.get(),
                        name = "알고리즘 스터디",
                        description = "설명"
                    )
                    val member = StudyGroupMember(
                        groupId = groupId,
                        memberId = memberId,
                        nickname = "제출자"
                    )
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = scheduleId,
                        type = TaskType.PRE,
                        title = "주차 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2)
                    )
                    val assignee = TaskAssignee(taskId = taskId, memberId = memberId)
                    val savedSubmission = TaskSubmission(
                        id = submissionId,
                        taskId = taskId,
                        memberId = memberId,
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = emptyList()
                    )
                    val expectedDto = TaskSubmissionDto(
                        submissionId = savedSubmission.identifier,
                        taskId = taskId.toHexString(),
                        memberId = memberId.toHexString(),
                        memberNickname = "제출자",
                        memberProfileImageUrl = "",
                        memberProfileImageType = ProfileImageType.PRESET,
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = emptyList(),
                        createdAt = savedSubmission.createdAt
                    )
                    val command = CreateTaskSubmissionCommand(
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = emptyList()
                    )
                    val eventSlot = slot<Any>()

                    every { support.requireGroupMember(groupId, memberId) } returns member
                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.checkTaskInGroup(task, groupId) } answers { Unit }
                    every { support.checkTaskActionAllowedBySchedule(task) } answers { Unit }
                    every { support.checkSubmissionUpdatable(task) } answers { Unit }
                    every { support.requireTaskAssignee(taskId, memberId) } returns assignee
                    every { taskSubmissionRepository.findByTaskIdAndMemberId(taskId, memberId) } returns null
                    every { support.toSubmissionAttachments(emptyList()) } returns emptyList()
                    every { support.submissionAttachmentFileIds(emptyList()) } returns emptyList()
                    every { support.validateSubmissionAttachmentFiles(emptyList()) } answers { Unit }
                    every { support.saveSubmission(any()) } returns savedSubmission
                    every { support.saveAssignee(assignee) } returns assignee
                    every { support.updateFileStatuses(emptyList(), TaskFileStatusDto.LINKED) } answers { Unit }
                    every { support.loadGroup(groupId.toHexString()) } returns group
                    every { support.toSubmissionDto(savedSubmission, groupId) } returns expectedDto
                    justRun { eventPublisher.publishEvent(any()) }

                    val result = useCase.createSubmission(
                        memberInfo = memberInfo,
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        command = command
                    )

                    verify(exactly = 1) { eventPublisher.publishEvent(capture(eventSlot)) }
                    val event = eventSlot.captured as TaskSubmissionCreatedEvent
                    event.taskId shouldBe taskId
                    event.groupId shouldBe groupId
                    event.scheduleId shouldBe scheduleId
                    event.task shouldBe task
                    event.group shouldBe group
                    event.member shouldBe member
                    result shouldBe expectedDto
                }
            }
        }
    }
}
