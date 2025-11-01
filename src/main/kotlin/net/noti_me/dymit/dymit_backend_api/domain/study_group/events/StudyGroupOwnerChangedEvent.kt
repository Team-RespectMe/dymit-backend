package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

/**
 * 그룹 소유자가 변경된 경우 발생하는 이벤트
 * 피드 서비스에서는 이 이벤트를 구독하여 새로운 그룹 소유자의 사용자 피드로 만들고, Push 알림을 발행한다..
 * @param group 소유자가 변경된 그룹
 */
class StudyGroupOwnerChangedEvent(
    val group: StudyGroup,
): PersonalImportantEvent(group) {

    private val eventName = "STUDY_GROUP_OWNER_CHANGED"

    override fun processUserFeed() = UserFeed(
        iconType = IconType.DATE,
        memberId = group.ownerId,
        messages = listOf(
            FeedMessage(
                text = "${group.name}의 소유자 위임",
            )
        ),
        eventName = eventName,
        associates = listOf(
            AssociatedResource(
                type = ResourceType.STUDY_GROUP,
                resourceId = group.identifier
            )
        )
    )

    override fun processPushMessage(): PersonalPushMessage {
        return PersonalPushMessage(
            memberId = group.ownerId,
            eventName = eventName,
            title = "Dymit",
            body = "${group.name} 새로운 소유자가 되셨습니다!",
            data = mapOf(
                "groupId" to group.identifier,
            ),
            image = null,
        )
    }
}
