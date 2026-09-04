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
import net.noti_me.dymit.dymit_backend_api.task.application.GetGroupTasksUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetGroupTasksQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import org.bson.types.ObjectId
import java.time.Instant

internal class GetGroupTasksUseCaseImplTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>()
    private val useCase = GetGroupTasksUseCaseImpl(support)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("그룹 과제 목록 조회 요청이 주어지면") {
            Then("목록 경로에서만 누락된 담당자 fallback 허용 플래그를 전달한다") {
                val groupId = ObjectId.get()
                val memberId = ObjectId.get()
                val memberInfo = MemberInfo(
                    memberId = memberId.toHexString(),
                    nickname = "member",
                    roles = listOf(MemberRole.ROLE_MEMBER.name)
                )
                val firstTask = createTask(expireAt = Instant.parse("2026-08-24T10:00:00Z"))
                val secondTask = createTask(expireAt = Instant.parse("2026-08-25T10:00:00Z"))
                val firstDto = createTaskDto(secondTask)
                val secondDto = createTaskDto(firstTask)

                every { support.requireGroupMember(groupId, memberId) } returns mockk()
                every { support.loadTasksByGroup(groupId) } returns listOf(firstTask, secondTask)
                every { support.toTaskDto(secondTask, groupId, allowMissingAssignee = true) } returns firstDto
                every { support.toTaskDto(firstTask, groupId, allowMissingAssignee = true) } returns secondDto

                val result = useCase.execute(GetGroupTasksQuery(memberInfo, groupId.toHexString()))

                result shouldBe listOf(firstDto, secondDto)
                verify(exactly = 1) { support.requireGroupMember(groupId, memberId) }
                verify(exactly = 1) { support.loadTasksByGroup(groupId) }
                verify(exactly = 1) { support.toTaskDto(secondTask, groupId, allowMissingAssignee = true) }
                verify(exactly = 1) { support.toTaskDto(firstTask, groupId, allowMissingAssignee = true) }
                verify(exactly = 0) { support.toTaskDto(any(), any()) }
            }

            Then("요청자가 그룹 비회원이면 403을 유지하고 DTO 변환을 호출하지 않는다") {
                val groupId = ObjectId.get()
                val memberId = ObjectId.get()
                val memberInfo = MemberInfo(
                    memberId = memberId.toHexString(),
                    nickname = "outsider",
                    roles = listOf(MemberRole.ROLE_MEMBER.name)
                )

                every { support.requireGroupMember(groupId, memberId) } throws ForbiddenException(
                    message = "그룹 멤버만 접근할 수 있습니다."
                )

                val exception = shouldThrow<ForbiddenException> {
                    useCase.execute(GetGroupTasksQuery(memberInfo, groupId.toHexString()))
                }

                exception.message shouldBe "그룹 멤버만 접근할 수 있습니다."
                verify(exactly = 1) { support.requireGroupMember(groupId, memberId) }
                verify(exactly = 0) { support.loadTasksByGroup(any()) }
                verify(exactly = 0) { support.toTaskDto(any(), any(), any()) }
            }
        }
    }

    private fun createTask(expireAt: Instant): Task {
        return Task(
            id = ObjectId.get(),
            relatedScheduleId = ObjectId.get(),
            type = TaskType.PRE,
            title = "과제",
            description = "설명",
            attachments = emptyList(),
            expireAt = expireAt
        )
    }

    private fun createTaskDto(task: Task): TaskDto {
        return TaskDto(
            taskId = task.identifier,
            relatedScheduleId = task.relatedScheduleId.toHexString(),
            type = task.type,
            title = task.title,
            description = task.description,
            attachments = emptyList(),
            expireAt = task.expireAt,
            submittedAssigneeCount = 0,
            notSubmittedAssigneeCount = 0,
            assignees = emptyList(),
            submissionType = TaskSubmissionType.OUTPUT
        )
    }
}
