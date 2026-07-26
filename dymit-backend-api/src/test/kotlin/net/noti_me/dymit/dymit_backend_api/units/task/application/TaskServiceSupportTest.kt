package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.unmockkObject
import net.noti_me.dymit.dymit_backend_api.task.application.TaskExpireAtNormalizer
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskProfileImageType as ProfileImageType
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto as StudySchedule
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.TaskFilePort
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleQueryPort
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskAssigneeRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskSubmissionCommentRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.TaskSubmissionRepository
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class TaskServiceSupportTest : BehaviorSpec() {

    private val loadStudyGroupPort = mockk<StudyGroupQueryPort>()
    private val groupMemberRepository = mockk<StudyGroupMemberPort>()
    private val studyScheduleQueryPort = mockk<StudyScheduleQueryPort>()
    private val taskRepository = mockk<TaskRepository>()
    private val taskAssigneeRepository = mockk<TaskAssigneeRepository>()
    private val taskSubmissionRepository = mockk<TaskSubmissionRepository>()
    private val taskSubmissionCommentRepository = mockk<TaskSubmissionCommentRepository>()
    private val taskFilePort = mockk<TaskFilePort>()

    private val support = TaskServiceSupport(
        loadStudyGroupPort = loadStudyGroupPort,
        groupMemberRepository = groupMemberRepository,
        studyScheduleQueryPort = studyScheduleQueryPort,
        taskRepository = taskRepository,
        taskAssigneeRepository = taskAssigneeRepository,
        taskSubmissionRepository = taskSubmissionRepository,
        taskSubmissionCommentRepository = taskSubmissionCommentRepository,
        taskFilePort = taskFilePort
    )
    private var taskExpireAtNormalizerMocked = false

    init {
        afterEach {
            if (taskExpireAtNormalizerMocked) {
                unmockkObject(TaskExpireAtNormalizer)
                taskExpireAtNormalizerMocked = false
            }
            clearAllMocks()
        }

        Given("TASK-64.2 과제 타입 자동 결정") {
            When("일정이 아직 시작 전이면") {
                Then("PRE 타입으로 결정된다") {
                    val requestedAt = LocalDateTime.of(2026, 6, 11, 10, 0, 0)
                    val upcomingSchedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = requestedAt.plusHours(1)
                    )

                    support.resolveTaskTypeBySchedule(upcomingSchedule, requestedAt) shouldBe TaskType.PRE
                }
            }

            When("일정 시작 시각과 요청 시각이 같으면") {
                Then("POST 타입으로 결정된다") {
                    val requestedAt = LocalDateTime.of(2026, 6, 11, 10, 0, 0)
                    val startedSchedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = requestedAt
                    )

                    support.resolveTaskTypeBySchedule(startedSchedule, requestedAt) shouldBe TaskType.POST
                }
            }

            When("일정이 이미 시작되었으면") {
                Then("POST 타입으로 결정된다") {
                    val requestedAt = LocalDateTime.of(2026, 6, 11, 10, 0, 0)
                    val startedSchedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = requestedAt.minusHours(1)
                    )

                    support.resolveTaskTypeBySchedule(startedSchedule, requestedAt) shouldBe TaskType.POST
                }
            }

            When("요청 시각이 일정 시작 24시간 전과 정확히 같으면") {
                Then("사전 과제 생성 검증을 통과한다") {
                    val schedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = LocalDateTime.of(2026, 6, 12, 10, 0, 0)
                    )

                    shouldNotThrowAny {
                        support.validatePreTaskCreatable(schedule, schedule.scheduleAt.minusHours(24))
                    }
                }
            }

            When("요청 시각이 일정 시작 24시간 전보다 늦으면") {
                Then("BadRequestException이 발생한다") {
                    val schedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = LocalDateTime.of(2026, 6, 12, 10, 0, 0)
                    )

                    val exception = shouldThrow<BadRequestException> {
                        support.validatePreTaskCreatable(schedule, schedule.scheduleAt.minusHours(24).plusMinutes(1))
                    }

                    exception.message shouldBe "사전 과제는 일정 시작 24시간 이전에만 생성할 수 있습니다."
                }
            }
        }

        Given("TASK-62 생성/수정 마감 시각 규칙") {
            When("PRE 생성 요청의 expireAt(KST)이 연관 일정 시간(UTC0)과 일치하면") {
                Then("정상적으로 일정 시간으로 변환된다") {
                    val scheduleAtUtc0 = LocalDateTime.of(2026, 6, 10, 0, 0, 0)
                    val schedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = scheduleAtUtc0
                    )

                    val normalized = support.normalizeExpireAtForCreate(
                        type = TaskType.PRE,
                        requestedExpireAt = LocalDateTime.of(2026, 6, 10, 9, 0, 0),
                        schedule = schedule
                    )

                    normalized shouldBe scheduleAtUtc0
                }
            }

            When("PRE 생성 요청의 expireAt이 연관 일정 시간과 달라도") {
                Then("요청 expireAt을 무시하고 연관 일정 시간을 사용한다") {
                    val schedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = LocalDateTime.of(2026, 6, 10, 0, 0, 0)
                    )

                    val normalized = support.normalizeExpireAtForCreate(
                        type = TaskType.PRE,
                        requestedExpireAt = LocalDateTime.of(2026, 6, 10, 9, 1, 0),
                        schedule = schedule
                    )

                    normalized shouldBe schedule.scheduleAt
                }
            }

            When("POST 생성 요청의 시간이 포함되어 오면") {
                Then("요청 날짜의 23:59:59(KST)를 UTC0로 정규화한다") {
                    val schedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = LocalDateTime.of(2026, 6, 1, 0, 0, 0)
                    )

                    val normalized = support.normalizeExpireAtForCreate(
                        type = TaskType.POST,
                        requestedExpireAt = LocalDateTime.of(2026, 6, 1, 8, 30, 0),
                        schedule = schedule
                    )

                    normalized shouldBe LocalDateTime.of(2026, 6, 1, 14, 59, 59)
                }
            }

            When("PRE 수정 요청이 들어오면") {
                Then("요청 expireAt을 무시하고 기존 마감일을 유지한다") {
                    val currentExpireAt = LocalDateTime.now().plusDays(3)

                    val normalized = support.normalizeExpireAtForUpdate(
                        type = TaskType.PRE,
                        requestedExpireAt = LocalDateTime.now().minusDays(1),
                        currentExpireAt = currentExpireAt
                    )

                    normalized shouldBe currentExpireAt
                }
            }

            When("POST 수정 요청의 시간이 포함되어 오면") {
                Then("요청 날짜의 23:59:59(KST)를 UTC0로 정규화한다") {
                    val normalized = support.normalizeExpireAtForUpdate(
                        type = TaskType.POST,
                        requestedExpireAt = LocalDateTime.of(2026, 7, 4, 10, 0, 0),
                        currentExpireAt = LocalDateTime.of(2026, 7, 1, 0, 0, 0)
                    )

                    normalized shouldBe LocalDateTime.of(2026, 7, 4, 14, 59, 59)
                }
            }
        }

        Given("TASK-62 마감일 잠금 검증") {
            When("과제 마감일이 이미 지났으면") {
                Then("수정/삭제 제약 예외가 발생한다") {
                    mockTaskExpireAtNormalizer(LocalDateTime.of(2026, 6, 15, 15, 0, 0))

                    val task = createTask(expireAt = LocalDateTime.of(2026, 6, 15, 14, 59, 59))

                    val exception = shouldThrow<BadRequestException> {
                        support.checkTaskActionAllowedBySchedule(task)
                    }

                    exception.message shouldBe "마감된 과제는 수정/삭제할 수 없습니다."
                }
            }

            When("과제 마감일이 지나지 않았으면") {
                Then("잠금 없이 통과한다") {
                    mockTaskExpireAtNormalizer(LocalDateTime.of(2026, 6, 15, 14, 59, 58))

                    val task = createTask(expireAt = LocalDateTime.of(2026, 6, 15, 14, 59, 59))

                    shouldNotThrowAny {
                        support.checkTaskActionAllowedBySchedule(task)
                    }
                }
            }
        }

        Given("POST 과제 대상자 검증") {
            When("요청 대상자 중 그룹 멤버가 아닌 사용자가 있으면") {
                Then("BadRequestException이 발생한다") {
                    val groupId = ObjectId.get()
                    val memberId1 = ObjectId.get()
                    val memberId2 = ObjectId.get()

                    every {
                        groupMemberRepository.findByGroupIdAndMemberIdsIn(groupId, listOf(memberId1, memberId2))
                    } returns listOf(
                        StudyGroupMember(
                            groupId = groupId,
                            memberId = memberId1,
                            nickname = "member-1",
                            profileImage = ProfileImageVo(StudyGroupProfileImageType.PRESET, "https://example.com/1.png")
                        )
                    )

                    val exception = shouldThrow<BadRequestException> {
                        support.validateAssigneeMembersInGroup(groupId, listOf(memberId1, memberId2))
                    }

                    exception.message shouldBe "과제 대상자는 모두 그룹 멤버여야 합니다."
                }
            }
        }

        Given("ObjectId 형식 검증") {
            When("문자열이 ObjectId 형식이 아니면") {
                Then("BadRequestException이 발생한다") {
                    val exception = shouldThrow<BadRequestException> {
                        support.toObjectId("not-object-id", "taskId")
                    }

                    exception.message shouldBe "taskId 형식이 올바르지 않습니다."
                }
            }
        }

        Given("과제 DTO 변환") {
            When("제출 상태가 섞인 assignee 목록이 있으면") {
                Then("제출/미제출 수를 계산해 DTO에 반영한다") {
                    val groupId = ObjectId.get()
                    val memberId1 = ObjectId.get()
                    val memberId2 = ObjectId.get()
                    val task = createTask(expireAt = LocalDateTime.now().plusDays(2))
                    val assignees = listOf(
                        TaskAssignee(taskId = task.id!!, memberId = memberId1, status = TaskAssigneeStatus.SUBMITTED),
                        TaskAssignee(taskId = task.id!!, memberId = memberId2, status = TaskAssigneeStatus.NOT_SUBMITTED),
                        TaskAssignee(taskId = task.id!!, memberId = ObjectId.get(), status = TaskAssigneeStatus.NOT_SUBMITTED)
                    )
                    val members = listOf(
                        StudyGroupMember(
                            groupId = groupId,
                            memberId = memberId1,
                            nickname = "member-1",
                            profileImage = ProfileImageVo(StudyGroupProfileImageType.PRESET, "https://example.com/1.png")
                        ),
                        StudyGroupMember(
                            groupId = groupId,
                            memberId = memberId2,
                            nickname = "member-2",
                            profileImage = ProfileImageVo(StudyGroupProfileImageType.EXTERNAL, "https://example.com/2.png")
                        ),
                        StudyGroupMember(
                            groupId = groupId,
                            memberId = assignees[2].memberId,
                            nickname = "member-3",
                            profileImage = ProfileImageVo(StudyGroupProfileImageType.PRESET, "https://example.com/3.png")
                        )
                    )

                    every { taskFilePort.loadByIds(emptyList()) } returns emptyList()
                    every { taskAssigneeRepository.findByTaskId(task.id!!) } returns assignees
                    every {
                        groupMemberRepository.findByGroupIdAndMemberIdsIn(
                            groupId,
                            assignees.map { it.memberId }
                        )
                    } returns members

                    val result = support.toTaskDto(task, groupId)

                    result.submittedAssigneeCount shouldBe 1
                    result.notSubmittedAssigneeCount shouldBe 2
                    result.assignees.size shouldBe 3
                    result.submissionType shouldBe TaskSubmissionType.OUTPUT
                }
            }
        }

        Given("첨부 파일 강등 처리") {
            When("어떤 과제/제출에서도 참조하지 않는 파일이면") {
                Then("파일 상태를 UNREFERENCED로 갱신한다") {
                    val fileId = ObjectId.get()
                    every { taskRepository.findAttachedFileIds(listOf(fileId)) } returns emptySet()
                    every { taskSubmissionRepository.findAttachedFileIds(listOf(fileId)) } returns emptySet()
                    every { taskFilePort.updateStatus(fileId, TaskFileStatusDto.UNREFERENCED) } returns TaskFileStatusDto.UNREFERENCED

                    support.downgradeOrphanedFiles(listOf(fileId))

                    verify(exactly = 1) { taskRepository.findAttachedFileIds(listOf(fileId)) }
                    verify(exactly = 1) { taskSubmissionRepository.findAttachedFileIds(listOf(fileId)) }
                    verify(exactly = 1) {
                        taskFilePort.updateStatus(fileId, TaskFileStatusDto.UNREFERENCED)
                    }
                }
            }

            When("다른 과제/제출에서 여전히 참조 중인 파일이면") {
                Then("UNREFERENCED로 갱신하지 않는다") {
                    val fileId = ObjectId.get()
                    every { taskRepository.findAttachedFileIds(listOf(fileId)) } returns setOf(fileId)
                    every { taskSubmissionRepository.findAttachedFileIds(listOf(fileId)) } returns emptySet()

                    support.downgradeOrphanedFiles(listOf(fileId))

                    verify(exactly = 0) { taskFilePort.updateStatus(any(), any()) }
                }
            }
        }
    }

    private fun mockTaskExpireAtNormalizer(currentUtcDateTime: LocalDateTime) {
        mockkObject(TaskExpireAtNormalizer)
        taskExpireAtNormalizerMocked = true
        every { TaskExpireAtNormalizer.currentUtcDateTime() } returns currentUtcDateTime
        every { TaskExpireAtNormalizer.isExpired(any()) } answers { callOriginal() }
        every { TaskExpireAtNormalizer.toKst(any()) } answers { callOriginal() }
    }

    private fun createTask(expireAt: LocalDateTime): net.noti_me.dymit.dymit_backend_api.task.domain.Task {
        return net.noti_me.dymit.dymit_backend_api.task.domain.Task(
            id = ObjectId.get(),
            relatedScheduleId = ObjectId.get(),
            type = TaskType.PRE,
            title = "과제 제목",
            description = "설명",
            attachments = emptyList(),
            expireAt = expireAt
        )
    }
}
