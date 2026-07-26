package net.noti_me.dymit.dymit_backend_api.application.feed

import net.noti_me.dymit.dymit_backend_api.application.feed.dto.CreateGroupFeedCommand
import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastFeedable
import net.noti_me.dymit.dymit_backend_api.common.event.Feedable
import net.noti_me.dymit.dymit_backend_api.common.event.GroupFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.GroupFeedable
import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.GroupMemberJoinEvent
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.GroupMemberLeaveEvent
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.StudyGroupOwnerChangedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class FeedEventHandler(
    private val userFeedService: UserFeedService,
    private val groupFeedService: GroupFeedService
) {

    @EventListener(classes = [PersonalFeedEvent::class, PersonalImportantEvent::class])
    @Async
    fun handleFeedableEvent(event: Feedable) {
        userFeedService.createUserFeed(userFeed=event.toUserFeed())
    }

    @EventListener(classes = [GroupFeedEvent::class, GroupImportantEvent::class])
    @Async
    fun handleGroupFeedableEvent(event: GroupFeedable) {
        groupFeedService.createGroupFeed(CreateGroupFeedCommand(
            groupFeed = event.toGroupFeed()
        ))
    }

    @EventListener(classes = [BroadcastFeedable::class])
    @Async
    fun handleBroadcastFeedableEvent(event: BroadcastFeedable) {
        val feeds = event.toFeeds()
        feeds.forEach { feed ->
            userFeedService.createUserFeed(userFeed = feed)
        }
    }

    @EventListener
    @Async
    fun handleGroupMemberJoinEvent(event: GroupMemberJoinEvent) {
        userFeedService.createUserFeed(
            UserFeed(
                memberId = event.ownerId,
                messages = listOf(
                    FeedMessage(text = "${event.memberNickname}님이 ${event.groupName}에 참가하셨습니다.")
                ),
                iconType = IconType.APPLAUSE,
                eventName = GroupMemberJoinEvent.EVENT_NAME,
                associates = listOf(
                    AssociatedResource(ResourceType.STUDY_GROUP, event.groupId)
                )
            )
        )
    }

    @EventListener
    @Async
    fun handleGroupMemberLeaveEvent(event: GroupMemberLeaveEvent) {
        userFeedService.createUserFeed(
            UserFeed(
                memberId = event.ownerId,
                messages = listOf(
                    FeedMessage(text = "${event.memberNickname} 님이 ${event.groupName}에서 탈퇴하셨습니다.")
                ),
                iconType = IconType.BAD,
                eventName = GroupMemberLeaveEvent.EVENT_NAME,
                associates = listOf(
                    AssociatedResource(ResourceType.STUDY_GROUP, event.groupId)
                )
            )
        )
    }

    @EventListener
    @Async
    fun handleStudyGroupOwnerChangedEvent(event: StudyGroupOwnerChangedEvent) {
        userFeedService.createUserFeed(
            UserFeed(
                memberId = event.ownerId,
                messages = listOf(
                    FeedMessage(text = "${event.groupName}의 소유자 위임")
                ),
                iconType = IconType.DATE,
                eventName = StudyGroupOwnerChangedEvent.EVENT_NAME,
                associates = listOf(
                    AssociatedResource(ResourceType.STUDY_GROUP, event.groupId)
                )
            )
        )
    }
}
