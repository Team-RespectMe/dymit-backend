package net.noti_me.dymit.dymit_backend_api.reminder.domain

import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushEventData
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushMessagesEvent
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.dto.ReminderStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.dto.ReminderStudyScheduleDto
import org.bson.types.ObjectId

/**
 * 한 시간 이내 예정된 일정의 개인 푸시 데이터를 제공하는 이벤트입니다.
 *
 * @param group Reminder 소유 그룹 정보
 * @param schedule Reminder 소유 일정 정보
 * @param memberIds 수신 회원 식별자 목록
 */
class HourlyScheduleReminderEvent(
    val group: ReminderStudyGroupDto,
    val schedule: ReminderStudyScheduleDto,
    val memberIds: List<ObjectId>
) : PersonalPushMessagesEvent {

    /**
     * 시작 예정 일정의 개인 푸시 메시지를 생성합니다.
     */
    override fun toPersonalPushMessages(): List<PersonalPushEventData> {
        return memberIds.map { memberId ->
            PersonalPushEventData(
                memberId = memberId,
                eventName = EVENT_NAME,
                body = "${group.name} 스터디의 ${schedule.session} 회차 일정이 곧 시작됩니다!",
                image = group.profileImageThumbnail,
                data = mapOf(
                    "groupId" to group.id.toHexString(),
                    "scheduleId" to schedule.id.toHexString(),
                    "ownerId" to group.ownerId.toString()
                )
            )
        }
    }

    private companion object {
        const val EVENT_NAME = "HOURLY_SCHEDULE_REMINDER"
    }
}
