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
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleModifiedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipationCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleAssignedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleChangedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleDeletedEventDto
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.GroupMemberJoinEvent
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.GroupMemberLeaveEvent
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.StudyGroupOwnerChangedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.bson.types.ObjectId

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

    @EventListener
    @Async
    fun handleStudyScheduleCreated(event: StudyScheduleCreatedEventDto) {
        groupFeedService.createGroupFeed(
            CreateGroupFeedCommand(
                groupFeed = GroupFeed(
                    groupId = ObjectId(event.schedule.groupId),
                    iconType = IconType.DATE,
                    eventName = "STUDY_SCHEDULE_CREATED",
                    messages = listOf(
                        FeedMessage(
                            text = "${event.group.name} ${event.schedule.session}회차 일정이 추가되었어요!"
                        )
                    ),
                    associates = scheduleAssociates(event.group.id, event.schedule.id, event.group.ownerId)
                )
            )
        )
    }

    @EventListener
    @Async
    fun handleStudyScheduleModified(event: StudyScheduleModifiedEventDto) {
        event.memberIds.forEach { memberId ->
            userFeedService.createUserFeed(
                UserFeed(
                    memberId = ObjectId(memberId),
                    iconType = IconType.DATE,
                    eventName = "STUDY_SCHEDULE_MODIFIED",
                    messages = listOf(
                        FeedMessage(text = event.group.name),
                        FeedMessage(text = " ${event.schedule.session}회차 "),
                        FeedMessage(text = " 일정이 변경되었어요!")
                    ),
                    associates = scheduleAssociates(event.group.id, event.schedule.id, event.group.ownerId)
                )
            )
        }
    }

    @EventListener
    @Async
    fun handleStudyScheduleCanceled(event: StudyScheduleCanceledEventDto) {
        event.memberIds.forEach { memberId ->
            userFeedService.createUserFeed(
                UserFeed(
                    memberId = ObjectId(memberId),
                    iconType = IconType.DATE,
                    eventName = "STUDY_SCHEDULE_CANCELED",
                    messages = listOf(
                        FeedMessage(
                            text = "${event.group.name} ${event.schedule.session}회차 일정이 취소되었어요!"
                        )
                    ),
                    associates = scheduleAssociates(event.group.id, event.schedule.id, event.group.ownerId)
                )
            )
        }
    }

    @EventListener
    @Async
    fun handleStudyScheduleParticipated(event: StudyScheduleParticipatedEventDto) {
        userFeedService.createUserFeed(
            UserFeed(
                memberId = ObjectId(event.group.ownerId),
                messages = listOf(
                    FeedMessage(
                        "${event.group.name} ${event.schedule.session}회차 일정에 ${event.member.nickname} 님이 참여하기로 했어요."
                    )
                ),
                iconType = IconType.CHECK,
                eventName = "PARTICIPATE_SCHEDULE",
                associates = scheduleAssociates(event.group.id, event.schedule.id, event.group.ownerId)
            )
        )
    }

    @EventListener
    @Async
    fun handleStudyScheduleParticipationCanceled(event: StudyScheduleParticipationCanceledEventDto) {
        userFeedService.createUserFeed(
            UserFeed(
                memberId = ObjectId(event.group.ownerId),
                messages = listOf(
                    FeedMessage(
                        "스터디 그룹 ${event.group.name}의 ${event.schedule.session}회차 일정에 ${event.member.nickname} 님이 참여하지 않기로 했어요."
                    )
                ),
                iconType = IconType.BAD,
                eventName = "CANCEL_TO_PARTICIPATE_SCHEDULE",
                associates = listOf(
                    AssociatedResource(ResourceType.STUDY_GROUP, event.group.id),
                    AssociatedResource(ResourceType.STUDY_GROUP_SCHEDULE, event.schedule.id)
                )
            )
        )
    }

    @EventListener
    @Async
    fun handleStudyScheduleRoleAssigned(event: StudyScheduleRoleAssignedEventDto) {
        userFeedService.createUserFeed(
            roleFeed(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_ASSIGNED",
                messages = listOf(
                    FeedMessage(text = "${event.group.name} ${event.schedule.session}회차 "),
                    FeedMessage(
                        text = event.role.roles.joinToString(", "),
                        textColor = "#FF821B",
                        highlightColor = "#FFF2E4"
                    ),
                    FeedMessage(text = "역할이 지정되었습니다.")
                ),
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    @EventListener
    @Async
    fun handleStudyScheduleRoleChanged(event: StudyScheduleRoleChangedEventDto) {
        userFeedService.createUserFeed(
            roleFeed(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_CHANGED",
                messages = listOf(
                    FeedMessage(
                        text = "${event.group.name} ${event.schedule.session} 회차에서 맡은 역할이 변경되었어요!"
                    )
                ),
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    @EventListener
    @Async
    fun handleStudyScheduleRoleDeleted(event: StudyScheduleRoleDeletedEventDto) {
        userFeedService.createUserFeed(
            roleFeed(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_DELETED",
                messages = listOf(
                    FeedMessage(text = "${event.group.name} ${event.schedule.session}회차 "),
                    FeedMessage(
                        text = event.role.roles.joinToString(", "),
                        textColor = "#FF821B",
                        highlightColor = "#FFF2E4"
                    ),
                    FeedMessage(text = " 역할이 해제되었어요!")
                ),
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    private fun roleFeed(
        memberId: String,
        eventName: String,
        messages: List<FeedMessage>,
        groupId: String,
        scheduleId: String,
        ownerId: String
    ): UserFeed {
        return UserFeed(
            iconType = IconType.ROLE,
            eventName = eventName,
            memberId = ObjectId(memberId),
            messages = messages,
            associates = scheduleAssociates(groupId, scheduleId, ownerId)
        )
    }

    private fun scheduleAssociates(
        groupId: String,
        scheduleId: String,
        ownerId: String
    ): List<AssociatedResource> {
        return listOf(
            AssociatedResource(ResourceType.STUDY_GROUP, groupId),
            AssociatedResource(ResourceType.STUDY_GROUP_SCHEDULE, scheduleId),
            AssociatedResource(ResourceType.STUDY_GROUP_OWNER, ownerId)
        )
    }
}
