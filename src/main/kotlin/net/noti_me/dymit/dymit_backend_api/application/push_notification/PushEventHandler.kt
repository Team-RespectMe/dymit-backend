package net.noti_me.dymit.dymit_backend_api.application.push_notification

import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
@Async
class PushEventHandler(
    private val pushService: PushService,
    private val loadMemberPort: LoadMemberPort,
    private val groupMemberRepository: StudyGroupMemberRepository,
    private val participantRepository: ScheduleParticipantRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    @EventListener
    fun handleGroupPushEvent(event: GroupBroadCastPushEvent) {
        val groupMembers = groupMemberRepository.findByGroupId(event.groupId)
        val memberIds = groupMembers.map { it.memberId.toHexString() }
        val deviceTokens = loadMemberPort.loadByIds(memberIds)
            .flatMap { it.deviceTokens }
            .map { it.token }

        if ( deviceTokens.isEmpty() ) return

        pushService.sendPushNotifications(
            deviceTokens = deviceTokens,
            title = event.title,
            body = event.body,
            image = event.image,
            data = event.data,
        )
    }

    @EventListener
    fun handleSchedulePushEvent(event: SchedulePushEvent) {
        val participants = participantRepository.getByScheduleId(event.scheduleId)
        val memberIds = participants.map { it.memberId.toHexString() }
        val deviceTokens = loadMemberPort.loadByIds(memberIds)
            .flatMap { it.deviceTokens }
            .map { it.token }

        if (deviceTokens.isEmpty()) return

        pushService.sendPushNotifications(
            deviceTokens = deviceTokens,
            title = event.title,
            body = event.body,
            image = event.image,
            data = event.data,
        )
    }

    @EventListener
    fun handleMemberPushEvent(event: MemberPushEvent) {
        loadMemberPort.loadById(event.memberId.toHexString())
            ?.let{  member ->
                pushService.sendPushNotifications(
                    deviceTokens = member.deviceTokens.map{ it.token },
                    title = event.title,
                    body = event.body,
                    image = event.image,
                    data = event.data,
                )
            }
    }
}