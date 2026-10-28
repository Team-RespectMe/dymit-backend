package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleQueryPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskExpireAtNormalizer
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.CreateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.TaskFilePort
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskAssigneeRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskSubmissionCommentRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskSubmissionRepository
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant

internal class TaskCreationTimeRegressionTest : BehaviorSpec() {

    private val now = Instant.parse("2026-06-11T16:00:00Z")

    init {
        beforeEach {
            mockkObject(TaskExpireAtNormalizer)
            every { TaskExpireAtNormalizer.currentUtcDateTime() } returns now
            every { TaskExpireAtNormalizer.toKst(any()) } answers { callOriginal() }
            every { TaskExpireAtNormalizer.normalizePostExpireAt(any()) } answers { callOriginal() }
        }

        afterEach {
            unmockkObject(TaskExpireAtNormalizer)
            clearAllMocks()
        }

        Given("과제 생성 시 일정 시작까지 남은 시간이 제한 기준과 가까우면") {
            listOf(
                "12시간" to 12L * 60L * 60L,
                "1초" to 1L,
                "정확히 24시간" to 24L * 60L * 60L,
                "24시간 초과" to 25L * 60L * 60L
            ).forEach { (remaining, seconds) ->
                When("일정이 $remaining 남아 있으면") {
                    Then("사전 과제를 생성하고 마감일을 일정 시작 시각으로 정규화한다") {
                        val created = createTaskAndCapture(
                            scheduleAt = now.plusSeconds(seconds),
                            requestedExpireAt = now.plusSeconds(5L * 86400L)
                        )

                        created.type shouldBe TaskType.PRE
                        created.expireAt shouldBe now.plusSeconds(seconds)
                    }
                }
            }
        }

        Given("일정 시작 시각부터는") {
            listOf(
                "시작 시각과 같을 때" to now,
                "시작 시각이 지난 뒤" to now.minusSeconds(1L)
            ).forEach { (timing, scheduleAt) ->
                When("$timing 과제를 생성하면") {
                    Then("사후 과제를 저장하며 과거 요청 마감일도 허용하고 KST 날짜 끝으로 정규화한다") {
                        val created = createTaskAndCapture(
                            scheduleAt = scheduleAt,
                            requestedExpireAt = Instant.parse("2026-06-11T08:00:00Z")
                        )

                        created.type shouldBe TaskType.POST
                        created.expireAt shouldBe Instant.parse("2026-06-11T14:59:59Z")
                    }
                }
            }
        }
    }

    private fun createTaskAndCapture(scheduleAt: Instant, requestedExpireAt: Instant): Task {
        val ownerId = ObjectId.get()
        val groupId = ObjectId.get()
        val scheduleId = ObjectId.get()
        val group = StudyGroupDto(id = groupId, ownerId = ownerId)
        val schedule = StudyScheduleServerDto(id = scheduleId, groupId = groupId, scheduleAt = scheduleAt)

        val groupQueryPort = mockk<StudyGroupQueryPort>()
        val groupMemberPort = mockk<StudyGroupMemberPort>(relaxed = true)
        val scheduleQueryPort = mockk<StudyScheduleQueryPort>(relaxed = true)
        val taskRepository = mockk<TaskRepository>()
        val taskAssigneeRepository = mockk<TaskAssigneeRepository>(relaxed = true)
        val taskSubmissionRepository = mockk<TaskSubmissionRepository>(relaxed = true)
        val taskSubmissionCommentRepository = mockk<TaskSubmissionCommentRepository>(relaxed = true)
        val taskFilePort = mockk<TaskFilePort>(relaxed = true)
        val savedTask = slot<Task>()

        every { groupQueryPort.loadByGroupId(groupId.toHexString()) } returns group
        every { scheduleQueryPort.loadById(scheduleId) } returns schedule
        every { scheduleQueryPort.getParticipantMemberIds(scheduleId) } returns emptyList()
        every { taskRepository.save(capture(savedTask)) } answers {
            val task = firstArg<Task>()
            Task(
                relatedScheduleId = task.relatedScheduleId,
                type = task.type,
                title = task.title,
                description = task.description,
                attachments = task.attachments,
                expireAt = task.expireAt,
                id = ObjectId.get(),
                submissionType = task.submissionType
            )
        }

        val support = TaskServiceSupport(
            loadStudyGroupPort = groupQueryPort,
            groupMemberRepository = groupMemberPort,
            studyScheduleQueryPort = scheduleQueryPort,
            taskRepository = taskRepository,
            taskAssigneeRepository = taskAssigneeRepository,
            taskSubmissionRepository = taskSubmissionRepository,
            taskSubmissionCommentRepository = taskSubmissionCommentRepository,
            taskFilePort = taskFilePort
        )
        val useCase = CreateTaskUseCaseImpl(support, mockk<ApplicationEventPublisher>(relaxed = true))
        val memberInfo = MemberInfo(ownerId.toHexString(), "tester", listOf(MemberRole.ROLE_MEMBER.name))
        val command = CreateTaskCommand(
            relatedScheduleId = scheduleId.toHexString(),
            title = "과제 제목",
            description = "과제 설명",
            attachmentFileIds = emptyList(),
            expireAt = requestedExpireAt
        )

        useCase.execute(CreateTaskInput(memberInfo, groupId.toHexString(), command))
        return savedTask.captured
    }
}
