package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.file.FileServiceFacade
import net.noti_me.dymit.dymit_backend_api.application.file.FileUrlResolver
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskAssigneeRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionCommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class TaskServiceSupportTest : BehaviorSpec() {

    private val loadStudyGroupPort = mockk<LoadStudyGroupPort>()
    private val groupMemberRepository = mockk<StudyGroupMemberRepository>()
    private val studyScheduleRepository = mockk<StudyScheduleRepository>()
    private val scheduleParticipantRepository = mockk<ScheduleParticipantRepository>()
    private val taskRepository = mockk<TaskRepository>()
    private val taskAssigneeRepository = mockk<TaskAssigneeRepository>()
    private val taskSubmissionRepository = mockk<TaskSubmissionRepository>()
    private val taskSubmissionCommentRepository = mockk<TaskSubmissionCommentRepository>()
    private val userFileRepository = mockk<UserFileRepository>()
    private val fileServiceFacade = mockk<FileServiceFacade>()
    private val fileUrlResolver = mockk<FileUrlResolver>()

    private val support = TaskServiceSupport(
        loadStudyGroupPort = loadStudyGroupPort,
        groupMemberRepository = groupMemberRepository,
        studyScheduleRepository = studyScheduleRepository,
        scheduleParticipantRepository = scheduleParticipantRepository,
        taskRepository = taskRepository,
        taskAssigneeRepository = taskAssigneeRepository,
        taskSubmissionRepository = taskSubmissionRepository,
        taskSubmissionCommentRepository = taskSubmissionCommentRepository,
        userFileRepository = userFileRepository,
        fileServiceFacade = fileServiceFacade,
        fileUrlResolver = fileUrlResolver
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("TASK-62 과제 타입 자동 결정") {
            When("일정이 아직 시작 전이면") {
                Then("POST 타입으로 결정된다") {
                    val upcomingSchedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = LocalDateTime.now().plusHours(1)
                    )

                    support.resolveTaskTypeBySchedule(upcomingSchedule) shouldBe TaskType.POST
                }
            }

            When("일정이 이미 시작되었으면") {
                Then("PRE 타입으로 결정된다") {
                    val startedSchedule = StudySchedule(
                        id = ObjectId.get(),
                        groupId = ObjectId.get(),
                        scheduleAt = LocalDateTime.now().minusHours(1)
                    )

                    support.resolveTaskTypeBySchedule(startedSchedule) shouldBe TaskType.PRE
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
                    val task = createTask(expireAt = LocalDateTime.now().minusMinutes(1))

                    val exception = shouldThrow<BadRequestException> {
                        support.checkTaskActionAllowedBySchedule(task)
                    }

                    exception.message shouldBe "마감된 과제는 수정/삭제할 수 없습니다."
                }
            }

            When("과제 마감일이 지나지 않았으면") {
                Then("잠금 없이 통과한다") {
                    val task = createTask(expireAt = LocalDateTime.now().plusDays(2))

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
                            profileImage = ProfileImageVo(ProfileImageType.PRESET, "https://example.com/1.png")
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
                            profileImage = ProfileImageVo(ProfileImageType.PRESET, "https://example.com/1.png")
                        ),
                        StudyGroupMember(
                            groupId = groupId,
                            memberId = memberId2,
                            nickname = "member-2",
                            profileImage = ProfileImageVo(ProfileImageType.EXTERNAL, "https://example.com/2.png")
                        ),
                        StudyGroupMember(
                            groupId = groupId,
                            memberId = assignees[2].memberId,
                            nickname = "member-3",
                            profileImage = ProfileImageVo(ProfileImageType.PRESET, "https://example.com/3.png")
                        )
                    )

                    every { userFileRepository.findByIds(emptyList()) } returns emptyList()
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
                    every { fileServiceFacade.updateFileStatus(any()) } returns createFileDto(fileId.toHexString(), UserFileStatus.UNREFERENCED)

                    support.downgradeOrphanedFiles(listOf(fileId))

                    verify(exactly = 1) { taskRepository.findAttachedFileIds(listOf(fileId)) }
                    verify(exactly = 1) { taskSubmissionRepository.findAttachedFileIds(listOf(fileId)) }
                    verify(exactly = 1) {
                        fileServiceFacade.updateFileStatus(match {
                            it.fileId == fileId.toHexString() && it.status == UserFileStatus.UNREFERENCED
                        })
                    }
                }
            }

            When("다른 과제/제출에서 여전히 참조 중인 파일이면") {
                Then("UNREFERENCED로 갱신하지 않는다") {
                    val fileId = ObjectId.get()
                    every { taskRepository.findAttachedFileIds(listOf(fileId)) } returns setOf(fileId)
                    every { taskSubmissionRepository.findAttachedFileIds(listOf(fileId)) } returns emptySet()

                    support.downgradeOrphanedFiles(listOf(fileId))

                    verify(exactly = 0) { fileServiceFacade.updateFileStatus(any()) }
                }
            }
        }
    }

    private fun createFileDto(fileId: String, status: UserFileStatus): FileDto {
        return FileDto(
            fileId = fileId,
            status = status,
            originalFileName = "test.txt",
            path = "/files/test.txt",
            url = "https://cdn.example.com/files/test.txt",
            thumbnail = null
        )
    }

    private fun createTask(expireAt: LocalDateTime): net.noti_me.dymit.dymit_backend_api.domain.task.Task {
        return net.noti_me.dymit.dymit_backend_api.domain.task.Task(
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
