package net.noti_me.dymit.dymit_backend_api.units.controllers.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.validation.Validation
import jakarta.validation.Validator
import net.noti_me.dymit.dymit_backend_api.application.task.TaskService
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeSummaryDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAttachmentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionAttachmentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.task.TaskApi
import net.noti_me.dymit.dymit_backend_api.controllers.task.TaskController
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskResponse
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionAttachmentRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionCommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskUpdateRequest
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskProfileImageType as ProfileImageType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class TaskControllerValidationAndResponseTest : BehaviorSpec() {

    private val taskService = mockk<TaskService>()
    private val controller = TaskController(taskService)
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    private val memberInfo = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "tester",
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 생성 요청 검증") {
            When("제목이 비어 있으면") {
                Then("검증에 실패한다") {
                    val request = TaskCommandRequest(
                        relatedScheduleId = ObjectId.get().toHexString(),
                        title = "",
                        description = "설명",
                        attachmentFileIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2)
                    )

                    TaskCommandRequest::class.java.declaredFields.map { it.name } shouldNotContain "type"

                    val violations = validator.validate(request)
                    violations.map { it.message } shouldContain "과제 제목은 비어 있을 수 없습니다."
                }
            }
        }

        Given("과제 수정 요청 검증") {
            When("제목이 비어 있으면") {
                Then("검증에 실패한다") {
                    val request = TaskUpdateRequest(
                        title = "",
                        description = "설명",
                        attachmentFileIds = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2)
                    )

                    val violations = validator.validate(request)
                    violations.map { it.message } shouldContain "과제 제목은 비어 있을 수 없습니다."
                }
            }

            When("submissionType을 포함하지 않으면") {
                Then("수정 요청에는 제출 방식 필드가 없다") {
                    val fields = TaskUpdateRequest::class.java.declaredFields.map { it.name }

                    fields shouldNotContain "submissionType"
                    fields shouldContain "assigneeMemberIds"
                }
            }
        }

        Given("댓글 생성 요청 검증") {
            When("댓글 내용이 비어 있으면") {
                Then("검증에 실패한다") {
                    val request = TaskSubmissionCommentCommandRequest(content = "")

                    val violations = validator.validate(request)
                    violations.map { it.message } shouldContain "댓글 내용은 비어 있을 수 없습니다."
                }
            }
        }

        Given("TaskController 응답 매핑") {
            When("createTask를 호출하면") {
                Then("서비스 DTO를 TaskResponse로 변환한다") {
                    val groupId = ObjectId.get().toHexString()
                    val request = TaskCommandRequest(
                        relatedScheduleId = ObjectId.get().toHexString(),
                        title = "사전 과제",
                        description = "설명",
                        attachmentFileIds = listOf(ObjectId.get().toHexString()),
                        expireAt = LocalDateTime.now().plusDays(2)
                    )
                    val dto = TaskDto(
                        taskId = ObjectId.get().toHexString(),
                        relatedScheduleId = request.relatedScheduleId,
                        type = TaskType.PRE,
                        title = "사전 과제",
                        description = "설명",
                        attachments = listOf(
                            TaskAttachmentDto(
                                fileId = request.attachmentFileIds.first(),
                                originalFileName = "task.pdf",
                                url = "https://cdn.example.com/task.pdf",
                                thumbnailUrl = null,
                                status = TaskFileStatusDto.LINKED
                            )
                        ),
                        expireAt = request.expireAt,
                        submittedAssigneeCount = 0,
                        notSubmittedAssigneeCount = 1,
                        submissionType = TaskSubmissionType.CHECK,
                        assignees = listOf(
                            TaskAssigneeSummaryDto(
                                memberId = memberInfo.memberId,
                                nickname = "tester",
                                profileImageUrl = "https://example.com/profile.png",
                                profileImageType = ProfileImageType.PRESET,
                                status = TaskAssigneeStatus.NOT_SUBMITTED
                            )
                        )
                    )

                    every { taskService.createTask(memberInfo, groupId, any()) } returns dto

                    val response = controller.createTask(memberInfo, groupId, request)

                    verify(exactly = 1) { taskService.createTask(memberInfo, groupId, any()) }
                    TaskResponse::class.java.declaredFields.map { it.name } shouldContain "type"
                    response.taskId shouldBe dto.taskId
                    response.type shouldBe dto.type
                    response.attachments[0].fileId shouldBe dto.attachments[0].fileId
                    response.submittedAssigneeCount shouldBe 0
                    response.notSubmittedAssigneeCount shouldBe 1
                    response.submissionType shouldBe TaskSubmissionType.CHECK
                    response.assignees[0].status shouldBe TaskAssigneeStatus.NOT_SUBMITTED
                    response._links["self"]?.href shouldBe "/api/v1/study-groups/$groupId/tasks/${dto.taskId}"
                }
            }

            When("createSubmissionComment를 호출하면") {
                Then("서비스 DTO를 TaskSubmissionCommentResponse로 변환한다") {
                    val groupId = ObjectId.get().toHexString()
                    val taskId = ObjectId.get().toHexString()
                    val submissionId = ObjectId.get().toHexString()
                    val request = TaskSubmissionCommentCommandRequest(content = "피드백")
                    val dto = TaskSubmissionCommentDto(
                        commentId = ObjectId.get().toHexString(),
                        taskId = taskId,
                        submissionId = submissionId,
                        writerId = memberInfo.memberId,
                        writerNickname = "tester",
                        writerProfileImageUrl = "https://example.com/profile.png",
                        writerProfileImageType = ProfileImageType.PRESET,
                        content = "피드백",
                        createdAt = LocalDateTime.now()
                    )

                    every {
                        taskService.createSubmissionComment(
                            memberInfo,
                            groupId,
                            taskId,
                            submissionId,
                            any()
                        )
                    } returns dto

                    val response = controller.createSubmissionComment(
                        memberInfo,
                        groupId,
                        taskId,
                        submissionId,
                        request
                    )

                    verify(exactly = 1) {
                        taskService.createSubmissionComment(
                            memberInfo,
                            groupId,
                            taskId,
                            submissionId,
                            any()
                        )
                    }
                    response.commentId shouldBe dto.commentId
                    response.writer.memberId shouldBe dto.writerId
                    response.writer.nickname shouldBe dto.writerNickname
                    response.writer.profileImageUrl shouldBe dto.writerProfileImageUrl
                    response.writer.profileImageType shouldBe dto.writerProfileImageType
                    response.content shouldBe "피드백"
                }
            }

            When("createSubmission을 호출하면") {
                Then("서비스 DTO를 TaskSubmissionResponse로 변환하고 member를 포함한다") {
                    val groupId = ObjectId.get().toHexString()
                    val taskId = ObjectId.get().toHexString()
                    val request = TaskSubmissionCommandRequest(
                        title = "제출 제목",
                        content = "제출 본문",
                        attachments = listOf(
                            TaskSubmissionAttachmentRequest(
                                type = TaskSubmitAttachmentType.URL,
                                title = "참고 링크",
                                url = "https://example.com/ref"
                            )
                        )
                    )
                    val dto = TaskSubmissionDto(
                        submissionId = ObjectId.get().toHexString(),
                        taskId = taskId,
                        memberId = memberInfo.memberId,
                        memberNickname = "tester",
                        memberProfileImageUrl = "https://example.com/profile.png",
                        memberProfileImageType = ProfileImageType.PRESET,
                        title = request.title,
                        content = request.content,
                        attachments = listOf(
                            TaskSubmissionAttachmentDto(
                                type = TaskSubmitAttachmentType.URL,
                                title = "참고 링크",
                                url = "https://example.com/ref",
                                fileId = null,
                                fileUrl = null,
                                originalFileName = null
                            )
                        ),
                        createdAt = LocalDateTime.now()
                    )

                    every { taskService.createSubmission(memberInfo, groupId, taskId, any()) } returns dto

                    val response = controller.createSubmission(
                        memberInfo = memberInfo,
                        groupId = groupId,
                        taskId = taskId,
                        request = request
                    )

                    verify(exactly = 1) { taskService.createSubmission(memberInfo, groupId, taskId, any()) }
                    response.submissionId shouldBe dto.submissionId
                    response.member.memberId shouldBe dto.memberId
                    response.member.nickname shouldBe dto.memberNickname
                    response.member.profileImageUrl shouldBe dto.memberProfileImageUrl
                    response.member.profileImageType shouldBe dto.memberProfileImageType
                    response.attachments.first().type shouldBe TaskSubmitAttachmentType.URL
                }
            }

            When("getSubmission을 호출하면") {
                Then("서비스 DTO를 TaskSubmissionResponse로 변환한다") {
                    val groupId = ObjectId.get().toHexString()
                    val taskId = ObjectId.get().toHexString()
                    val submissionMemberId = ObjectId.get().toHexString()
                    val dto = TaskSubmissionDto(
                        submissionId = ObjectId.get().toHexString(),
                        taskId = taskId,
                        memberId = submissionMemberId,
                        memberNickname = "submitter",
                        memberProfileImageUrl = "https://example.com/submission-profile.png",
                        memberProfileImageType = ProfileImageType.EXTERNAL,
                        title = "조회 제출",
                        content = "본문",
                        attachments = listOf(
                            TaskSubmissionAttachmentDto(
                                type = TaskSubmitAttachmentType.FILE,
                                title = "첨부",
                                url = null,
                                fileId = ObjectId.get().toHexString(),
                                fileUrl = "https://cdn.example.com/attachment.pdf",
                                originalFileName = "attachment.pdf"
                            )
                        ),
                        createdAt = LocalDateTime.now()
                    )

                    every {
                        taskService.getTaskSubmission(memberInfo, groupId, taskId, submissionMemberId)
                    } returns dto

                    val response = controller.getSubmission(memberInfo, groupId, taskId, submissionMemberId)

                    verify(exactly = 1) {
                        taskService.getTaskSubmission(memberInfo, groupId, taskId, submissionMemberId)
                    }
                    response.submissionId shouldBe dto.submissionId
                    response.taskId shouldBe dto.taskId
                    response.member.memberId shouldBe dto.memberId
                    response.member.nickname shouldBe dto.memberNickname
                    response.member.profileImageUrl shouldBe dto.memberProfileImageUrl
                    response.member.profileImageType shouldBe dto.memberProfileImageType
                    response.attachments.first().type shouldBe TaskSubmitAttachmentType.FILE
                    response.attachments.first().fileUrl shouldBe "https://cdn.example.com/attachment.pdf"
                }
            }

            When("getSubmissions가 활성 API에 남아 있는지 확인하면") {
                Then("TaskApi와 TaskController에 노출되지 않는다") {
                    TaskApi::class.java.methods.none { it.name == "getSubmissions" } shouldBe true
                    TaskController::class.java.methods.none { it.name == "getSubmissions" } shouldBe true
                }
            }

            When("updateTask를 호출하면") {
                Then("TaskUpdateRequest를 UpdateTaskCommand로 전달하고 응답을 매핑한다") {
                    val groupId = ObjectId.get().toHexString()
                    val taskId = ObjectId.get().toHexString()
                    val commandSlot = slot<UpdateTaskCommand>()
                    val request = TaskUpdateRequest(
                        title = "수정 과제",
                        description = "수정 설명",
                        attachmentFileIds = listOf(ObjectId.get().toHexString()),
                        expireAt = LocalDateTime.now().plusDays(3),
                        assigneeMemberIds = listOf(ObjectId.get().toHexString())
                    )
                    val dto = TaskDto(
                        taskId = taskId,
                        relatedScheduleId = ObjectId.get().toHexString(),
                        type = TaskType.POST,
                        title = request.title,
                        description = request.description,
                        attachments = listOf(
                            TaskAttachmentDto(
                                fileId = request.attachmentFileIds.first(),
                                originalFileName = "updated.pdf",
                                url = "https://cdn.example.com/updated.pdf",
                                thumbnailUrl = null,
                                status = TaskFileStatusDto.LINKED
                            )
                        ),
                        expireAt = request.expireAt,
                        submittedAssigneeCount = 1,
                        notSubmittedAssigneeCount = 0,
                        submissionType = TaskSubmissionType.OUTPUT,
                        assignees = listOf(
                            TaskAssigneeSummaryDto(
                                memberId = memberInfo.memberId,
                                nickname = "tester",
                                profileImageUrl = "https://example.com/profile.png",
                                profileImageType = ProfileImageType.PRESET,
                                status = TaskAssigneeStatus.SUBMITTED
                            )
                        )
                    )

                    every { taskService.updateTask(memberInfo, groupId, taskId, capture(commandSlot)) } returns dto

                    val response = controller.updateTask(memberInfo, groupId, taskId, request)

                    verify(exactly = 1) { taskService.updateTask(memberInfo, groupId, taskId, any()) }
                    commandSlot.captured.assigneeMemberIds shouldBe request.assigneeMemberIds
                    response.taskId shouldBe dto.taskId
                    response.title shouldBe "수정 과제"
                    response.type shouldBe TaskType.POST
                    response.submissionType shouldBe TaskSubmissionType.OUTPUT
                    response.submittedAssigneeCount shouldBe 1
                    response.notSubmittedAssigneeCount shouldBe 0
                    response._links["self"]?.href shouldBe "/api/v1/study-groups/$groupId/tasks/$taskId"
                }
            }

            When("getTasks를 호출하면") {
                Then("목록 응답 각 항목에 카운트와 self 링크를 포함한다") {
                    val groupId = ObjectId.get().toHexString()
                    val taskId = ObjectId.get().toHexString()
                    val dto = TaskDto(
                        taskId = taskId,
                        relatedScheduleId = ObjectId.get().toHexString(),
                        type = TaskType.PRE,
                        title = "목록 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2),
                        submittedAssigneeCount = 1,
                        notSubmittedAssigneeCount = 2,
                        assignees = listOf(
                            TaskAssigneeSummaryDto(
                                memberId = memberInfo.memberId,
                                nickname = "tester",
                                profileImageUrl = "https://example.com/profile.png",
                                profileImageType = ProfileImageType.PRESET,
                                status = TaskAssigneeStatus.SUBMITTED
                            )
                        )
                    )

                    every { taskService.getGroupTasks(memberInfo, groupId) } returns listOf(dto)

                    val response = controller.getTasks(memberInfo, groupId)

                    verify(exactly = 1) { taskService.getGroupTasks(memberInfo, groupId) }
                    response.items.size shouldBe 1
                    response.items[0].submittedAssigneeCount shouldBe 1
                    response.items[0].notSubmittedAssigneeCount shouldBe 2
                    response.items[0]._links["self"]?.href shouldBe "/api/v1/study-groups/$groupId/tasks/$taskId"
                }
            }

            When("getSubmission 경로를 확인하면") {
                Then("assigneeId는 쿼리 파라미터로만 받는다") {
                    val method = TaskController::class.java.methods.first { it.name == "getSubmission" }

                    method.declaringClass shouldBe TaskController::class.java
                    method.annotations.first { it.annotationClass.simpleName == "GetMapping" }.toString()
                        .contains("/submissions") shouldBe true
                    method.parameterAnnotations[3].any { it.annotationClass.simpleName == "RequestParam" } shouldBe true
                    method.parameterAnnotations[3].any { it.annotationClass.simpleName == "PathVariable" } shouldBe false
                    TaskApi::class.java.methods.none { it.name == "getSubmissions" } shouldBe true
                    TaskController::class.java.methods.none { it.name == "getSubmissions" } shouldBe true
                }
            }

            When("withdrawCheckSubmissionByAssignee를 호출하면") {
                Then("check 전용 서비스 메서드로 위임한다") {
                    val groupId = ObjectId.get().toHexString()
                    val taskId = ObjectId.get().toHexString()
                    val assigneeId = ObjectId.get().toHexString()

                    every {
                        taskService.withdrawCheckSubmissionByAssignee(
                            memberInfo,
                            groupId,
                            taskId,
                            assigneeId
                        )
                    } returns Unit

                    controller.withdrawCheckSubmissionByAssignee(memberInfo, groupId, taskId, assigneeId)

                    verify(exactly = 1) {
                        taskService.withdrawCheckSubmissionByAssignee(
                            memberInfo,
                            groupId,
                            taskId,
                            assigneeId
                        )
                    }
                    verify(exactly = 0) { taskService.withdrawSubmission(any(), any(), any(), any()) }
                }
            }
        }
    }
}
