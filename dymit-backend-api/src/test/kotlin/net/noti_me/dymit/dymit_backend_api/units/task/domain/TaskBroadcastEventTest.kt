package net.noti_me.dymit.dymit_backend_api.units.task.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskCreatedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskDeletedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskModifiedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import org.bson.types.ObjectId
import java.time.Instant

internal class TaskBroadcastEventTest : BehaviorSpec({

    Given("과제 생성 브로드캐스트 이벤트") {
        When("중복된 수신자 목록으로 피드와 푸시를 만들면") {
            Then("중복이 제거되고 생성 규격에 맞는 payload가 만들어진다") {
                val group = createGroup(name = "알고리즘 스터디")
                val task = createTask(title = "사전 과제")
                val memberId1 = ObjectId.get()
                val memberId2 = ObjectId.get()
                val event = TaskCreatedBroadcastEvent(group, task.identifier, listOf(memberId1, memberId1, memberId2))

                val feeds = event.toPersonalFeedData()
                val pushes = event.toPersonalPushMessages()

                feeds.map { it.memberId } shouldContainExactly listOf(memberId1.toHexString(), memberId2.toHexString())
                feeds.first().iconType shouldBe FeedEventIconType.NOTICE
                feeds.first().eventName shouldBe "TASK_CREATED"
                feeds.first().messages.single().text shouldBe "알고리즘 스터디에 새로운 과제가 추가되었어요."
                feeds.first().resources.map { it.type } shouldContainExactly listOf(FeedEventResourceType.STUDY_GROUP, FeedEventResourceType.TASK)
                feeds.first().resources.map { it.resourceId } shouldContainExactly listOf(group.identifier, task.identifier)
                pushes.map { it.memberId } shouldContainExactly listOf(memberId1, memberId2)
                pushes.first().eventName shouldBe "TASK_CREATED"
                pushes.first().title shouldBe group.name
                pushes.first().body shouldBe "알고리즘 스터디에 새로운 과제가 추가되었어요."
                pushes.first().data shouldBe mapOf(
                    "groupId" to group.identifier,
                    "taskId" to task.identifier
                )
            }
        }
    }

    Given("과제 수정 브로드캐스트 이벤트") {
        When("피드와 푸시를 만들면") {
            Then("수정 규격에 맞는 payload가 만들어진다") {
                val group = createGroup(name = "백엔드 스터디")
                val task = createTask(title = "ERD 정리")
                val memberId = ObjectId.get()
                val event = TaskModifiedBroadcastEvent(group, task, listOf(memberId))

                val feed = event.toPersonalFeedData().single()
                val push = event.toPersonalPushMessages().single()

                feed.iconType shouldBe FeedEventIconType.CHECK
                feed.eventName shouldBe "TASK_MODIFIED"
                feed.messages.single().text shouldBe "백엔드 스터디의 과제 ERD 정리에 수정된 내용이 있어요."
                feed.resources.map { it.type } shouldContainExactly listOf(FeedEventResourceType.STUDY_GROUP, FeedEventResourceType.TASK)
                push.eventName shouldBe "TASK_MODIFIED"
                push.title shouldBe group.name
                push.body shouldBe "백엔드 스터디의 과제 ERD 정리에 수정된 내용이 있어요."
                push.data shouldBe mapOf(
                    "groupId" to group.identifier,
                    "taskId" to task.identifier
                )
            }
        }
    }

    Given("과제 삭제 브로드캐스트 이벤트") {
        When("피드와 푸시를 만들면") {
            Then("삭제 규격에 맞는 payload가 만들어진다") {
                val group = createGroup(name = "코틀린 스터디")
                val task = createTask(title = "리팩터링")
                val memberId = ObjectId.get()
                val event = TaskDeletedBroadcastEvent(group, task, listOf(memberId, memberId))

                val feed = event.toPersonalFeedData().single()
                val push = event.toPersonalPushMessages().single()

                event.toPersonalPushMessages().map { it.memberId } shouldContainExactly listOf(memberId)
                feed.iconType shouldBe FeedEventIconType.NOTICE
                feed.eventName shouldBe "TASK_DELETED"
                feed.messages.single().text shouldBe "코틀린 스터디의 과제 리팩터링가 취소되었어요."
                feed.resources.map { it.type } shouldContainExactly listOf(FeedEventResourceType.STUDY_GROUP)
                feed.resources.single().resourceId shouldBe group.identifier
                push.eventName shouldBe "TASK_DELETED"
                push.title shouldBe group.name
                push.body shouldBe "코틀린 스터디의 과제 리팩터링가 취소되었어요."
                push.data shouldBe mapOf(
                    "groupId" to group.identifier
                )
            }
        }
    }
}) {

    companion object {
        private fun createGroup(name: String): StudyGroup {
            return StudyGroup(
                id = ObjectId.get(),
                ownerId = ObjectId.get(),
                name = name,
                description = "설명"
            )
        }

        private fun createTask(title: String): Task {
            return Task(
                id = ObjectId.get(),
                relatedScheduleId = ObjectId.get(),
                type = TaskType.PRE,
                title = title,
                description = "설명",
                attachments = emptyList(),
                expireAt = Instant.now().plusSeconds(2L * 86400L)
            )
        }
    }
}
