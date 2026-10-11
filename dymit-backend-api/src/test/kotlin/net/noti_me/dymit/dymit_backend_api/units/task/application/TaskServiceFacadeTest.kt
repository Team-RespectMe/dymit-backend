package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceFacade
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateSubmissionInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.*
import org.bson.types.ObjectId

/** Verifies that the web-facing facade crosses into Task use cases using port-owned inputs. */
internal class TaskServiceFacadeTest : BehaviorSpec({
    Given("a submission creation request") {
        val createSubmission = mockk<CreateSubmissionUseCase>()
        val facade = TaskServiceFacade(
            mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), createSubmission, mockk(), mockk(), mockk(),
            mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk()
        )
        val member = MemberInfo(ObjectId.get().toHexString(), "tester", listOf(MemberRole.ROLE_MEMBER.name))
        val groupId = ObjectId.get().toHexString()
        val taskId = ObjectId.get().toHexString()
        val command = CreateTaskSubmissionCommand("title", "content", emptyList())
        every { createSubmission.execute(CreateSubmissionInput(member, groupId, taskId, command)) } returns mockk<TaskSubmissionDto>()

        When("the facade delegates it") {
            facade.createSubmission(member, groupId, taskId, command)

            Then("it supplies the Task port input DTO") {
                verify(exactly = 1) { createSubmission.execute(CreateSubmissionInput(member, groupId, taskId, command)) }
            }
        }
    }
    Given("관리 과제 파사드 요청") {
        Then("현재 사용자의 커서와 size를 전용 유스케이스에 전달한다") {
            val managed = mockk<GetManagedTasksUseCase>()
            val facade = TaskServiceFacade(
                mockk(), mockk(), mockk(), mockk(), managed, mockk(), mockk(), mockk(), mockk(), mockk(),
                mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk()
            )
            val member = net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo(net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity())
            every { managed.execute(any()) } returns emptyList()
            facade.getManagedTasks(member, "opaque", 7)
            verify(exactly = 1) { managed.execute(match { it.memberInfo == member && it.cursor == "opaque" && it.size == 7 }) }
        }
    }

})
