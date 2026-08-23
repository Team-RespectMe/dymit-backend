package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.task.application.GetTaskDetailUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskDetailQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeSummaryDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskProfileImageType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class GetTaskDetailUseCaseImplTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>()
    private val useCase = GetTaskDetailUseCaseImpl(support)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 상세 조회 요청이 주어지면") {
            Then("탈퇴 담당자를 지정된 fallback 값으로 반환한다") {
                val groupId = ObjectId.get()
                val memberId = ObjectId.get()
                val task = createTask()
                val memberInfo = MemberInfo(
                    memberId = memberId.toHexString(),
                    nickname = "member",
                    roles = listOf(MemberRole.ROLE_MEMBER.name)
                )
                val expected = createTaskDto(
                    task = task,
                    assignees = listOf(
                        TaskAssigneeSummaryDto(
                            memberId = ObjectId.get().toHexString(),
                            nickname = "탈퇴한 회원",
                            profileImageUrl = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/kick_64x64.png",
                            profileImageType = TaskProfileImageType.PRESET,
                            status = TaskAssigneeStatus.NOT_SUBMITTED
                        )
                    )
                )

                every { support.requireGroupMember(groupId, memberId) } returns mockk()
                every { support.loadTask(task.identifier) } returns task
                every { support.checkTaskInGroup(task, groupId) } returns Unit
                every { support.toTaskDto(task, groupId, allowMissingAssignee = true) } returns expected

                val result = useCase.execute(
                    GetTaskDetailQuery(memberInfo, groupId.toHexString(), task.identifier)
                )

                result.assignees.single() shouldBe expected.assignees.single()
                verify(exactly = 1) { support.requireGroupMember(groupId, memberId) }
                verify(exactly = 1) { support.loadTask(task.identifier) }
                verify(exactly = 1) { support.checkTaskInGroup(task, groupId) }
                verify(exactly = 1) { support.toTaskDto(task, groupId, allowMissingAssignee = true) }
            }

            Then("정상 담당자의 닉네임·프로필·상태를 유지한다") {
                val groupId = ObjectId.get()
                val memberId = ObjectId.get()
                val task = createTask()
                val memberInfo = MemberInfo(
                    memberId = memberId.toHexString(),
                    nickname = "member",
                    roles = listOf(MemberRole.ROLE_MEMBER.name)
                )
                val expectedAssignee = TaskAssigneeSummaryDto(
                    memberId = ObjectId.get().toHexString(),
                    nickname = "assignee",
                    profileImageUrl = "https://example.com/profile.png",
                    profileImageType = TaskProfileImageType.EXTERNAL,
                    status = TaskAssigneeStatus.SUBMITTED
                )
                val expected = createTaskDto(task, listOf(expectedAssignee))

                every { support.requireGroupMember(groupId, memberId) } returns mockk()
                every { support.loadTask(task.identifier) } returns task
                every { support.checkTaskInGroup(task, groupId) } returns Unit
                every { support.toTaskDto(task, groupId, allowMissingAssignee = true) } returns expected

                val result = useCase.execute(
                    GetTaskDetailQuery(memberInfo, groupId.toHexString(), task.identifier)
                )

                result.assignees.single() shouldBe expectedAssignee
            }

            Then("요청자가 그룹 비회원이면 403을 유지하고 DTO 변환을 호출하지 않는다") {
                val groupId = ObjectId.get()
                val memberId = ObjectId.get()
                val taskId = ObjectId.get().toHexString()
                val memberInfo = MemberInfo(
                    memberId = memberId.toHexString(),
                    nickname = "outsider",
                    roles = listOf(MemberRole.ROLE_MEMBER.name)
                )

                every { support.requireGroupMember(groupId, memberId) } throws ForbiddenException(
                    message = "그룹 멤버만 접근할 수 있습니다."
                )

                val exception = shouldThrow<ForbiddenException> {
                    useCase.execute(GetTaskDetailQuery(memberInfo, groupId.toHexString(), taskId))
                }

                exception.message shouldBe "그룹 멤버만 접근할 수 있습니다."
                verify(exactly = 1) { support.requireGroupMember(groupId, memberId) }
                verify(exactly = 0) { support.loadTask(any()) }
                verify(exactly = 0) { support.toTaskDto(any(), any(), any()) }
            }
        }
    }

    private fun createTask(): Task {
        return Task(
            id = ObjectId.get(),
            relatedScheduleId = ObjectId.get(),
            type = TaskType.PRE,
            title = "과제",
            description = "설명",
            attachments = emptyList(),
            expireAt = LocalDateTime.of(2026, 8, 23, 12, 0)
        )
    }

    private fun createTaskDto(
        task: Task,
        assignees: List<TaskAssigneeSummaryDto>
    ): TaskDto {
        return TaskDto(
            taskId = task.identifier,
            relatedScheduleId = task.relatedScheduleId.toHexString(),
            type = task.type,
            title = task.title,
            description = task.description,
            attachments = emptyList(),
            expireAt = task.expireAt,
            submittedAssigneeCount = assignees.count { it.status == TaskAssigneeStatus.SUBMITTED },
            notSubmittedAssigneeCount = assignees.count { it.status == TaskAssigneeStatus.NOT_SUBMITTED },
            assignees = assignees,
            submissionType = TaskSubmissionType.OUTPUT
        )
    }
}
