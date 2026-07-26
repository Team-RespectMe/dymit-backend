package net.noti_me.dymit.dymit_backend_api.application.reminder.events

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastPushable
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto as StudySchedule
import org.bson.types.ObjectId

class HourlyScheduleReminderEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    val memberIds: List<ObjectId>
) : BroadcastPushable {

    private val EVENT_NAME = "HOURLY_SCHEDULE_REMINDER"

    override fun toPushMessages(): List<PersonalPushMessage> {
        return memberIds.map { memberId ->
            PersonalPushMessage(
                memberId = memberId,
                eventName = EVENT_NAME,
                body = "${group.name} 스터디의 ${schedule.session} 회차 일정이 곧 시작됩니다!",
                image = group.profileImage.thumbnail,
                data = mapOf(
                    "groupId" to group.identifier,
                    "scheduleId" to schedule.id.toHexString(),
                    "ownerId" to group.ownerId.toString()
                )
            )
        }
    }
}
