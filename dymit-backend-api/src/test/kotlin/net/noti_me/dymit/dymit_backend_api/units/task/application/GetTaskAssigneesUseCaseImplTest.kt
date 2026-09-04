package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskAssigneesQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeMemberDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.GetTaskAssigneesUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskProfileImageType as ProfileImageType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto as StudySchedule
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import org.bson.types.ObjectId
import java.time.Instant

/**
 * 과제 제출 대상 목록 조회 유즈케이스 단위 테스트입니다.
 */
internal class GetTaskAssigneesUseCaseImplTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>()
    private val useCase = GetTaskAssigneesUseCaseImpl(support)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 제출 대상 목록 조회 요청이 주어지면") {
            When("유즈케이스를 실행하면") {
                Then("과제와 일정, 멤버 검증 후 전체 대상자를 반환한다") {
                    val memberId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = ObjectId.get(),
                        type = net.noti_me.dymit.dymit_backend_api.task.domain.TaskType.PRE,
                        title = "과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = Instant.now().plusSeconds(2L * 86400L)
                    )
                    val schedule = StudySchedule(
                        id = task.relatedScheduleId,
                        groupId = groupId,
                        scheduleAt = Instant.now().plusSeconds(1L * 86400L)
                    )
                    val memberInfo = MemberInfo(
                        memberId = memberId.toHexString(),
                        nickname = "tester",
                        roles = listOf(MemberRole.ROLE_MEMBER.name)
                    )
                    val assignees = listOf(
                        TaskAssigneeDto(
                            taskId = taskId.toHexString(),
                            member = TaskAssigneeMemberDto(
                                id = memberId.toHexString(),
                                nickname = "member-1",
                                profileImage = ProfileImageVo(ProfileImageType.PRESET, "https://example.com/profile.png")
                            )
                        ),
                        TaskAssigneeDto(
                            taskId = taskId.toHexString(),
                            member = TaskAssigneeMemberDto(
                                id = ObjectId.get().toHexString(),
                                nickname = "member-2",
                                profileImage = ProfileImageVo(ProfileImageType.EXTERNAL, "https://example.com/profile2.png")
                            )
                        )
                    )

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.loadSchedule(task.relatedScheduleId.toHexString()) } returns schedule
                    every { support.requireGroupMember(groupId, memberId) } returns mockk<StudyGroupMember>()
                    every { support.toTaskAssigneeDtos(taskId, groupId) } returns assignees

                    val result = useCase.execute(GetTaskAssigneesQuery(memberInfo, taskId.toHexString()))

                    verify(exactly = 1) { support.loadTask(taskId.toHexString()) }
                    verify(exactly = 1) { support.loadSchedule(task.relatedScheduleId.toHexString()) }
                    verify(exactly = 1) { support.requireGroupMember(groupId, memberId) }
                    verify(exactly = 1) { support.toTaskAssigneeDtos(taskId, groupId) }
                    result.size shouldBe 2
                    result[0].member.nickname shouldBe "member-1"
                    result[1].member.profileImage.url shouldBe "https://example.com/profile2.png"
                }

                Then("요청자가 그룹 비회원이면 403을 유지한다") {
                    val requesterId = ObjectId.get()
                    val taskId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val task = Task(
                        id = taskId,
                        relatedScheduleId = ObjectId.get(),
                        type = net.noti_me.dymit.dymit_backend_api.task.domain.TaskType.PRE,
                        title = "과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = Instant.now().plusSeconds(2L * 86400L)
                    )
                    val schedule = StudySchedule(
                        id = task.relatedScheduleId,
                        groupId = groupId,
                        scheduleAt = Instant.now().plusSeconds(1L * 86400L)
                    )
                    val memberInfo = MemberInfo(
                        memberId = requesterId.toHexString(),
                        nickname = "outsider",
                        roles = listOf(MemberRole.ROLE_MEMBER.name)
                    )

                    every { support.loadTask(taskId.toHexString()) } returns task
                    every { support.loadSchedule(task.relatedScheduleId.toHexString()) } returns schedule
                    every { support.requireGroupMember(groupId, requesterId) } throws ForbiddenException(
                        message = "그룹 멤버만 접근할 수 있습니다."
                    )

                    val exception = shouldThrow<ForbiddenException> {
                        useCase.execute(GetTaskAssigneesQuery(memberInfo, taskId.toHexString()))
                    }

                    exception.message shouldBe "그룹 멤버만 접근할 수 있습니다."
                    verify(exactly = 1) { support.loadTask(taskId.toHexString()) }
                    verify(exactly = 1) { support.loadSchedule(task.relatedScheduleId.toHexString()) }
                    verify(exactly = 1) { support.requireGroupMember(groupId, requesterId) }
                    verify(exactly = 0) { support.toTaskAssigneeDtos(any(), any()) }
                }
            }
        }
    }
}
