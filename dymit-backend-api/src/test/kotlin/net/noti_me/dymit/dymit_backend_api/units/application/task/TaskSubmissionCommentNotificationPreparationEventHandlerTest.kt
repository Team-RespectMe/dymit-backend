package net.noti_me.dymit.dymit_backend_api.units.application.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.task.TaskNotificationPreparationEventHandler
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskSubmissionCommentCreatedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskSubmissionCommentCreatedEvent
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class TaskSubmissionCommentNotificationPreparationEventHandlerTest : BehaviorSpec() {

    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val handler = TaskNotificationPreparationEventHandler(support, eventPublisher)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("과제 제출 댓글 생성 이벤트 핸들러") {
            When("댓글 생성 이벤트를 받으면") {
                Then("제출자 대상 TaskSubmissionCommentCreatedBroadcastEvent를 발행한다") {
                    val group = StudyGroup(
                        id = ObjectId.get(),
                        ownerId = ObjectId.get(),
                        name = "알고리즘 스터디",
                        description = "설명"
                    )
                    val task = Task(
                        id = ObjectId.get(),
                        relatedScheduleId = ObjectId.get(),
                        type = TaskType.PRE,
                        title = "DP 과제",
                        description = "설명",
                        attachments = emptyList(),
                        expireAt = LocalDateTime.now().plusDays(2)
                    )
                    val commenter = StudyGroupMember(
                        groupId = group.id!!,
                        memberId = ObjectId.get(),
                        nickname = "민수"
                    )
                    val assigneeMemberId = ObjectId.get()
                    val event = TaskSubmissionCommentCreatedEvent(
                        taskId = task.id!!,
                        groupId = group.id!!,
                        submissionId = ObjectId.get(),
                        assigneeMemberId = assigneeMemberId,
                        task = task,
                        group = group,
                        member = commenter
                    )
                    val slot = slot<TaskSubmissionCommentCreatedBroadcastEvent>()

                    justRun { eventPublisher.publishEvent(any<TaskSubmissionCommentCreatedBroadcastEvent>()) }

                    handler.onTaskSubmissionCommentCreated(event)

                    verify(exactly = 1) { eventPublisher.publishEvent(capture(slot)) }
                    val published = slot.captured
                    published.memberIds shouldContainExactly listOf(assigneeMemberId)
                    published.toFeeds().single().associates.map { it.resourceId } shouldContainExactly listOf(
                        group.identifier,
                        task.identifier,
                        assigneeMemberId.toHexString()
                    )
                    published.toPushMessages().single().data shouldBe mapOf(
                        "groupId" to group.identifier,
                        "taskId" to task.identifier,
                        "assigneeId" to assigneeMemberId.toHexString()
                    )
                }
            }
        }
    }
}
