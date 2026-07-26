package net.noti_me.dymit.dymit_backend_api.feed.adapter.`in`.event

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreatePersonalFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreatePersonalFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.domain.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.feed.domain.FeedMessage
import net.noti_me.dymit.dymit_backend_api.feed.domain.IconType
import net.noti_me.dymit.dymit_backend_api.feed.domain.ResourceType
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.GroupMemberJoinEvent
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.GroupMemberLeaveEvent
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.StudyGroupOwnerChangedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * StudyGroup 이벤트를 개인 피드 명령으로 변환하는 입력 어댑터입니다.
 *
 * @param createPersonalFeedUseCase 개인 피드 생성 유스케이스
 */
@Component
class StudyGroupFeedEventAdapter(
    private val createPersonalFeedUseCase: CreatePersonalFeedUseCase
) {

    /**
     * 그룹 참가 이벤트의 기존 소유자 피드를 생성합니다.
     *
     * @param event 그룹 참가 이벤트
     */
    @EventListener
    @Async
    fun handleGroupMemberJoinEvent(event: GroupMemberJoinEvent) {
        createPersonalFeedUseCase.execute(
            groupEventCommand(
                memberId = event.ownerId.toHexString(),
                iconType = IconType.APPLAUSE,
                eventName = GroupMemberJoinEvent.EVENT_NAME,
                message = "${event.memberNickname}님이 ${event.groupName}에 참가하셨습니다.",
                groupId = event.groupId
            )
        )
    }

    /**
     * 그룹 탈퇴 이벤트의 기존 소유자 피드를 생성합니다.
     *
     * @param event 그룹 탈퇴 이벤트
     */
    @EventListener
    @Async
    fun handleGroupMemberLeaveEvent(event: GroupMemberLeaveEvent) {
        createPersonalFeedUseCase.execute(
            groupEventCommand(
                memberId = event.ownerId.toHexString(),
                iconType = IconType.BAD,
                eventName = GroupMemberLeaveEvent.EVENT_NAME,
                message = "${event.memberNickname} 님이 ${event.groupName}에서 탈퇴하셨습니다.",
                groupId = event.groupId
            )
        )
    }

    /**
     * 그룹 소유자 변경 이벤트의 기존 소유자 피드를 생성합니다.
     *
     * @param event 그룹 소유자 변경 이벤트
     */
    @EventListener
    @Async
    fun handleStudyGroupOwnerChangedEvent(event: StudyGroupOwnerChangedEvent) {
        createPersonalFeedUseCase.execute(
            groupEventCommand(
                memberId = event.ownerId.toHexString(),
                iconType = IconType.DATE,
                eventName = StudyGroupOwnerChangedEvent.EVENT_NAME,
                message = "${event.groupName}의 소유자 위임",
                groupId = event.groupId
            )
        )
    }

    private fun groupEventCommand(
        memberId: String,
        iconType: IconType,
        eventName: String,
        message: String,
        groupId: String
    ): CreatePersonalFeedCommand {
        return CreatePersonalFeedCommand(
            memberId = memberId,
            iconType = iconType,
            eventName = eventName,
            messages = listOf(FeedMessage(text = message)),
            associates = listOf(
                AssociatedResource(ResourceType.STUDY_GROUP, groupId)
            )
        )
    }
}
