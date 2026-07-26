package net.noti_me.dymit.dymit_backend_api.application.push_notification

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastPushable
import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.GroupPushEvent
import net.noti_me.dymit.dymit_backend_api.common.event.GroupPushable
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalPushEvent
import net.noti_me.dymit.dymit_backend_api.common.event.Pushable
import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCommentCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleModifiedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleAssignedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleChangedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleDeletedEventDto
import org.bson.types.ObjectId
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.StudyGroupOwnerChangedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class PersonalPushEventHandler(
    private val pushService: PushService
) {

    @EventListener(classes = [PersonalPushEvent::class, PersonalImportantEvent::class])
    fun handlePersonalPushEvent(event: Pushable) {
        pushService.sendPersonalPush(event.toPushMessage())
    }

    @EventListener(classes = [GroupPushEvent::class, GroupImportantEvent::class])
    fun handleGroupPushEvent(event: GroupPushable) {
        pushService.sendGroupPush(event.toGroupPush())
    }

    @EventListener(classes = [BroadcastPushable::class])
    fun handleBroadcastPushEvent(event: BroadcastPushable) {
        val pushMessages = event.toPushMessages()
        pushMessages.forEach { pushMessage ->
            pushService.sendPersonalPush(pushMessage)
        }
    }

    @EventListener
    fun handleStudyGroupOwnerChangedEvent(event: StudyGroupOwnerChangedEvent) {
        pushService.sendPersonalPush(
            PersonalPushMessage(
                memberId = event.ownerId,
                eventName = StudyGroupOwnerChangedEvent.EVENT_NAME,
                title = "Dymit",
                body = "${event.groupName} 새로운 소유자가 되셨습니다!",
                data = mapOf("groupId" to event.groupId),
                image = null
            )
        )
    }

    @EventListener
    fun handleStudyScheduleCreated(event: StudyScheduleCreatedEventDto) {
        pushService.sendGroupPush(
            GroupPushMessage(
                groupId = ObjectId(event.schedule.groupId),
                title = event.group.name,
                eventName = "STUDY_SCHEDULE_CREATED",
                body = "${event.schedule.session}회차 일정이 추가되었어요!",
                data = scheduleData(event.group.id, event.schedule.id, event.group.ownerId)
            )
        )
    }

    @EventListener
    fun handleStudyScheduleModified(event: StudyScheduleModifiedEventDto) {
        event.memberIds.forEach { memberId ->
            pushService.sendPersonalPush(
                PersonalPushMessage(
                    memberId = ObjectId(memberId),
                    eventName = "STUDY_SCHEDULE_MODIFIED",
                    title = event.group.name,
                    body = "${event.schedule.session}회차 일정이 변경되었어요!",
                    image = null,
                    data = scheduleData(event.group.id, event.schedule.id, event.group.ownerId)
                )
            )
        }
    }

    @EventListener
    fun handleStudyScheduleCanceled(event: StudyScheduleCanceledEventDto) {
        event.memberIds.forEach { memberId ->
            pushService.sendPersonalPush(
                PersonalPushMessage(
                    memberId = ObjectId(memberId),
                    eventName = "STUDY_SCHEDULE_CANCELED",
                    title = event.group.name,
                    body = "${event.schedule.session}회차 일정이 취소되었어요!",
                    image = null,
                    data = scheduleData(event.group.id, event.schedule.id, event.group.ownerId)
                )
            )
        }
    }

    @EventListener
    fun handleStudyScheduleParticipated(event: StudyScheduleParticipatedEventDto) {
        pushService.sendPersonalPush(
            PersonalPushMessage(
                memberId = ObjectId(event.group.ownerId),
                title = "Dymit",
                body = "${event.group.name} ${event.schedule.session}회차 일정에 ${event.member.nickname} 님이 참여하기로 했어요.",
                eventName = "PARTICIPATE_SCHEDULE",
                data = scheduleData(event.group.id, event.schedule.id, event.group.ownerId),
                image = event.group.profileImageThumbnail
            )
        )
    }

    @EventListener
    fun handleStudyScheduleCommentCreated(event: StudyScheduleCommentCreatedEventDto) {
        pushService.sendPersonalPush(
            PersonalPushMessage(
                memberId = ObjectId(event.group.ownerId),
                title = event.group.name,
                body = "${event.schedule.session}회차 일정에 댓글이 달렸어요!",
                eventName = "SCHEDULE_COMMENT_CREATED",
                data = scheduleData(event.group.id, event.schedule.id, event.group.ownerId) +
                    ("commentId" to event.commentId),
                image = event.group.profileImageThumbnail
            )
        )
    }

    @EventListener
    fun handleStudyScheduleRoleAssigned(event: StudyScheduleRoleAssignedEventDto) {
        pushService.sendPersonalPush(
            rolePush(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_ASSIGNED",
                body = "${event.group.name} ${event.schedule.session}회차에 새로운 역할이 부여되었어요!",
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    @EventListener
    fun handleStudyScheduleRoleChanged(event: StudyScheduleRoleChangedEventDto) {
        pushService.sendPersonalPush(
            rolePush(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_CHANGED",
                body = "${event.group.name} ${event.schedule.session}회차 맡은 역할이 변경되었어요!",
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    @EventListener
    fun handleStudyScheduleRoleDeleted(event: StudyScheduleRoleDeletedEventDto) {
        pushService.sendPersonalPush(
            rolePush(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_DELETED",
                body = "${event.group.name} ${event.schedule.session}회차에 맡은 역할이 해제되었어요!",
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    private fun rolePush(
        memberId: String,
        eventName: String,
        body: String,
        groupId: String,
        scheduleId: String,
        ownerId: String
    ): PersonalPushMessage {
        return PersonalPushMessage(
            memberId = ObjectId(memberId),
            title = "Dymit",
            eventName = eventName,
            body = body,
            data = scheduleData(groupId, scheduleId, ownerId),
            image = null
        )
    }

    private fun scheduleData(
        groupId: String,
        scheduleId: String,
        ownerId: String
    ): Map<String, String> {
        return mapOf(
            "groupId" to groupId,
            "scheduleId" to scheduleId,
            "ownerId" to ownerId
        )
    }
}
