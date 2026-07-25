package net.noti_me.dymit.dymit_backend_api.units.controllers.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.TaskService
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeMemberDto
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.task.TaskAssigneeController
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import org.bson.types.ObjectId

/**
 * 과제 제출 대상 조회 컨트롤러 단위 테스트입니다.
 */
internal class TaskAssigneeControllerTest : BehaviorSpec() {

    private val taskService = mockk<TaskService>()
    private val controller = TaskAssigneeController(taskService)

    private val memberInfo = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "tester",
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 제출 대상 목록 조회 요청이 주어지면") {
            When("컨트롤러가 응답을 생성하면") {
                Then("서비스 결과를 ListResponse와 assigneeId 쿼리 기반 HATEOAS 응답으로 변환한다") {
                    val groupId = ObjectId.get().toHexString()
                    val taskId = ObjectId.get().toHexString()
                    val assigneeDto = TaskAssigneeDto(
                        groupId = groupId,
                        taskId = taskId,
                        member = TaskAssigneeMemberDto(
                            id = ObjectId.get().toHexString(),
                            nickname = "member-1",
                            profileImage = ProfileImageVo(
                                type = ProfileImageType.PRESET,
                                url = "https://example.com/profile.png"
                            )
                        )
                    )

                    every { taskService.getTaskAssignees(memberInfo, taskId) } returns listOf(assigneeDto)

                    val response: ListResponse<net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskAssigneeResponse> =
                        controller.getTaskAssignees(memberInfo, taskId)

                    verify(exactly = 1) { taskService.getTaskAssignees(memberInfo, taskId) }
                    response.count shouldBe 1L
                    response.items[0].taskId shouldBe taskId
                    response.items[0].member.id shouldBe assigneeDto.member.id
                    response.items[0].member.nickname shouldBe assigneeDto.member.nickname
                    response.items[0].member.profileImage.url shouldBe assigneeDto.member.profileImage.url
                    response.items[0]._links["self"]?.href shouldBe
                        "/api/v1/study-groups/$groupId/tasks/$taskId/submissions?assigneeId=${assigneeDto.member.id}"
                    response.items[0]._links["self"]?.href shouldNotBe
                        "/api/v1/study-groups/$groupId/tasks/$taskId/assignees/${assigneeDto.member.id}/submission"
                }
            }
        }
    }
}
