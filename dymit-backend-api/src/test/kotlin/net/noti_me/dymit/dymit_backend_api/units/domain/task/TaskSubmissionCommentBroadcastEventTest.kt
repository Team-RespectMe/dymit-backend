package net.noti_me.dymit.dymit_backend_api.units.domain.task

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskSubmissionCommentCreatedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class TaskSubmissionCommentBroadcastEventTest : BehaviorSpec({

    Given("과제 제출 댓글 생성 브로드캐스트 이벤트") {
        When("피드와 푸시를 만들면") {
            Then("댓글 생성 규격에 맞는 payload가 만들어진다") {
                val group = StudyGroup(
                    id = ObjectId.get(),
                    ownerId = ObjectId.get(),
                    name = "네트워크 스터디",
                    description = "설명"
                )
                val task = Task(
                    id = ObjectId.get(),
                    relatedScheduleId = ObjectId.get(),
                    type = TaskType.PRE,
                    title = "주차 과제",
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
                val event = TaskSubmissionCommentCreatedBroadcastEvent(
                    group = group,
                    task = task,
                    member = commenter,
                    assigneeMemberId = assigneeMemberId
                )

                val feeds = event.toFeeds()
                val pushes = event.toPushMessages()

                event.memberIds shouldContainExactly listOf(assigneeMemberId)
                feeds.first().iconType shouldBe IconType.NOTICE
                feeds.first().eventName shouldBe "TASK_SUBMISSION_COMMENT_CREATED"
                feeds.first().messages.single().text shouldBe "민수 님이 회원님의 과제 제출에 댓글을 달았습니다."
                feeds.first().associates.map { it.type } shouldContainExactly listOf(
                    ResourceType.STUDY_GROUP,
                    ResourceType.TASK,
                    ResourceType.MEMBER
                )
                feeds.first().associates.map { it.resourceId } shouldContainExactly listOf(
                    group.identifier,
                    task.identifier,
                    assigneeMemberId.toHexString()
                )
                pushes.map { it.memberId } shouldContainExactly listOf(assigneeMemberId)
                pushes.first().eventName shouldBe "TASK_SUBMISSION_COMMENT_CREATED"
                pushes.first().title shouldBe group.name
                pushes.first().body shouldBe "민수 님이 회원님의 과제 제출에 댓글을 달았습니다."
                pushes.first().data shouldBe mapOf(
                    "groupId" to group.identifier,
                    "taskId" to task.identifier,
                    "assigneeId" to assigneeMemberId.toHexString()
                )
            }
        }
    }
})
