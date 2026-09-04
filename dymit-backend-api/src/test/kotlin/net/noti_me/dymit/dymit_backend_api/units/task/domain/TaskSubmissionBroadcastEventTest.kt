package net.noti_me.dymit.dymit_backend_api.units.task.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskSubmissionCreatedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import org.bson.types.ObjectId
import java.time.Instant

internal class TaskSubmissionBroadcastEventTest : BehaviorSpec({

    Given("과제 제출 생성 브로드캐스트 이벤트") {
        When("중복된 수신자 목록으로 피드와 푸시를 만들면") {
            Then("제출 생성 규격에 맞는 payload가 만들어진다") {
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
                    expireAt = Instant.now().plusSeconds(2L * 86400L)
                )
                val submitter = StudyGroupMember(
                    groupId = group.id!!,
                    memberId = ObjectId.get(),
                    nickname = "민수"
                )
                val memberId1 = ObjectId.get()
                val memberId2 = ObjectId.get()
                val event = TaskSubmissionCreatedBroadcastEvent(
                    group = group,
                    task = task,
                    member = submitter,
                    memberIds = listOf(memberId1, memberId1, memberId2)
                )

                val feeds = event.toPersonalFeedData()
                val pushes = event.toPersonalPushMessages()

                event.toPersonalPushMessages().map { it.memberId } shouldContainExactly listOf(memberId1, memberId2)
                feeds.first().iconType shouldBe FeedEventIconType.CHECK
                feeds.first().eventName shouldBe "TASK_SUBMISSION_CREATED"
                feeds.first().messages.single().text shouldBe "네트워크 스터디의 과제 주차 과제를 민수 님이 제출했어요."
                feeds.first().resources.map { it.type } shouldContainExactly listOf(
                    FeedEventResourceType.STUDY_GROUP,
                    FeedEventResourceType.TASK,
                    FeedEventResourceType.MEMBER
                )
                feeds.first().resources.map { it.resourceId } shouldContainExactly listOf(
                    group.identifier,
                    task.identifier,
                    submitter.memberId.toHexString()
                )
                pushes.map { it.memberId } shouldContainExactly listOf(memberId1, memberId2)
                pushes.first().eventName shouldBe "TASK_SUBMISSION_CREATED"
                pushes.first().title shouldBe group.name
                pushes.first().body shouldBe "네트워크 스터디의 과제 주차 과제를 민수 님이 제출했어요."
                pushes.first().data shouldBe mapOf(
                    "groupId" to group.identifier,
                    "taskId" to task.identifier,
                    "assigneeId" to submitter.memberId.toHexString()
                )
            }
        }
    }
})
