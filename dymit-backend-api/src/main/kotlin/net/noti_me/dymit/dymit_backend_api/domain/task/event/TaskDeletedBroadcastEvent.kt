package net.noti_me.dymit.dymit_backend_api.domain.task.event

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastEvent
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.bson.types.ObjectId

/**
 * 과제 삭제 브로드캐스트 이벤트입니다.
 *
 * @param group 과제가 속했던 스터디 그룹
 * @param task 삭제된 과제
 * @param memberIds 수신 대상 멤버 ID 목록
 */
class TaskDeletedBroadcastEvent(
    private val group: StudyGroup,
    private val task: Task,
    memberIds: List<ObjectId>
) : BroadcastEvent(memberIds.distinct()) {

    private val eventName = "TASK_DELETED"
    private val message = "${group.name}의 과제 ${task.title}가 취소되었어요."

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
                    "groupId" to group.identifier
                )
            )
        }
    }

    /**
     * 수신 대상별 사용자 피드를 생성합니다.
     *
     * @return 사용자 피드 목록
     */
    override fun processUserFeeds(): List<UserFeed> {
        return memberIds.map { memberId ->
            UserFeed(
                memberId = memberId,
                iconType = IconType.NOTICE,
                eventName = eventName,
                messages = listOf(FeedMessage(text = message)),
                associates = listOf(
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP,
                        resourceId = group.identifier
                    )
                )
            )
        }
    }
}
