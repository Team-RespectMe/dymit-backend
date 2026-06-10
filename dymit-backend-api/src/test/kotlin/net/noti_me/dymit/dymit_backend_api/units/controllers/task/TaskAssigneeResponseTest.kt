package net.noti_me.dymit.dymit_backend_api.units.controllers.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeMemberDto
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskAssigneeResponse
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo

/**
 * 과제 제출 대상 응답 HATEOAS 단위 테스트입니다.
 */
internal class TaskAssigneeResponseTest : BehaviorSpec({

    Given("과제 제출 대상 DTO가 주어지면") {
        val groupId = "688c25eb2f3a71dcf291aac9"
        val taskId = "688c25eb2f3a71dcf291aaca"
        val memberId = "688c25eb2f3a71dcf291aacb"
        val dto = TaskAssigneeDto(
            groupId = groupId,
            taskId = taskId,
            member = TaskAssigneeMemberDto(
                id = memberId,
                nickname = "member-1",
                profileImage = ProfileImageVo(
                    type = ProfileImageType.PRESET,
                    url = "https://example.com/profile.png"
                )
            )
        )

        When("TaskAssigneeResponse로 변환하면") {
            val response = TaskAssigneeResponse.from(dto)

            Then("assigneeId 쿼리 파라미터 기반 단건 제출 조회 링크를 노출한다") {
                response.taskId shouldBe taskId
                response.member.id shouldBe memberId
                response._links["self"]?.href shouldBe
                    "/api/v1/study-groups/$groupId/tasks/$taskId/submissions?assigneeId=$memberId"
                response._links["self"]?.href shouldNotBe
                    "/api/v1/study-groups/$groupId/tasks/$taskId/assignees/$memberId/submission"
            }
        }
    }
})
