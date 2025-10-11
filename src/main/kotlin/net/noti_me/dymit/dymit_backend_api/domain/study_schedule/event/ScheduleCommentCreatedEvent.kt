package net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event

import net.noti_me.dymit.dymit_backend_api.common.event.PersonalPushEvent
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule

class ScheduleCommentCreatedEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    val comment: ScheduleComment
): PersonalPushEvent(comment) {

    override fun processPushMessage(): PersonalPushMessage {
        return PersonalPushMessage(
            memberId = group.ownerId,
            title = group.name,
            body = "${schedule.session}회차 일정에 댓글이 달렸어요!",
            data = mapOf(
                "type" to "SCHEDULE_COMMENT",
                "groupId" to schedule.groupId.toHexString(),
                "scheduleId" to schedule.identifier,
                "commentId" to comment.identifier,
            ),
            image = group.profileImage.url,
        )
    }
}