package net.noti_me.dymit.dymit_backend_api.feed.adapter.`in`.event

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreateGroupFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreatePersonalFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreateGroupFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreatePersonalFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.domain.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.feed.domain.FeedMessage
import net.noti_me.dymit.dymit_backend_api.feed.domain.IconType
import net.noti_me.dymit.dymit_backend_api.feed.domain.ResourceType
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleModifiedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipationCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleAssignedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleChangedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleDeletedEventDto
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * StudySchedule 이벤트를 Feed 입력 명령으로 변환하는 어댑터입니다.
 *
 * @param createPersonalFeedUseCase 개인 피드 생성 유스케이스
 * @param createGroupFeedUseCase 그룹 피드 생성 유스케이스
 */
@Component
class StudyScheduleFeedEventAdapter(
    private val createPersonalFeedUseCase: CreatePersonalFeedUseCase,
    private val createGroupFeedUseCase: CreateGroupFeedUseCase
) {

    /**
     * 일정 생성 이벤트의 그룹 피드를 생성합니다.
     *
     * @param event 일정 생성 이벤트
     */
    @EventListener
    @Async
    fun handleStudyScheduleCreated(event: StudyScheduleCreatedEventDto) {
        createGroupFeedUseCase.execute(
            CreateGroupFeedCommand(
                groupId = event.schedule.groupId,
                iconType = IconType.DATE,
                eventName = "STUDY_SCHEDULE_CREATED",
                messages = listOf(
                    FeedMessage(
                        text = "${event.group.name} ${event.schedule.session}회차 일정이 추가되었어요!"
                    )
                ),
                associates = scheduleAssociates(
                    event.group.id,
                    event.schedule.id,
                    event.group.ownerId
                )
            )
        )
    }

    /**
     * 일정 수정 이벤트의 수신자별 피드를 생성합니다.
     *
     * @param event 일정 수정 이벤트
     */
    @EventListener
    @Async
    fun handleStudyScheduleModified(event: StudyScheduleModifiedEventDto) {
        event.memberIds.forEach { memberId ->
            createPersonalFeedUseCase.execute(
                scheduleCommand(
                    memberId = memberId,
                    iconType = IconType.DATE,
                    eventName = "STUDY_SCHEDULE_MODIFIED",
                    messages = listOf(
                        FeedMessage(text = event.group.name),
                        FeedMessage(text = " ${event.schedule.session}회차 "),
                        FeedMessage(text = " 일정이 변경되었어요!")
                    ),
                    groupId = event.group.id,
                    scheduleId = event.schedule.id,
                    ownerId = event.group.ownerId
                )
            )
        }
    }

    /**
     * 일정 취소 이벤트의 수신자별 피드를 생성합니다.
     *
     * @param event 일정 취소 이벤트
     */
    @EventListener
    @Async
    fun handleStudyScheduleCanceled(event: StudyScheduleCanceledEventDto) {
        event.memberIds.forEach { memberId ->
            createPersonalFeedUseCase.execute(
                scheduleCommand(
                    memberId = memberId,
                    iconType = IconType.DATE,
                    eventName = "STUDY_SCHEDULE_CANCELED",
                    messages = listOf(
                        FeedMessage(
                            text = "${event.group.name} ${event.schedule.session}회차 일정이 취소되었어요!"
                        )
                    ),
                    groupId = event.group.id,
                    scheduleId = event.schedule.id,
                    ownerId = event.group.ownerId
                )
            )
        }
    }

    /**
     * 일정 참가 이벤트의 그룹 소유자 피드를 생성합니다.
     *
     * @param event 일정 참가 이벤트
     */
    @EventListener
    @Async
    fun handleStudyScheduleParticipated(event: StudyScheduleParticipatedEventDto) {
        createPersonalFeedUseCase.execute(
            scheduleCommand(
                memberId = event.group.ownerId,
                iconType = IconType.CHECK,
                eventName = "PARTICIPATE_SCHEDULE",
                messages = listOf(
                    FeedMessage(
                        "${event.group.name} ${event.schedule.session}회차 일정에 ${event.member.nickname} 님이 참여하기로 했어요."
                    )
                ),
                groupId = event.group.id,
                scheduleId = event.schedule.id,
                ownerId = event.group.ownerId
            )
        )
    }

    /**
     * 일정 참가 취소 이벤트의 그룹 소유자 피드를 생성합니다.
     *
     * @param event 일정 참가 취소 이벤트
     */
    @EventListener
    @Async
    fun handleStudyScheduleParticipationCanceled(
        event: StudyScheduleParticipationCanceledEventDto
    ) {
        createPersonalFeedUseCase.execute(
            CreatePersonalFeedCommand(
                memberId = event.group.ownerId,
                iconType = IconType.BAD,
                eventName = "CANCEL_TO_PARTICIPATE_SCHEDULE",
                messages = listOf(
                    FeedMessage(
                        "스터디 그룹 ${event.group.name}의 ${event.schedule.session}회차 일정에 ${event.member.nickname} 님이 참여하지 않기로 했어요."
                    )
                ),
                associates = listOf(
                    AssociatedResource(ResourceType.STUDY_GROUP, event.group.id),
                    AssociatedResource(ResourceType.STUDY_GROUP_SCHEDULE, event.schedule.id)
                )
            )
        )
    }

    /**
     * 일정 역할 지정 이벤트의 대상 회원 피드를 생성합니다.
     *
     * @param event 역할 지정 이벤트
     */
    @EventListener
    @Async
    fun handleStudyScheduleRoleAssigned(event: StudyScheduleRoleAssignedEventDto) {
        createPersonalFeedUseCase.execute(
            roleCommand(
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

    /**
     * 일정 역할 변경 이벤트의 대상 회원 피드를 생성합니다.
     *
     * @param event 역할 변경 이벤트
     */
    @EventListener
    @Async
    fun handleStudyScheduleRoleChanged(event: StudyScheduleRoleChangedEventDto) {
        createPersonalFeedUseCase.execute(
            roleCommand(
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

    /**
     * 일정 역할 삭제 이벤트의 대상 회원 피드를 생성합니다.
     *
     * @param event 역할 삭제 이벤트
     */
    @EventListener
    @Async
    fun handleStudyScheduleRoleDeleted(event: StudyScheduleRoleDeletedEventDto) {
        createPersonalFeedUseCase.execute(
            roleCommand(
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

    private fun roleCommand(
        memberId: String,
        eventName: String,
        messages: List<FeedMessage>,
        groupId: String,
        scheduleId: String,
        ownerId: String
    ): CreatePersonalFeedCommand {
        return scheduleCommand(
            memberId = memberId,
            iconType = IconType.ROLE,
            eventName = eventName,
            messages = messages,
            groupId = groupId,
            scheduleId = scheduleId,
            ownerId = ownerId
        )
    }

    private fun scheduleCommand(
        memberId: String,
        iconType: IconType,
        eventName: String,
        messages: List<FeedMessage>,
        groupId: String,
        scheduleId: String,
        ownerId: String
    ): CreatePersonalFeedCommand {
        return CreatePersonalFeedCommand(
            memberId = memberId,
            iconType = iconType,
            eventName = eventName,
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
