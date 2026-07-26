package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.TaskExpireAtNormalizer
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.CreateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.UpdateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.WithdrawSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskProfileImageType as ProfileImageType
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto as StudySchedule
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.TaskFilePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleQueryPort
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskAssigneeRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskSubmissionCommentRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskSubmissionRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskDeadlineTimezonePolicyTest : BehaviorSpec() {

    private val loadStudyGroupPort = mockk<StudyGroupQueryPort>()
    private val groupMemberRepository = mockk<StudyGroupMemberPort>()
    private val studyScheduleQueryPort = mockk<StudyScheduleQueryPort>()
    private val taskRepository = mockk<TaskRepository>()
    private val taskAssigneeRepository = mockk<TaskAssigneeRepository>()
    private val taskSubmissionRepository = mockk<TaskSubmissionRepository>()
    private val taskSubmissionCommentRepository = mockk<TaskSubmissionCommentRepository>()
    private val taskFilePort = mockk<TaskFilePort>()

    private val support = spyk(
        TaskServiceSupport(
            loadStudyGroupPort = loadStudyGroupPort,
            groupMemberRepository = groupMemberRepository,
            studyScheduleQueryPort = studyScheduleQueryPort,
            taskRepository = taskRepository,
            taskAssigneeRepository = taskAssigneeRepository,
            taskSubmissionRepository = taskSubmissionRepository,
            taskSubmissionCommentRepository = taskSubmissionCommentRepository,
            taskFilePort = taskFilePort
        )
    )
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val createSubmissionUseCase = CreateSubmissionUseCaseImpl(support, taskSubmissionRepository, eventPublisher)
    private val updateSubmissionUseCase = UpdateSubmissionUseCaseImpl(support)
    private val withdrawSubmissionUseCase = WithdrawSubmissionUseCaseImpl(support)
    private var taskExpireAtNormalizerMocked = false

    init {
        afterEach {
            if (taskExpireAtNormalizerMocked) {
                unmockkObject(TaskExpireAtNormalizer)
                taskExpireAtNormalizerMocked = false
            }
            clearAllMocks()
        }

        Given("TASK-68 마감 시각 정책") {
            When("KST 날짜 입력을 POST 마감 시각으로 정규화하면") {
                Then("해당 날짜의 23:59:59 KST를 UTC LocalDateTime으로 저장한다") {
                    val requestedExpireAt = LocalDateTime.of(2026, 6, 15, 8, 30, 0)

                    mockTaskExpireAtClock(LocalDateTime.of(2026, 6, 15, 14, 59, 58))

                    val normalized = TaskExpireAtNormalizer.normalizePostExpireAt(requestedExpireAt)

                    normalized shouldBe LocalDateTime.of(2026, 6, 15, 14, 59, 59)
                    TaskExpireAtNormalizer.toKst(normalized) shouldBe LocalDateTime.of(2026, 6, 15, 23, 59, 59)
                }
            }

            When("UTC로 저장된 오늘 마감 과제를 KST 23:59:59 이전에 조회하면") {
                Then("제출 생성이 가능하다") {
                    val groupId = ObjectId.get()
                    val ownerId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val task = createTask(taskId, scheduleId)
                    val group = createGroup(groupId, ownerId)
                    val member = createMember(groupId, memberId)
                    val assignee = createAssignee(taskId, memberId, TaskAssigneeStatus.NOT_SUBMITTED)
                    val savedSubmission = createSubmission(submissionId, taskId, memberId)
                    val command = CreateTaskSubmissionCommand("제목", "본문", emptyList())

                    mockTaskExpireAtClock(LocalDateTime.of(2026, 6, 15, 14, 59, 58))
                    stubTaskInGroup(task, groupId)
                    every { support.requireGroupMember(groupId, memberId) } returns member
                    every { support.requireTaskAssignee(taskId, memberId) } returns assignee
                    every { taskSubmissionRepository.findByTaskIdAndMemberId(taskId, memberId) } returns null
                    every { support.saveSubmission(any()) } returns savedSubmission
                    every { support.saveAssignee(any()) } answers { firstArg() }
                    every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns group
                    every { support.toSubmissionDto(savedSubmission, groupId) } returns createSubmissionDto(savedSubmission)

                    val result = createSubmissionUseCase.createSubmission(
                        memberInfo = createMemberInfo(memberId),
                        groupId = groupId.toHexString(),
                        taskId = taskId.toHexString(),
                        command = command
                    )

                    result.submissionId shouldBe submissionId.toHexString()
                    verify(exactly = 1) { support.saveSubmission(match { it.taskId == taskId && it.memberId == memberId }) }
                    verify(exactly = 1) { support.saveAssignee(match { it.status == TaskAssigneeStatus.SUBMITTED }) }
                }
            }

            When("UTC로 저장된 오늘 마감 과제를 KST 23:59:59 이전에 철회하면") {
                Then("제출 철회가 가능하다") {
                    val groupId = ObjectId.get()
                    val ownerId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val task = createTask(taskId, scheduleId)
                    val group = createGroup(groupId, ownerId)
                    val member = createMember(groupId, memberId)
                    val assignee = createAssignee(taskId, memberId, TaskAssigneeStatus.SUBMITTED)
                    val submission = createSubmission(submissionId, taskId, memberId)

                    mockTaskExpireAtClock(LocalDateTime.of(2026, 6, 15, 14, 59, 58))
                    stubTaskInGroup(task, groupId)
                    every { support.requireGroupMember(groupId, memberId) } returns member
                    every { support.requireTaskAssignee(taskId, memberId) } returns assignee
                    every { support.loadSubmission(submissionId.toHexString()) } returns submission
                    every { support.saveAssignee(any()) } answers { firstArg() }
                    every { support.removeCommentsBySubmission(submissionId) } returns 0L
                    every { support.removeSubmissionById(submissionId) } returns false
                    every { support.downgradeOrphanedFiles(emptyList()) } just runs

                    shouldNotThrowAny {
                        withdrawSubmissionUseCase.withdrawSubmission(
                            memberInfo = createMemberInfo(memberId),
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            submissionId = submissionId.toHexString()
                        )
                    }

                    verify(exactly = 1) { support.removeCommentsBySubmission(submissionId) }
                    verify(exactly = 1) { support.removeSubmissionById(submissionId) }
                    verify(exactly = 1) { support.saveAssignee(match { it.status == TaskAssigneeStatus.NOT_SUBMITTED }) }
                }
            }

            When("UTC로 저장된 오늘 마감 과제가 KST 23:59:59를 지난 뒤면") {
                Then("제출 생성이 차단된다") {
                    val groupId = ObjectId.get()
                    val ownerId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val task = createTask(taskId, scheduleId)

                    mockTaskExpireAtClock(LocalDateTime.of(2026, 6, 15, 15, 0, 0))
                    stubTaskInGroup(task, groupId)
                    every { support.requireGroupMember(groupId, memberId) } returns createMember(groupId, memberId)

                    shouldThrow<BadRequestException> {
                        createSubmissionUseCase.createSubmission(
                            memberInfo = createMemberInfo(memberId),
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            command = CreateTaskSubmissionCommand("제목", "본문", emptyList())
                        )
                    }.message shouldBe "마감된 과제는 제출/수정/철회할 수 없습니다."
                }
            }

            When("UTC로 저장된 오늘 마감 과제가 KST 23:59:59를 지난 뒤면") {
                Then("제출 수정이 차단된다") {
                    val groupId = ObjectId.get()
                    val ownerId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val task = createTask(taskId, scheduleId)

                    mockTaskExpireAtClock(LocalDateTime.of(2026, 6, 15, 15, 0, 0))
                    stubTaskInGroup(task, groupId)
                    every { support.requireGroupMember(groupId, memberId) } returns createMember(groupId, memberId)

                    shouldThrow<BadRequestException> {
                        updateSubmissionUseCase.updateSubmission(
                            memberInfo = createMemberInfo(memberId),
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            submissionId = submissionId.toHexString(),
                            command = UpdateTaskSubmissionCommand("제목", "본문", emptyList())
                        )
                    }.message shouldBe "마감된 과제는 제출/수정/철회할 수 없습니다."
                }
            }

            When("UTC로 저장된 오늘 마감 과제가 KST 23:59:59를 지난 뒤면") {
                Then("제출 철회가 차단된다") {
                    val groupId = ObjectId.get()
                    val ownerId = ObjectId.get()
                    val memberId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val submissionId = ObjectId.get()
                    val task = createTask(taskId, scheduleId)

                    mockTaskExpireAtClock(LocalDateTime.of(2026, 6, 15, 15, 0, 0))
                    stubTaskInGroup(task, groupId)
                    every { support.requireGroupMember(groupId, memberId) } returns createMember(groupId, memberId)

                    shouldThrow<BadRequestException> {
                        withdrawSubmissionUseCase.withdrawSubmission(
                            memberInfo = createMemberInfo(memberId),
                            groupId = groupId.toHexString(),
                            taskId = taskId.toHexString(),
                            submissionId = submissionId.toHexString()
                        )
                    }.message shouldBe "마감된 과제는 제출/수정/철회할 수 없습니다."
                }
            }
        }
    }

    private fun mockTaskExpireAtClock(currentUtcDateTime: LocalDateTime) {
        mockkObject(TaskExpireAtNormalizer)
        taskExpireAtNormalizerMocked = true
        every { TaskExpireAtNormalizer.currentUtcDateTime() } returns currentUtcDateTime
        every { TaskExpireAtNormalizer.isExpired(any()) } answers { callOriginal() }
        every { TaskExpireAtNormalizer.toKst(any()) } answers { callOriginal() }
        every { TaskExpireAtNormalizer.normalizePostExpireAt(any()) } answers { callOriginal() }
    }

    private fun stubTaskInGroup(task: Task, groupId: ObjectId) {
        every { taskRepository.findById(task.id!!) } returns task
        every { studyScheduleQueryPort.loadById(task.relatedScheduleId) } returns StudySchedule(
            id = task.relatedScheduleId,
            groupId = groupId,
            scheduleAt = LocalDateTime.of(2026, 6, 15, 0, 0, 0)
        )
    }

    private fun createMemberInfo(memberId: ObjectId): MemberInfo {
        return MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "tester",
            roles = listOf(MemberRole.ROLE_MEMBER.name)
        )
    }

    private fun createMember(groupId: ObjectId, memberId: ObjectId): StudyGroupMember {
        return StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "tester",
            profileImage = ProfileImageVo(StudyGroupProfileImageType.PRESET, "https://example.com/profile.png")
        )
    }

    private fun createGroup(groupId: ObjectId, ownerId: ObjectId): StudyGroup {
        return StudyGroup(
            id = groupId,
            ownerId = ownerId
        )
    }

    private fun createTask(taskId: ObjectId, scheduleId: ObjectId): Task {
        return Task(
            id = taskId,
            relatedScheduleId = scheduleId,
            type = TaskType.POST,
            title = "과제",
            description = "설명",
            attachments = emptyList(),
            expireAt = LocalDateTime.of(2026, 6, 15, 14, 59, 59)
        )
    }

    private fun createAssignee(taskId: ObjectId, memberId: ObjectId, status: TaskAssigneeStatus): TaskAssignee {
        return TaskAssignee(
            taskId = taskId,
            memberId = memberId,
            status = status
        )
    }

    private fun createSubmission(submissionId: ObjectId, taskId: ObjectId, memberId: ObjectId): TaskSubmission {
        return TaskSubmission(
            id = submissionId,
            taskId = taskId,
            memberId = memberId,
            title = "제출",
            content = "본문",
            attachments = emptyList()
        )
    }

    private fun createSubmissionDto(submission: TaskSubmission): TaskSubmissionDto {
        return TaskSubmissionDto(
            submissionId = submission.identifier,
            taskId = submission.taskId.toHexString(),
            memberId = submission.memberId.toHexString(),
            memberNickname = "tester",
            memberProfileImageUrl = "https://example.com/profile.png",
            memberProfileImageType = ProfileImageType.PRESET,
            title = submission.title,
            content = submission.content,
            attachments = emptyList(),
            createdAt = submission.createdAt
        )
    }
}
