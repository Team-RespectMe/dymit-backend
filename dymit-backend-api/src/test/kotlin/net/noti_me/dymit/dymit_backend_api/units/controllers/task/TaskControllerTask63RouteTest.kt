package net.noti_me.dymit.dymit_backend_api.units.controllers.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.TaskService
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.task.TaskController
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDateTime

internal class TaskControllerTask63RouteTest : BehaviorSpec() {

    private val taskService = mockk<TaskService>()
    private val controller = TaskController(taskService)

    private val memberInfo = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "tester",
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 상세 조회 응답") {
            When("서비스 DTO에 submissionType이 있으면") {
                Then("TaskResponse에도 동일한 submissionType이 포함된다") {
                    val groupId = ObjectId.get().toHexString()
                    val taskId = ObjectId.get().toHexString()
                    val dto = TaskDto(
                        taskId = taskId,
                        relatedScheduleId = ObjectId.get().toHexString(),
                        type = TaskType.POST,
                        title = "상세 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2),
                        submittedAssigneeCount = 1,
                        notSubmittedAssigneeCount = 0,
                        assignees = emptyList(),
                        submissionType = TaskSubmissionType.CHECK
                    )

                    every { taskService.getTaskDetail(memberInfo, groupId, taskId) } returns dto

                    val response = controller.getTask(memberInfo, groupId, taskId)

                    verify(exactly = 1) { taskService.getTaskDetail(memberInfo, groupId, taskId) }
                    response.submissionType shouldBe TaskSubmissionType.CHECK
                }
            }
        }

        Given("과제 제출 단건 조회 라우트") {
            When("getSubmission 매핑을 확인하면") {
                Then("assigneeId를 RequestParam으로 받고 기존 assignee path 매핑은 없다") {
                    val method = TaskController::class.java.methods.first { it.name == "getSubmission" }
                    val mapping = requireNotNull(method.getAnnotation(GetMapping::class.java))

                    mapping.value.single() shouldBe "/{groupId}/tasks/{taskId}/submissions"
                    method.parameterAnnotations[3].any { it is RequestParam } shouldBe true
                    method.parameterAnnotations[3].any { it is PathVariable } shouldBe false

                    TaskController::class.java.methods
                        .mapNotNull { it.getAnnotation(GetMapping::class.java) }
                        .flatMap { it.value.toList() }
                        .none { it.contains("/assignees/") } shouldBe true
                }
            }
        }
    }
}
