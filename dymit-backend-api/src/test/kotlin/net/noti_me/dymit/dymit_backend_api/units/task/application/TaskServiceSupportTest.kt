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
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
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
import java.time.Instant

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
                    val requestedAt = Instant.parse("2026-06-11T10:00:00Z")
                    val upcomingSchedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = requestedAt.plusSeconds(1L * 3600L)
                    )

                    support.resolveTaskTypeBySchedule(upcomingSchedule, requestedAt) shouldBe TaskType.PRE
                }
            }

            When("일정 시작 시각과 요청 시각이 같으면") {
                Then("POST 타입으로 결정된다") {
                    val requestedAt = Instant.parse("2026-06-11T10:00:00Z")
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
                    val requestedAt = Instant.parse("2026-06-11T10:00:00Z")
                    val startedSchedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = requestedAt.minusSeconds(1L * 3600L)
                    )

                    support.resolveTaskTypeBySchedule(startedSchedule, requestedAt) shouldBe TaskType.POST
                }
            }

            When("요청 시각이 일정 시작 24시간 전과 정확히 같으면") {
                Then("사전 과제 생성 검증을 통과한다") {
                    val schedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = Instant.parse("2026-06-12T10:00:00Z")
                    )

                    shouldNotThrowAny {
                        support.validatePreTaskCreatable(schedule, schedule.scheduleAt.minusSeconds(24L * 3600L))
                    }
                }
            }

            When("요청 시각이 일정 시작 24시간 전보다 늦으면") {
                Then("BadRequestException이 발생한다") {
                    val schedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = Instant.parse("2026-06-12T10:00:00Z")
                    )

                    val exception = shouldThrow<BadRequestException> {
                        support.validatePreTaskCreatable(schedule, schedule.scheduleAt.minusSeconds(24L * 3600L).plusSeconds(1L * 60L))
                    }

                    exception.message shouldBe "사전 과제는 일정 시작 24시간 이전에만 생성할 수 있습니다."
                }
            }
        }

        Given("TASK-62 생성/수정 마감 시각 규칙") {
            When("PRE 생성 요청의 expireAt(KST)이 연관 일정 시간(UTC0)과 일치하면") {
                Then("정상적으로 일정 시간으로 변환된다") {
                    val scheduleAtUtc0 = Instant.parse("2026-06-10T00:00:00Z")
                    val schedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = scheduleAtUtc0
                    )

                    val normalized = support.normalizeExpireAtForCreate(
                        type = TaskType.PRE,
                        requestedExpireAt = Instant.parse("2026-06-10T09:00:00Z"),
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
                        scheduleAt = Instant.parse("2026-06-10T00:00:00Z")
                    )

                    val normalized = support.normalizeExpireAtForCreate(
                        type = TaskType.PRE,
                        requestedExpireAt = Instant.parse("2026-06-10T09:01:00Z"),
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
                        scheduleAt = Instant.parse("2026-06-01T00:00:00Z")
                    )

                    val normalized = support.normalizeExpireAtForCreate(
                        type = TaskType.POST,
                        requestedExpireAt = Instant.parse("2026-06-01T08:30:00Z"),
                        schedule = schedule
                    )

                    normalized shouldBe Instant.parse("2026-06-01T14:59:59Z")
                }
            }

            When("PRE 수정 요청이 들어오면") {
                Then("요청 expireAt을 무시하고 기존 마감일을 유지한다") {
                    val currentExpireAt = Instant.now().plusSeconds(3L * 86400L)

                    val normalized = support.normalizeExpireAtForUpdate(
                        type = TaskType.PRE,
                        requestedExpireAt = Instant.now().minusSeconds(1L * 86400L),
                        currentExpireAt = currentExpireAt
                    )

                    normalized shouldBe currentExpireAt
                }
            }

            When("POST 수정 요청의 시간이 포함되어 오면") {
                Then("요청 날짜의 23:59:59(KST)를 UTC0로 정규화한다") {
                    val normalized = support.normalizeExpireAtForUpdate(
                        type = TaskType.POST,
                        requestedExpireAt = Instant.parse("2026-07-04T10:00:00Z"),
                        currentExpireAt = Instant.parse("2026-07-01T00:00:00Z")
                    )

                    normalized shouldBe Instant.parse("2026-07-04T14:59:59Z")
                }
            }
        }

        Given("TASK-62 마감일 잠금 검증") {
            When("과제 마감일이 이미 지났으면") {
                Then("수정/삭제 제약 예외가 발생한다") {
                    mockTaskExpireAtNormalizer(Instant.parse("2026-06-15T15:00:00Z"))

                    val task = createTask(expireAt = Instant.parse("2026-06-15T14:59:59Z"))

                    val exception = shouldThrow<BadRequestException> {
                        support.checkTaskActionAllowedBySchedule(task)
                    }

                    exception.message shouldBe "마감된 과제는 수정/삭제할 수 없습니다."
                }
            }

            When("과제 마감일이 지나지 않았으면") {
                Then("잠금 없이 통과한다") {
                    mockTaskExpireAtNormalizer(Instant.parse("2026-06-15T14:59:58Z"))

                    val task = createTask(expireAt = Instant.parse("2026-06-15T14:59:59Z"))

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
            When("제출 상태가 섞인 assignee 목록이 있고 누락된 그룹 멤버를 허용하면") {
                Then("fallback 값과 정상 멤버 값을 함께 반영한다") {
                    val groupId = ObjectId.get()
                    val memberId1 = ObjectId.get()
                    val memberId2 = ObjectId.get()
                    val missingMemberId = ObjectId.get()
                    val task = createTask(expireAt = Instant.now().plusSeconds(2L * 86400L))
                    val assignees = listOf(
                        TaskAssignee(taskId = task.id!!, memberId = memberId1, status = TaskAssigneeStatus.SUBMITTED),
                        TaskAssignee(taskId = task.id!!, memberId = memberId2, status = TaskAssigneeStatus.NOT_SUBMITTED),
                        TaskAssignee(taskId = task.id!!, memberId = missingMemberId, status = TaskAssigneeStatus.NOT_SUBMITTED)
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

                    val result = support.toTaskDto(task, groupId, allowMissingAssignee = true)

                    result.submittedAssigneeCount shouldBe 1
                    result.notSubmittedAssigneeCount shouldBe 2
                    result.assignees.size shouldBe 3
                    result.submissionType shouldBe TaskSubmissionType.OUTPUT
                    result.assignees[0].memberId shouldBe memberId1.toHexString()
                    result.assignees[0].nickname shouldBe "member-1"
                    result.assignees[0].profileImageUrl shouldBe "https://example.com/1.png"
                    result.assignees[0].profileImageType shouldBe ProfileImageType.PRESET
                    result.assignees[0].status shouldBe TaskAssigneeStatus.SUBMITTED
                    result.assignees[1].memberId shouldBe memberId2.toHexString()
                    result.assignees[1].nickname shouldBe "member-2"
                    result.assignees[1].profileImageUrl shouldBe "https://example.com/2.png"
                    result.assignees[1].profileImageType shouldBe ProfileImageType.EXTERNAL
                    result.assignees[1].status shouldBe TaskAssigneeStatus.NOT_SUBMITTED
                    result.assignees[2].memberId shouldBe missingMemberId.toHexString()
                    result.assignees[2].nickname shouldBe "탈퇴한 회원"
                    result.assignees[2].profileImageUrl shouldBe
                        "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/kick_64x64.png"
                    result.assignees[2].profileImageType shouldBe ProfileImageType.PRESET
                    result.assignees[2].status shouldBe TaskAssigneeStatus.NOT_SUBMITTED
                }
            }

            When("제출 대상자 중 그룹 멤버가 누락되고 기본 경로를 사용하면") {
                Then("NotFoundException을 유지한다") {
                    val groupId = ObjectId.get()
                    val task = createTask(expireAt = Instant.now().plusSeconds(2L * 86400L))
                    val missingMemberId = ObjectId.get()
                    val assignees = listOf(
                        TaskAssignee(taskId = task.id!!, memberId = missingMemberId, status = TaskAssigneeStatus.NOT_SUBMITTED)
                    )

                    every { taskFilePort.loadByIds(emptyList()) } returns emptyList()
                    every { taskAssigneeRepository.findByTaskId(task.id!!) } returns assignees
                    every {
                        groupMemberRepository.findByGroupIdAndMemberIdsIn(
                            groupId,
                            assignees.map { it.memberId }
                        )
                    } returns emptyList()

                    val exception = shouldThrow<NotFoundException> {
                        support.toTaskDto(task, groupId)
                    }

                    exception.message shouldBe "그룹 멤버 정보를 찾을 수 없습니다."
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

    private fun mockTaskExpireAtNormalizer(currentUtcDateTime: Instant) {
        mockkObject(TaskExpireAtNormalizer)
        taskExpireAtNormalizerMocked = true
        every { TaskExpireAtNormalizer.currentUtcDateTime() } returns currentUtcDateTime
        every { TaskExpireAtNormalizer.isExpired(any()) } answers { callOriginal() }
        every { TaskExpireAtNormalizer.toKst(any()) } answers { callOriginal() }
    }

    private fun createTask(expireAt: Instant): net.noti_me.dymit.dymit_backend_api.task.domain.Task {
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
