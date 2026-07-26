package net.noti_me.dymit.dymit_backend_api.feed.adapter.`in`.event

import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResource
import net.noti_me.dymit.dymit_backend_api.common.event.feed.GroupFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreateGroupFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreatePersonalFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreateGroupFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreatePersonalFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.domain.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.feed.domain.FeedMessage
import net.noti_me.dymit.dymit_backend_api.feed.domain.IconType
import net.noti_me.dymit.dymit_backend_api.feed.domain.ResourceType
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 공통 피드 이벤트를 Feed 입력 명령으로 변환하는 어댑터입니다.
 *
 * @param createPersonalFeedUseCase 개인 피드 생성 유스케이스
 * @param createGroupFeedUseCase 그룹 피드 생성 유스케이스
 */
@Component
class CommonFeedEventAdapter(
    private val createPersonalFeedUseCase: CreatePersonalFeedUseCase,
    private val createGroupFeedUseCase: CreateGroupFeedUseCase
) {

    /**
     * 개인 피드 이벤트의 모든 수신자 피드를 비동기로 생성합니다.
     *
     * @param event 개인 피드 이벤트
     */
    @EventListener
    @Async
    fun handlePersonalFeedEvent(event: PersonalFeedEvent) {
        event.toPersonalFeedData().forEach { data ->
            createPersonalFeedUseCase.execute(
                CreatePersonalFeedCommand(
                    memberId = data.memberId,
                    iconType = IconType.valueOf(data.iconType.name),
                    eventName = data.eventName,
                    messages = data.messages.map(::toFeedMessage),
                    associates = data.resources.map(::toAssociatedResource)
                )
            )
        }
    }

    /**
     * 그룹 피드 이벤트를 비동기로 생성합니다.
     *
     * @param event 그룹 피드 이벤트
     */
    @EventListener
    @Async
    fun handleGroupFeedEvent(event: GroupFeedEvent) {
        val data = event.toGroupFeedData()
        createGroupFeedUseCase.execute(
            CreateGroupFeedCommand(
                groupId = data.groupId,
                iconType = IconType.valueOf(data.iconType.name),
                eventName = data.eventName,
                title = data.title,
                messages = data.messages.map(::toFeedMessage),
                associates = data.resources.map(::toAssociatedResource),
                excludedMemberIds = data.excludedMemberIds
            )
        )
    }

    private fun toFeedMessage(message: FeedEventMessage): FeedMessage {
        return FeedMessage(
            text = message.text,
            textColor = message.textColor,
            highlightColor = message.highlightColor
        )
    }

    private fun toAssociatedResource(resource: FeedEventResource): AssociatedResource {
        return AssociatedResource(
            type = ResourceType.valueOf(resource.type.name),
            resourceId = resource.resourceId
        )
    }
}
