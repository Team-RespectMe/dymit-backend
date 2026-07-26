package net.noti_me.dymit.dymit_backend_api.task.domain.event

import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushEventData
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushMessagesEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResource
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import org.bson.types.ObjectId

/**
 * 과제 생성 브로드캐스트 이벤트입니다.
 *
 * @param group 과제가 속한 스터디 그룹
 * @param taskId 생성된 과제 ID
 * @param memberIds 수신 대상 멤버 ID 목록
 */
class TaskCreatedBroadcastEvent(
    private val group: StudyGroup,
    private val taskId: String,
    memberIds: List<ObjectId>
) : PersonalPushMessagesEvent, PersonalFeedEvent {

    private val distinctMemberIds = memberIds.distinct()
    private val eventName = "TASK_CREATED"
    private val message = "${group.name}에 새로운 과제가 추가되었어요."

    /**
     * 수신 대상별 개인 푸시 메시지를 생성합니다.
     *
     * @return 개인 푸시 메시지 목록
     */
    override fun toPersonalPushMessages(): List<PersonalPushEventData> {
        return distinctMemberIds.map { memberId ->
            PersonalPushEventData(
                memberId = memberId,
                eventName = eventName,
                title = group.name,
                body = message,
                image = null,
                data = mapOf(
                    "groupId" to group.identifier,
                    "taskId" to taskId
                )
            )
        }
    }

    /**
     * 수신 대상별 사용자 피드를 생성합니다.
     *
     * @return 사용자 피드 목록
     */
    override fun toPersonalFeedData(): List<PersonalFeedEventData> {
        return distinctMemberIds.map { memberId ->
            PersonalFeedEventData(
                memberId = memberId.toHexString(),
                iconType = FeedEventIconType.NOTICE,
                eventName = eventName,
                messages = listOf(FeedEventMessage(text = message)),
                resources = listOf(
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP,
                        resourceId = group.identifier
                    ),
                    FeedEventResource(
                        type = FeedEventResourceType.TASK,
                        resourceId = taskId
                    )
                )
            )
        }
    }
}
