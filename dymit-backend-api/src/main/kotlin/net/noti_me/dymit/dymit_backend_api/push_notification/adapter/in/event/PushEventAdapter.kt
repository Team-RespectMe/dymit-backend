package net.noti_me.dymit.dymit_backend_api.push_notification.adapter.`in`.event

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastPushable
import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.GroupPushEvent
import net.noti_me.dymit.dymit_backend_api.common.event.GroupPushable
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalPushEvent
import net.noti_me.dymit.dymit_backend_api.common.event.Pushable
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushEventData
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushMessagesEvent
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group.SendGroupPushUseCase
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group.dto.SendGroupPushCommand
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal.SendPersonalPushUseCase
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal.dto.SendPersonalPushCommand
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.StudyGroupOwnerChangedEvent
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCommentCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleModifiedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleAssignedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleChangedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleDeletedEventDto
import org.bson.types.ObjectId
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * 외부 모듈 이벤트를 푸시 모듈의 전송 명령으로 변환하는 입력 어댑터입니다.
 */
@Component
class PushEventAdapter(
    private val sendPersonalPushUseCase: SendPersonalPushUseCase,
    private val sendGroupPushUseCase: SendGroupPushUseCase
) {

    /**
     * 개인 푸시 이벤트를 개인 전송 명령으로 변환합니다.
     */
    @EventListener(classes = [PersonalPushEvent::class, PersonalImportantEvent::class])
    fun handlePersonalPushEvent(event: Pushable) {
        sendPersonalPushUseCase.execute(event.toPushMessage().toCommand())
    }

    /**
     * 그룹 푸시 이벤트를 그룹 전송 명령으로 변환합니다.
     */
    @EventListener(classes = [GroupPushEvent::class, GroupImportantEvent::class])
    fun handleGroupPushEvent(event: GroupPushable) {
        sendGroupPushUseCase.execute(event.toGroupPush().toCommand())
    }

    /**
     * 브로드캐스트 이벤트의 각 메시지를 개인 전송 명령으로 변환합니다.
     */
    @EventListener(classes = [BroadcastPushable::class])
    fun handleBroadcastPushEvent(event: BroadcastPushable) {
        event.toPushMessages().forEach { message ->
            sendPersonalPushUseCase.execute(message.toCommand())
        }
    }

    /**
     * 모듈 독립 푸시 이벤트의 각 메시지를 개인 전송 명령으로 변환합니다.
     */
    @EventListener(classes = [PersonalPushMessagesEvent::class])
    fun handlePersonalPushMessagesEvent(event: PersonalPushMessagesEvent) {
        event.toPersonalPushMessages().forEach { message ->
            sendPersonalPushUseCase.execute(message.toCommand())
        }
    }

    /**
     * 스터디 그룹 소유자 변경 이벤트를 새 소유자 대상 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyGroupOwnerChangedEvent(event: StudyGroupOwnerChangedEvent) {
        sendPersonalPushUseCase.execute(
            SendPersonalPushCommand(
                memberId = event.ownerId,
                eventName = StudyGroupOwnerChangedEvent.EVENT_NAME,
                title = "Dymit",
                body = "${event.groupName} 새로운 소유자가 되셨습니다!",
                data = mapOf("groupId" to event.groupId),
                image = null
            )
        )
    }

    /**
     * 일정 생성 이벤트를 그룹 대상 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyScheduleCreated(event: StudyScheduleCreatedEventDto) {
        sendGroupPushUseCase.execute(
            SendGroupPushCommand(
                groupId = ObjectId(event.schedule.groupId),
                title = event.group.name,
                eventName = "STUDY_SCHEDULE_CREATED",
                body = "${event.schedule.session}회차 일정이 추가되었어요!",
                data = scheduleData(event.group.id, event.schedule.id, event.group.ownerId),
                image = null,
                excludedMemberIds = emptySet()
            )
        )
    }

    /**
     * 일정 수정 이벤트를 각 대상 회원 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyScheduleModified(event: StudyScheduleModifiedEventDto) {
        event.memberIds.forEach { memberId ->
            sendPersonalPushUseCase.execute(
                SendPersonalPushCommand(
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

    /**
     * 일정 취소 이벤트를 각 대상 회원 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyScheduleCanceled(event: StudyScheduleCanceledEventDto) {
        event.memberIds.forEach { memberId ->
            sendPersonalPushUseCase.execute(
                SendPersonalPushCommand(
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

    /**
     * 일정 참여 이벤트를 그룹 소유자 대상 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyScheduleParticipated(event: StudyScheduleParticipatedEventDto) {
        sendPersonalPushUseCase.execute(
            SendPersonalPushCommand(
                memberId = ObjectId(event.group.ownerId),
                title = "Dymit",
                body = "${event.group.name} ${event.schedule.session}회차 일정에 ${event.member.nickname} 님이 참여하기로 했어요.",
                eventName = "PARTICIPATE_SCHEDULE",
                data = scheduleData(event.group.id, event.schedule.id, event.group.ownerId),
                image = event.group.profileImageThumbnail
            )
        )
    }

    /**
     * 일정 댓글 생성 이벤트를 그룹 소유자 대상 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyScheduleCommentCreated(event: StudyScheduleCommentCreatedEventDto) {
        sendPersonalPushUseCase.execute(
            SendPersonalPushCommand(
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

    /**
     * 일정 역할 배정 이벤트를 역할 대상 회원 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyScheduleRoleAssigned(event: StudyScheduleRoleAssignedEventDto) {
        sendPersonalPushUseCase.execute(
            roleCommand(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_ASSIGNED",
                body = "${event.group.name} ${event.schedule.session}회차에 새로운 역할이 부여되었어요!",
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    /**
     * 일정 역할 변경 이벤트를 역할 대상 회원 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyScheduleRoleChanged(event: StudyScheduleRoleChangedEventDto) {
        sendPersonalPushUseCase.execute(
            roleCommand(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_CHANGED",
                body = "${event.group.name} ${event.schedule.session}회차 맡은 역할이 변경되었어요!",
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    /**
     * 일정 역할 해제 이벤트를 역할 대상 회원 푸시로 변환합니다.
     */
    @EventListener
    fun handleStudyScheduleRoleDeleted(event: StudyScheduleRoleDeletedEventDto) {
        sendPersonalPushUseCase.execute(
            roleCommand(
                memberId = event.role.memberId,
                eventName = "STUDY_ROLE_DELETED",
                body = "${event.group.name} ${event.schedule.session}회차에 맡은 역할이 해제되었어요!",
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    private fun PersonalPushMessage.toCommand(): SendPersonalPushCommand {
        return SendPersonalPushCommand(
            memberId = memberId,
            eventName = eventName,
            title = title,
            body = body,
            image = image,
            data = data
        )
    }

    private fun PersonalPushEventData.toCommand(): SendPersonalPushCommand {
        return SendPersonalPushCommand(
            memberId = memberId,
            eventName = eventName,
            title = title,
            body = body,
            image = image,
            data = data
        )
    }

    private fun GroupPushMessage.toCommand(): SendGroupPushCommand {
        return SendGroupPushCommand(
            groupId = groupId,
            eventName = eventName,
            title = title,
            body = body,
            image = image,
            data = data,
            excludedMemberIds = excluded
        )
    }

    private fun roleCommand(
        memberId: String,
        eventName: String,
        body: String,
        groupId: String,
        scheduleId: String,
        ownerId: String
    ): SendPersonalPushCommand {
        return SendPersonalPushCommand(
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
