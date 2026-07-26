package net.noti_me.dymit.dymit_backend_api.domain.task.event

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResource
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import org.bson.types.ObjectId

/**
 * 과제 수정 브로드캐스트 이벤트입니다.
 *
 * @param group 과제가 속한 스터디 그룹
 * @param task 수정된 과제
 * @param memberIds 수신 대상 멤버 ID 목록
 */
class TaskModifiedBroadcastEvent(
    private val group: StudyGroup,
    private val task: Task,
    memberIds: List<ObjectId>
) : BroadcastEvent(memberIds.distinct()) {

    private val eventName = "TASK_MODIFIED"
    private val message = "${group.name}의 과제 ${task.title}에 수정된 내용이 있어요."

    /**
     * 수신 대상별 개인 푸시 메시지를 생성합니다.
     *
     * @return 개인 푸시 메시지 목록
     */
    override fun processPushMessages(): List<PersonalPushMessage> {
        return memberIds.map { memberId ->
            PersonalPushMessage(
                memberId = memberId,
                eventName = eventName,
                title = group.name,
                body = message,
                image = null,
                data = mapOf(
                    "groupId" to group.identifier,
                    "taskId" to task.identifier
                )
            )
        }
    }

    /**
     * 수신 대상별 사용자 피드를 생성합니다.
     *
     * @return 사용자 피드 목록
     */
    override fun processPersonalFeedData(): List<PersonalFeedEventData> {
        return memberIds.map { memberId ->
            PersonalFeedEventData(
                memberId = memberId.toHexString(),
                iconType = FeedEventIconType.CHECK,
                eventName = eventName,
                messages = listOf(FeedEventMessage(text = message)),
                resources = listOf(
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP,
                        resourceId = group.identifier
                    ),
                    FeedEventResource(
                        type = FeedEventResourceType.TASK,
                        resourceId = task.identifier
                    )
                )
            )
        }
    }
}
