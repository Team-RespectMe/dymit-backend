package net.noti_me.dymit.dymit_backend_api.application.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.ScheduleTimeChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyRoleAssignedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyScheduleCanceledEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyScheduleCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.userFeed.UserFeedRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
@Async
class StudyScheduleEventHandler(
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val studyGroupRepository: LoadStudyGroupPort,
    private val participantRepository: ScheduleParticipantRepository,
    private val userFeedRepository: UserFeedRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun handleStudyScheduleCreatedEvent(event: StudyScheduleCreatedEvent) {
        logger.info("스터디 일정 생성 이벤트 처리 시작: ${event.studySchedule.id}")
        val schedule = event.studySchedule
        val groupId = schedule.groupId
        val group = studyGroupRepository.loadByGroupId(groupId.toHexString())
            ?: run {
                logger.error("스터디 그룹을 찾을 수 없습니다. groupId: ${groupId.toHexString()}")
                return
            }
        val members = studyGroupMemberRepository.findByGroupId(groupId)

        members.forEach { member ->
            val feed = UserFeed(
                memberId = member.memberId,
                message = "[${group.name}] 새로운 스터디 일정: ${schedule.title}",
                associates = listOf(
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP,
                        resourceId = group.id.toHexString()
                    ),
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP_SCHEDULE,
                        resourceId = schedule.id.toHexString()
                    )
                )
            )
            userFeedRepository.save(feed)
        }

        // 역할이 부여된 멤버들에게도 별도의 알림 전송
        val roleAssignedMemberIds = schedule.roles.map(ScheduleRole::memberId).toSet()
        roleAssignedMemberIds.forEach { assignedMember ->
            val feed = UserFeed(
                memberId = assignedMember,
                message = "[${group.name}] 에서 회원님에게 스터디 일정에 필요한 역할을 부여했어요.",
                associates = listOf(
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP,
                        resourceId = group.id.toHexString()
                    ),
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP_SCHEDULE,
                        resourceId = schedule.id.toHexString()
                    ),
                )
            )
            userFeedRepository.save(feed)
        }
    }

    /**
     * 스터디 일정 변경 이벤트 처리
     * @param event ScheduleTimeChangedEvent
     */
    @EventListener
    fun handleStudyScheduleUpdatedEvent(event: ScheduleTimeChangedEvent) {
        val schedule = event.schedule
        val groupId = schedule.groupId
        val group = studyGroupRepository.loadByGroupId(groupId.toHexString())
            ?: run {
                logger.error("스터디 그룹을 찾을 수 없습니다. groupId: ${groupId.toHexString()}")
                return
            }
        val members = studyGroupMemberRepository.findByGroupId(groupId)

        members.forEach { member ->
            val feed = UserFeed(
                memberId = member.memberId,
                message = "[${group.name}] 스터디 일정이 변경되었어요. ${schedule.title}",
                associates = listOf(
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP,
                        resourceId = group.id.toHexString()
                    ),
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP_SCHEDULE,
                        resourceId = schedule.id.toHexString()
                    )
                )
            )
            userFeedRepository.save(feed)
        }
    }

    /**
     * 스터디 일정에 역할이 부여된 회원들에게 알림 및 피드 생성
     */
    @EventListener
    fun handleStudyScheduleRoleAssignedEvent(event: StudyRoleAssignedEvent) {
        logger.debug("스터디 일정 역할 부여 이벤트 처리 시작: ${event.role}, 일정 ID: ${event.schedule.id}")
        val role = event.role
        val schedule = event.schedule
        val groupId = schedule.groupId
        val group = studyGroupRepository.loadByGroupId(groupId.toHexString())
            ?: return
        val memberIds = schedule.roles.map { it -> it.memberId }
            .toSet()
            .toList()
        studyGroupMemberRepository.findByGroupIdAndMemberIdsIn(groupId, memberIds)
            .forEach { groupMember ->
                val feed = UserFeed(
                    memberId = groupMember.memberId,
                    message = "[${group.name}] 에서 회원님에게 스터디 일정에 필요한 역할을 부여했어요.",
                    associates = listOf(
                        AssociatedResource(
                            type = ResourceType.STUDY_GROUP,
                            resourceId = group.id.toHexString()
                        ),
                        AssociatedResource(
                            type = ResourceType.STUDY_GROUP_SCHEDULE,
                            resourceId = schedule.id.toHexString()
                        )
                    )
                )
            }
    }

    /**
     * 스터디 일정 취소 시 그룹 멤버들에게 알림 전송 및 피드 생성
     */
    @EventListener
    fun handleStudyScheduleCancelledEvent(event: StudyScheduleCanceledEvent) {
        // 참여자 대상으로만 취소 사실 통보
        val schedule = event.schedule
        val groupId = schedule.groupId
        studyGroupRepository.loadByGroupId(groupId.toHexString())
            ?.let { group ->
                participantRepository.getByScheduleId(schedule.id)
                    .forEach { participant ->
                        val userFeed = UserFeed(
                            memberId = participant.memberId,
                            message = "[${group.name}] 스터디 일정이 취소되었어요. ${schedule.title}",
                            associates = listOf(
                                AssociatedResource(
                                    type = ResourceType.STUDY_GROUP,
                                    resourceId = group.id.toHexString()
                                ),
                                AssociatedResource(
                                    type = ResourceType.STUDY_GROUP_SCHEDULE,
                                    resourceId = schedule.id.toHexString()
                                )
                            )
                        )
                        participantRepository.delete(participant)
                        userFeedRepository.save(userFeed)
                    }
            }
    }
}