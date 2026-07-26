package net.noti_me.dymit.dymit_backend_api.reminder.domain

import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResource
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushEventData
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushMessagesEvent
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.dto.ReminderStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.dto.ReminderStudyScheduleDto
import org.bson.types.ObjectId

/**
 * 당일 예정된 일정의 푸시와 개인 피드 데이터를 제공하는 이벤트입니다.
 *
 * @param group Reminder 소유 그룹 정보
 * @param schedule Reminder 소유 일정 정보
 * @param memberIds 수신 회원 식별자 목록
 */
class DailyScheduleReminderEvent(
    val group: ReminderStudyGroupDto,
    val schedule: ReminderStudyScheduleDto,
    private val memberIds: List<ObjectId>
) : PersonalPushMessagesEvent, PersonalFeedEvent {

    /**
     * 당일 일정 개인 푸시 메시지를 생성합니다.
     */
    override fun toPersonalPushMessages(): List<PersonalPushEventData> {
        return memberIds.map { memberId ->
            PersonalPushEventData(
                memberId = memberId,
                eventName = EVENT_NAME,
                body = "${group.name} 의 ${schedule.session} 회차 스터디가 오늘 예정되어 있어요!",
                image = group.profileImageThumbnail,
                data = mapOf(
                    "groupId" to group.id.toString(),
                    "scheduleId" to schedule.id.toString(),
                    "ownerId" to group.ownerId.toString()
                )
            )
        }
    }

    /**
     * 당일 일정 개인 피드 데이터를 생성합니다.
     */
    override fun toPersonalFeedData(): List<PersonalFeedEventData> {
        return memberIds.map { memberId ->
            PersonalFeedEventData(
                memberId = memberId.toHexString(),
                iconType = FeedEventIconType.DATE,
                eventName = EVENT_NAME,
                messages = listOf(
                    FeedEventMessage(
                        text = "${group.name} ${schedule.session}회차 일정이 오늘 예정되어 있어요!"
                    )
                ),
                resources = listOf(
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP,
                        resourceId = group.id.toString()
                    ),
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP_SCHEDULE,
                        resourceId = schedule.id.toString()
                    ),
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP_OWNER,
                        resourceId = memberId.toString()
                    )
                )
            )
        }
    }

    private companion object {
        const val EVENT_NAME = "DAILY_SCHEDULE_REMINDER"
    }
}
