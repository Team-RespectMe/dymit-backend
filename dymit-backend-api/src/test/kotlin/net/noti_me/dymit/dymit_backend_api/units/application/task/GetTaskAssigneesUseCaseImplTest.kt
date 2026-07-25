package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeMemberDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.GetTaskAssigneesUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import org.bson.types.ObjectId
import java.time.LocalDateTime

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
                        type = net.noti_me.dymit.dymit_backend_api.domain.task.TaskType.PRE,
                        title = "과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2)
                    )
                    val schedule = StudySchedule(
                        id = task.relatedScheduleId,
                        groupId = groupId,
                        scheduleAt = LocalDateTime.now().plusDays(1)
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

                    val result = useCase.getTaskAssignees(memberInfo, taskId.toHexString())

                    verify(exactly = 1) { support.loadTask(taskId.toHexString()) }
                    verify(exactly = 1) { support.loadSchedule(task.relatedScheduleId.toHexString()) }
                    verify(exactly = 1) { support.requireGroupMember(groupId, memberId) }
                    verify(exactly = 1) { support.toTaskAssigneeDtos(taskId, groupId) }
                    result.size shouldBe 2
                    result[0].member.nickname shouldBe "member-1"
                    result[1].member.profileImage.url shouldBe "https://example.com/profile2.png"
                }
            }
        }
    }
}
